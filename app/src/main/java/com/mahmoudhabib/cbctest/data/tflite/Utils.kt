package com.mahmoudhabib.cbctest.data.tflite

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import java.io.FileInputStream
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
}