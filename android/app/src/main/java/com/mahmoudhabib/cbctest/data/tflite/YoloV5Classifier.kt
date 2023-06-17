package com.mahmoudhabib.cbctest.data.tflite

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.RectF
import android.os.Build
import com.mahmoudhabib.cbctest.domain.model.Recognition
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.nnapi.NnApiDelegate
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.util.PriorityQueue
import kotlin.math.pow

class YoloV5Classifier private constructor() {


    //config yolo
    var inputSize = -1
        private set
    private var outputBox = 0
    private lateinit var nnApiDelegate: NnApiDelegate
    private lateinit var mappedByteBuffer: MappedByteBuffer


    // Config values.
    // Pre-allocated buffers.
    private val labels = mutableListOf<String>()
    private lateinit var intValues: IntArray
    private lateinit var imgData: ByteBuffer
    private lateinit var outData: ByteBuffer
    private lateinit var interpreter: Interpreter
    private var numClass = 0

    //non maximum suppression
    private fun nms(list: List<Recognition>): List<Recognition> {
        val nmsList = mutableListOf<Recognition>()
        for (k in labels.indices) {
            //1.find max confidence per class
            val pq = PriorityQueue(
                50,
                Comparator<Recognition> { lhs, rhs -> // Intentionally reversed to put high confidence at the head of the queue.
                    rhs.confidence.compareTo(lhs.confidence)
                })
            for (i in list.indices) {
                if (list[i].detectedClass == k) {
                    pq.add(list[i])
                }
            }

            //2.do non maximum suppression
            while (pq.size > 0) {
                //insert detection with max confidence
                val a = arrayOf<Recognition>()
                val detections = pq.toArray(a)
                val max = detections[0]
                nmsList.add(max)
                pq.clear()
                for (j in 1 until detections.size) {
                    val detection = detections[j]
                    val b = detection.location
                    if (boxIou(max.location, b) < NMS_THRESH) {
                        pq.add(detection)
                    }
                }
            }
        }
        return nmsList
    }

    private fun boxIou(a: RectF, b: RectF): Float {
        return boxIntersection(a, b) / boxUnion(a, b)
    }

    private fun boxIntersection(a: RectF, b: RectF): Float {
        val w = overlap(
            (a.left + a.right) / 2, a.right - a.left,
            (b.left + b.right) / 2, b.right - b.left
        )
        val h = overlap(
            (a.top + a.bottom) / 2, a.bottom - a.top,
            (b.top + b.bottom) / 2, b.bottom - b.top
        )
        return if (w < 0 || h < 0) 0f else w * h
    }

    private fun boxUnion(a: RectF, b: RectF): Float {
        val i = boxIntersection(a, b)
        return (a.right - a.left) * (a.bottom - a.top) + (b.right - b.left) * (b.bottom - b.top) - i
    }

    private fun overlap(x1: Float, w1: Float, x2: Float, w2: Float): Float {
        val l1 = x1 - w1 / 2
        val l2 = x2 - w2 / 2
        val left = if (l1 > l2) l1 else l2
        val r1 = x1 + w1 / 2
        val r2 = x2 + w2 / 2
        val right = if (r1 < r2) r1 else r2
        return right - left
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        imgData.rewind()
        for (i in 0 until inputSize) {
            for (j in 0 until inputSize) {
                val pixelValue = intValues[i * inputSize + j]
                imgData.putFloat(((pixelValue shr 16 and 0xFF)) / IMAGE_STD)
                imgData.putFloat(((pixelValue shr 8 and 0xFF)) / IMAGE_STD)
                imgData.putFloat(((pixelValue and 0xFF)) / IMAGE_STD)
            }
        }
        return imgData
    }

    fun recognizeImage(bitmap: Bitmap): List<Recognition> {
        convertBitmapToByteBuffer(bitmap)
        val outputMap: MutableMap<Int, Any> = HashMap()
        outData.rewind()
        outputMap[0] = outData
        val inputArray = arrayOf<Any>(imgData)
        interpreter.runForMultipleInputsOutputs(inputArray, outputMap)
        val byteBuffer = outputMap[0] as ByteBuffer
        byteBuffer.rewind()
        val detections = mutableListOf<Recognition>()
        val out = Array(1) {
            Array(outputBox) {
                FloatArray(numClass + 5)
            }
        }
        for (i in 0 until outputBox) {
            for (j in 0 until numClass + 5) {
                out[0][i][j] = byteBuffer.float
            }
            // Denormalize xywh
            for (j in 0..3) {
                out[0][i][j] *= inputSize.toFloat()
            }
        }
        for (i in 0 until outputBox) {
            val offset = 0
            val confidence = out[0][i][4]
            var detectedClass = -1
            var maxClass = 0f
            val classes = FloatArray(labels.size)
            for (c in labels.indices) {
                classes[c] = out[0][i][5 + c]
            }
            for (c in labels.indices) {
                if (classes[c] > maxClass) {
                    detectedClass = c
                    maxClass = classes[c]
                }
            }
            val confidenceInClass = maxClass * confidence
            if (confidenceInClass > objThresh) {
                val xPos = out[0][i][0]
                val yPos = out[0][i][1]
                val w = out[0][i][2]
                val h = out[0][i][3]
                val rect = RectF(
                    0f.coerceAtLeast(xPos - w / 2),
                    0f.coerceAtLeast(yPos - h / 2),
                    (bitmap.width - 1).toFloat().coerceAtMost(xPos + w / 2),
                    (bitmap.height - 1).toFloat().coerceAtMost(yPos + h / 2)
                )
                detections.add(
                    Recognition(
                        id = "" + offset,
                        title = labels[detectedClass],
                        confidence = confidenceInClass,
                        location = rect,
                        detectedClass = detectedClass
                    )
                )
            }
        }

        return nms(detections)
    }

    companion object {
        private const val IMAGE_STD = 255.0f
        private const val NMS_THRESH = 0.6f
        private const val objThresh = 0.3f


        @Throws(IOException::class)
        fun create(
            assetManager: AssetManager,
            modelFilename: String,
            labelFilename: String,
            inputSize: Int,
            isNnApi: Boolean = true,
        ): YoloV5Classifier {
            val classifier = YoloV5Classifier()
            val labelsInput = assetManager.open(labelFilename)
            val bufferedReader = BufferedReader(InputStreamReader(labelsInput))
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                line?.let { classifier.labels.add(it) }
            }
            bufferedReader.close()
            try {
                val options = Interpreter.Options()
                if (isNnApi) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        classifier.nnApiDelegate = NnApiDelegate()
                        options.addDelegate(classifier.nnApiDelegate)
                        options.useNNAPI = true
                    }
                }
                classifier.mappedByteBuffer = Utils.loadModelFile(assetManager, modelFilename)
                classifier.interpreter = Interpreter(classifier.mappedByteBuffer, options)
            } catch (e: Exception) {
                throw e
            }

            // Pre-allocate buffers.
            val numBytesPerChannel = 4 // Floating point
            classifier.inputSize = inputSize
            classifier.imgData =
                ByteBuffer.allocateDirect(1 * classifier.inputSize * classifier.inputSize * 3 * numBytesPerChannel)
            classifier.imgData.order(ByteOrder.nativeOrder())
            classifier.intValues = IntArray(classifier.inputSize * classifier.inputSize)
            classifier.outputBox =
                (((inputSize / 32).toDouble().pow(2.0) + (inputSize / 16).toDouble()
                    .pow(2.0) + (inputSize / 8).toDouble().pow(2.0)) * 3).toInt()
            val shape = classifier.interpreter.getOutputTensor(0).shape()
            val numClass = shape[shape.size - 1] - 5
            classifier.numClass = numClass
            classifier.outData =
                ByteBuffer.allocateDirect(classifier.outputBox * (numClass + 5) * numBytesPerChannel)
            classifier.outData.order(ByteOrder.nativeOrder())
            return classifier
        }
    }
}