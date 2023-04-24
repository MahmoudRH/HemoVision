package com.mahmoudhabib.cbctest.presentation.screens.addTest

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mahmoudhabib.cbctest.domain.usecases.TestResultsUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Inject


@HiltViewModel
class AddNewTestViewModel @Inject constructor(
    private val application: Application,
    private val useCases: TestResultsUseCases,
) :
    ViewModel() {

    private val _addTestState = mutableStateOf<AddTestState>(AddTestState())
    val addTestState: State<AddTestState> = _addTestState
    val TAG = "Mah "

    //    private val imageSize = 640
    private val imageSize = 640
    fun onEvent(event: AddTestEvent) {
        when (event) {
            is AddTestEvent.SetImageUri -> {
                _addTestState.value = addTestState.value.copy(
                    selectedImageUri = event.uri,
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
                    withContext(Dispatchers.IO) {
                        val bufferSize = 7 * 8400 * java.lang.Float.SIZE / java.lang.Byte.SIZE
                        val modelOutput =
                            ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder())
                        val interpreter = Interpreter(loadModelFile(), Interpreter.Options())
                        interpreter.run(getInputBuffer(event.bitmap), modelOutput)

                        modelOutput.rewind()
                        val probabilities = modelOutput.asFloatBuffer()

                        for ((count, i) in (0 until probabilities.capacity() step 6).withIndex()) {
                            val probability =
                                "[${probabilities.get(i)}, ${probabilities.get(i + 1)}, ${
                                    probabilities.get(i + 2)
                                }, ${probabilities.get(i + 3)}, ${probabilities.get(i + 4)}, ${
                                    probabilities.get(
                                        i + 5
                                    )
                                }]"
                            Log.e(TAG, "line[$count]: $probability")
                        }

                    }
                }
            }
        }
    }

    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = application.assets.openFd("model32.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun getInputBuffer(inputBitmap: Bitmap): ByteBuffer {
        val bitmap = Bitmap.createScaledBitmap(inputBitmap, imageSize, imageSize, true)
        val input =
            ByteBuffer.allocateDirect(imageSize * imageSize * 3 * 4)
                .order(ByteOrder.nativeOrder())
        for (y in 0 until imageSize) {
            for (x in 0 until imageSize) {
                val px = bitmap.getPixel(x, y)

                // Get channel values from the pixel value.
                val r = Color.red(px)
                val g = Color.green(px)
                val b = Color.blue(px)

                // Normalize channel values to [-1.0, 1.0]. This requirement depends on the model.
                // For example, some models might require values to be normalized to the range
                // [0.0, 1.0] instead.
                val rf = (r - 127) / 255f
                val gf = (g - 127) / 255f
                val bf = (b - 127) / 255f

                input.putFloat(rf)
                input.putFloat(gf)
                input.putFloat(bf)
            }
        }
        return input
    }
}
