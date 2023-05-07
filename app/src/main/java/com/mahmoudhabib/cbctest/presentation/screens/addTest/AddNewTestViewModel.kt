package com.mahmoudhabib.cbctest.presentation.screens.addTest

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mahmoudhabib.cbctest.data.tflite.Utils
import com.mahmoudhabib.cbctest.data.tflite.YoloV5Classifier
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
    private val imageSize = 416

    fun onEvent(event: AddTestEvent) {
        when (event) {
            is AddTestEvent.SetImageUri -> {
                val bitmap = Utils.getBitmapFromUri(event.uri, application.contentResolver)
                _addTestState.value = addTestState.value.copy(
                    selectedBitmap = bitmap,
                    showImagePreview = event.uri != null
                )

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
                                val resultBitmap =
                                    Utils.drawTheDetectedObjects(processedBitmap, modelResults)

                                //-> Step 4: Save the original image and the result image locally
                                val currentTimeInMillis = System.currentTimeMillis()
                                val saveDateFormatted =
                                    SimpleDateFormat(
                                        "dd-MMM-yyyy_HHmmss",
                                        Locale.getDefault()
                                    ).format(
                                        currentTimeInMillis
                                    )
                                val originalImagePath = Utils.saveBitmapToInternalStorage(
                                    context = application.applicationContext,
                                    bitmap = selectedBitmap,
                                    fileName = "originalImage.jpg",
                                    subdirectoryName = "${title}_$saveDateFormatted"
                                )
                                val resultImagePath = Utils.saveBitmapToInternalStorage(
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
}