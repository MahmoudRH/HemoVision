package com.mahmoudhabib.cbctest.data.tflite

import android.content.ContentResolver
import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.mahmoudhabib.cbctest.domain.model.Recognition
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

object Utils {

    @Throws(IOException::class)
    fun loadModelFile(assets: AssetManager, modelFilename: String): MappedByteBuffer {
        val fileDescriptor = assets.openFd(modelFilename)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun getTransformationMatrix(
        srcWidth: Int,
        srcHeight: Int,
        dstWidth: Int,
        dstHeight: Int,
    ): Matrix {
        val matrix = Matrix()
        // Apply scaling if necessary.
        if (srcWidth != dstWidth || srcHeight != dstHeight) {
            val scaleFactorX = dstWidth / srcWidth.toFloat()
            val scaleFactorY = dstHeight / srcHeight.toFloat()
            // Scale exactly to fill dst from src.
            matrix.postScale(scaleFactorX, scaleFactorY)
        }
        return matrix
    }

    fun processBitmap(source: Bitmap, size: Int): Bitmap {
        val imageHeight = source.height
        val imageWidth = source.width
        val croppedBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val frameToCropTransformations =
            getTransformationMatrix(imageWidth, imageHeight, size, size)
        val cropToFrameTransformations = Matrix()
        frameToCropTransformations.invert(cropToFrameTransformations)
        val canvas = Canvas(croppedBitmap)
        canvas.drawBitmap(source, frameToCropTransformations, null)
        return croppedBitmap
    }

    fun unProcessBitmap(croppedBitmap: Bitmap, originalWidth: Int, originalHeight: Int): Bitmap {
        val size = croppedBitmap.width
        val outputBitmap =
            Bitmap.createBitmap(originalWidth, originalHeight, Bitmap.Config.ARGB_8888)
        val cropToFrameTransformations =
            getTransformationMatrix(size, size, originalWidth, originalHeight)
        val canvas = Canvas(outputBitmap)
        canvas.drawBitmap(croppedBitmap, cropToFrameTransformations, null)
        return outputBitmap
    }

    fun saveBitmapToInternalStorage(
        context: Context,
        bitmap: Bitmap,
        fileName: String,
        subdirectoryName: String,
    ): Uri {
        val directory = File(context.filesDir, subdirectoryName)
        if (!directory.exists()) {
            directory.mkdir()
        }
        val file = File(directory, fileName)
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
        return Uri.fromFile(file)
    }

    fun drawTheDetectedObjects(bitmap: Bitmap, results: List<Recognition>): Bitmap {
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        val textPaint = Paint().apply {
            style = Paint.Style.FILL
            textSize = 15f
        }

        results.forEach {
            val color: Int = when (it.title) {
                "RBC" -> Color.RED
                "WBC" -> Color.GREEN
                "Platelets" -> Color.BLUE
                else -> Color.BLACK
            }
            textPaint.color = color
            paint.color = color
            canvas.drawText(it.title, it.location.centerX(), it.location.centerY(), textPaint)
            canvas.drawOval(it.location, paint)
        }
        return bitmap
    }

    fun getBitmapFromUri(uri: Uri?, contentResolver: ContentResolver): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            uri?.let {
                bitmap = if (Build.VERSION.SDK_INT < 28) {
                    MediaStore.Images.Media.getBitmap(contentResolver, it)
                } else {
                    val src = ImageDecoder.createSource(contentResolver, it)
                    ImageDecoder.decodeBitmap(src) { decoder, _, _ ->
                        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                        decoder.isMutableRequired = true
                    }
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bitmap
    }
}