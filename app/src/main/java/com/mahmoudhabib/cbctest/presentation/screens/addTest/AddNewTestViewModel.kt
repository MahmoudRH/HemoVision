package com.mahmoudhabib.cbctest.presentation.screens.addTest

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mahmoudhabib.cbctest.data.tflite.Utils
import com.mahmoudhabib.cbctest.data.tflite.YoloV5Classifier
import com.mahmoudhabib.cbctest.domain.model.Recognition
import com.mahmoudhabib.cbctest.domain.model.TestResult
import com.mahmoudhabib.cbctest.domain.usecases.TestResultsUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject


@HiltViewModel
class AddNewTestViewModel @Inject constructor(
    private val application: Application,
    private val useCases: TestResultsUseCases,
) :
    ViewModel() {
    private val _addTestState = mutableStateOf<AddTestState>(AddTestState())
    val addTestState: State<AddTestState> = _addTestState
    private val imageSize = 416

    fun onEvent(event: AddTestEvent) {
        when (event) {
            is AddTestEvent.SetImageUri -> {
                val bitmap = getBitmapFromUri(event.uri, application.contentResolver)
                _addTestState.value = addTestState.value.copy(
                    selectedBitmap = bitmap,
                    showImagePreview = event.uri != null
                )

            }

            is AddTestEvent.ChangeTitleText -> {
                _addTestState.value = addTestState.value.copy(
                    titleText = event.newText
                )
            }

            is AddTestEvent.ProcessImage -> {
                viewModelScope.launch {
                    val selectedBitmap = _addTestState.value.selectedBitmap
                    val title = _addTestState.value.titleText
                    //-> Step 0: Check if the user has provided a bitmap or not.
                    if (selectedBitmap != null) {
                        withContext(Dispatchers.IO) {
                            //-> Step 1: Initialize the classifier
                            _addTestState.value = addTestState.value.copy(isLoading = true)
                            val classifier = YoloV5Classifier.create(
                                application.assets,
                                "best4-float16.tflite",
                                "CBC.txt",
                                imageSize
                            )

                            //-> Step 2: Process the input bitmap to get it ready for recognition
                            val processedBitmap =
                                Utils.processBitmap(selectedBitmap, imageSize)

                            //-> Step 3: Run the model on the pre-processed image to get results
                            val modelResults = classifier.recognizeImage(processedBitmap)
                            val resultBitmap = drawTheDetectedObjects(processedBitmap, modelResults)

                            //-> Step 4: Save the original image and the result image locally
                            val currentTimeInMillis = System.currentTimeMillis()
                            val saveDateFormatted =
                                SimpleDateFormat("dd-MMM-yyyy_HHmmss", Locale.getDefault()).format(
                                    currentTimeInMillis
                                )
                            val originalImagePath = saveBitmapToInternalStorage(
                                context = application.applicationContext,
                                bitmap = selectedBitmap,
                                fileName = "originalImage.jpg",
                                subdirectoryName = "${title}_$saveDateFormatted"
                            )
                            val resultImagePath = saveBitmapToInternalStorage(
                                context = application.applicationContext,
                                bitmap = Utils.unProcessBitmap(
                                    resultBitmap,
                                    selectedBitmap.width,
                                    selectedBitmap.height
                                ),
                                fileName = "resultImage.jpg",
                                subdirectoryName = "${title}_$saveDateFormatted"
                            )

                            //-> Step 5: Insert a new record into the database
                            val rowId = useCases.saveTestResult(
                                TestResult(
                                    title = title,
                                    date = currentTimeInMillis,
                                    redBloodCells = modelResults.count { it.detectedClass == 0 },
                                    whiteBloodCells = modelResults.count { it.detectedClass == 1 },
                                    platelets = modelResults.count { it.detectedClass == 2 },
                                    originalImagePath = originalImagePath.toString(),
                                    resultImagePath = resultImagePath.toString(),
                                )
                            )
                            _addTestState.value = addTestState.value.copy(
                                isLoading = false,
                                rowId = rowId.toInt(),
                                shouldNavigateToTestDetails = true
                            )
                        }
                    }
                }
            }
        }
    }

    private fun getBitmapFromUri(uri: Uri?, contentResolver: ContentResolver): Bitmap? {
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

    private fun drawTheDetectedObjects(bitmap: Bitmap, results: List<Recognition>): Bitmap {
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

    private fun saveBitmapToInternalStorage(
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

}

/*    private fun getSampleImage(drawable: Int = R.drawable.blood_sample): Bitmap {
        return BitmapFactory.decodeResource(
            application.resources,
            drawable,
            BitmapFactory.Options().apply {
                inMutable = true
            })
    }*/
