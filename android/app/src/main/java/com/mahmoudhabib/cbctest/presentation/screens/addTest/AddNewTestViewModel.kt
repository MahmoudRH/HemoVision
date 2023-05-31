package com.mahmoudhabib.cbctest.presentation.screens.addTest

import android.app.Application
import android.content.res.AssetManager
import android.graphics.Bitmap
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


    fun onEvent(event: AddTestEvent) {
        when (event) {
            is AddTestEvent.SetImageUri -> {
                event.uri?.let {
                    val bitmap = Utils.getBitmapFromUri(event.uri, application.contentResolver)
                    _addTestState.value = addTestState.value.copy(
                        selectedBitmap = bitmap,
                        showImagePreview = true
                    )
                }
            }

            is AddTestEvent.ChangeTitleText -> {
                val titleRegex = "^[a-zA-Z_\u0621-\u064A0-9 -]{1,50}$".toRegex()
                _addTestState.value = addTestState.value.copy(
                    titleText = event.newText
                )
                if (event.newText.matches(titleRegex)) {
                    _addTestState.value = addTestState.value.copy(
                        isTitleError = false,
                        titleErrorMessage = ""
                    )
                } else {
                    _addTestState.value = addTestState.value.copy(
                        isTitleError = true,
                        titleErrorMessage = "Invalid Title"
                    )
                }

            }

            is AddTestEvent.ProcessImage -> {
                viewModelScope.launch {
                    val selectedBitmap = _addTestState.value.selectedBitmap
                    val title = _addTestState.value.titleText
                    if (title.isNotEmpty()) {
                        _addTestState.value =
                            addTestState.value.copy(isTitleError = false, titleErrorMessage = "")
                        //-> Step 0: Check if the user has provided a bitmap or not.
                        if (selectedBitmap != null) {
                            withContext(Dispatchers.IO) {

                                _addTestState.value =
                                    addTestState.value.copy(isLoading = true)
                                val (cbcBitmapResult, cbcModelRecognitions) = runYoloModel(
                                    CBC_MODEL_PATH,
                                    CBC_MODEL_LABELS_FILE_PATH,
                                    selectedBitmap
                                )

                                val (_, abnormalitiesModelRecognitions) = runYoloModel(
                                    ABNORMALITIES_MODEL_PATH,
                                    ABNORMALITIES_MODEL_LABELS_FILE_PATH,
                                    selectedBitmap
                                )

                                //-> get the title of the most detected abnormality
                                val abnormality =
                                    abnormalitiesModelRecognitions.run {
                                        if (this.isNotEmpty()) {
                                            groupBy { it.title }.maxBy { it.value.size }.key
                                        } else {
                                            ""
                                        }
                                    }


                                //-> Step 4: Save the original image and the result image locally
                                val testDate = System.currentTimeMillis()
                                val (originalImagePath, resultImagePath) = saveTestImages(
                                    testTitle = title,
                                    testDate = testDate,
                                    originalBitmap = selectedBitmap,
                                    resultBitmap = cbcBitmapResult
                                )


                                //-> Step 5: Insert a new record into the database
                                val rowId = useCases.saveTestResult(
                                    TestResult(
                                        title = title,
                                        date = testDate,
                                        redBloodCells = cbcModelRecognitions.count { it.detectedClass == 0 },
                                        whiteBloodCells = cbcModelRecognitions.count { it.detectedClass == 1 },
                                        platelets = cbcModelRecognitions.count { it.detectedClass == 2 },
                                        originalImagePath = originalImagePath,
                                        resultImagePath = resultImagePath,
                                        abnormalities = abnormality
                                    )
                                )

                                _addTestState.value = addTestState.value.copy(
                                    isLoading = false,
                                    rowId = rowId.toInt(),
                                    shouldNavigateToTestDetails = true
                                )
                            }
                        }
                    } else {
                        //show error for title validation
                        _addTestState.value = addTestState.value.copy(
                            isTitleError = true,
                            titleErrorMessage = "Title must not be empty"
                        )
                    }
                }
            }
        }
    }

    private fun saveTestImages(
        testTitle: String,
        testDate: Long,
        originalBitmap: Bitmap,
        resultBitmap: Bitmap,
    ): Pair<String, String> {
        val saveDateFormatted =
            SimpleDateFormat("dd-MMM-yyyy_HHmmss", Locale.getDefault()).format(testDate)
        val originalImagePath = Utils.saveBitmapToInternalStorage(
            context = application.applicationContext,
            bitmap = originalBitmap,
            fileName = "originalImage.jpg",
            subdirectoryName = "${testTitle}_$saveDateFormatted"
        )
        val resultImagePath = Utils.saveBitmapToInternalStorage(
            context = application.applicationContext,
            bitmap = Utils.unProcessBitmap(
                resultBitmap,
                originalBitmap.width,
                originalBitmap.height
            ),
            fileName = "resultImage.jpg",
            subdirectoryName = "${testTitle}_$saveDateFormatted"
        )
        return Pair(originalImagePath.toString(), resultImagePath.toString())
    }

    private fun runYoloModel(
        modelFilePath: String,
        labelsFilePath: String,
        image: Bitmap,
        assetsManager: AssetManager = application.assets,
        imageSize: Int = IMAGE_SIZE,
    ): Pair<Bitmap, List<Recognition>> {
        //-> Step 1: Initialize the classifier
        val classifier = YoloV5Classifier.create(
            assetsManager,
            modelFilePath,
            labelsFilePath,
            imageSize
        )

        //-> Step 2: Process the input bitmap to get it ready for recognition
        val processedBitmap =
            Utils.processBitmap(image, imageSize)

        //-> Step 3: Run the model on the pre-processed image to get results
        val modelResults = classifier.recognizeImage(processedBitmap)
        val bitmapResult = Utils.drawTheDetectedObjects(
            processedBitmap,
            modelResults
        )

        return bitmapResult to modelResults
    }


    private companion object {
        const val CBC_MODEL_PATH = "cbc-model.tflite"
        const val CBC_MODEL_LABELS_FILE_PATH = "cbc-labels.txt"
        const val ABNORMALITIES_MODEL_PATH = "abnormalities-model.tflite"
        const val ABNORMALITIES_MODEL_LABELS_FILE_PATH = "abnormalities-labels.txt"
        const val IMAGE_SIZE = 416
    }

    private fun <T> calculateExecutionTime(function: () -> T): Pair<T, Long> {
        val startTime = System.currentTimeMillis()
        val result = function()
        val endTime = System.currentTimeMillis()
        val executionTime = endTime - startTime
        return result to executionTime
    }
}