package com.mahmoudhabib.cbctest.presentation

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.itextpdf.io.font.PdfEncodings
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.font.PdfFont
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.borders.SolidBorder
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.HorizontalAlignment
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.itextpdf.layout.properties.VerticalAlignment
import com.mahmoudhabib.cbctest.domain.model.TestResult
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

object PdfUtils {

    private val pageSize = PageSize.LETTER

    fun createReport(context: Context, testResult: TestResult): Uri {
        val pdfDir = File(context.filesDir, "pdfs")
        if (!pdfDir.exists()) {
            pdfDir.mkdirs()
        }
        val pdfFile = File(pdfDir, "${testResult.title.trim().replace(' ', '-')}.pdf")
        val document = setupDocument(pdfFile)
        val fontRegular = regularFont(context)
        val fontBold = boldFont(context)
        val availableHeight = pageSize.height - document.topMargin - document.bottomMargin
        val availableWidth = pageSize.width - document.leftMargin - document.rightMargin

        val pageTitle = setPageTitle(fontBold)
        val detailsTable = setDetailsTable(
            fontRegular,
            testResult.title,
            testResult.redBloodCells,
            testResult.whiteBloodCells,
            testResult.platelets,
            testResult.abnormalities
        )

        val testImages =
            setTestImages(availableHeight, availableWidth, testResult.originalImagePath, testResult.resultImagePath)
        val testDate = setTestDateDetails(fontRegular, testResult.date)

        document.add(pageTitle)
        document.add(detailsTable)
        document.add(testImages)
        document.add(testDate)
        document.close()
        return FileProvider.getUriForFile(
            context,
            "com.mahmoudhabib.cbctest.fileprovider",
            pdfFile
        )

    }

    private fun setTestDateDetails(font: PdfFont, date: Long): Table {
        val table = Table(2).apply {
            width = UnitValue.createPercentValue(100f)
            setMarginTop(30f)
        }
        addTableRow(table, font, "Day", date.format("EEEE"))
        addTableRow(table, font, "Date", date.format("dd-MM-yyyy"))
        addTableRow(table, font, "Time", date.format("hh:mm:ss aa"))

        return table
    }

    private fun setTestImages(
        availableHeight: Float,
        availableWidth: Float,
        originalImagePath: String,
        resultImagePath: String,
    ): Table {
        val imagesTable = Table(2).apply {
            width = UnitValue.createPercentValue(100f)
            setHorizontalAlignment(HorizontalAlignment.CENTER)
            setMarginTop(20f)
        }
        val originalImageCell = imageCell(originalImagePath,availableWidth, availableHeight, "Original Image")
        val resultImageCell = imageCell(resultImagePath,availableWidth, availableHeight, "Result Image")
        imagesTable.addCell(originalImageCell)
        imagesTable.addCell(resultImageCell)
        return imagesTable
    }

    private fun imageCell(imagePath: String, availableWidth: Float, availableHeight: Float, caption: String): Cell {
        return Cell().apply {
            setHorizontalAlignment(HorizontalAlignment.CENTER)
            setVerticalAlignment(VerticalAlignment.MIDDLE)
            setBorder(Border.NO_BORDER)
            add(
                Image(ImageDataFactory.create(imagePath))
                    .scaleToFit(availableWidth * 0.475f, availableHeight * 0.5f)
                    .setBorder(SolidBorder(2f))
            )
            add(
                Paragraph(caption)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(12f)
                    .setMarginTop(5f)
            )
        }
    }

    private fun setDetailsTable(
        font: PdfFont,
        title: String,
        redBloodCells: Int,
        whiteBloodCells: Int,
        platelets: Int,
        abnormalities: String,
    ): Table {
        val table = Table(2).apply {
            width = UnitValue.createPercentValue(100f)
            setMarginTop(10f)
        }
        val titleCell = Cell(1, 2).apply { add(boldParagraph(font, title)) }
        table.addCell(titleCell)
        addTableRow(table, font, "Red Blood Cells", redBloodCells.toString())
        addTableRow(table, font, "White Blood Cells", whiteBloodCells.toString())
        addTableRow(table, font, "Platelets", platelets.toString())
        addTableRow(table, font, "Abnormalities", abnormalities.ifEmpty { "-" })
        return table
    }

    private fun boldParagraph(font: PdfFont, text: String): Paragraph {
        return Paragraph(text)
            .setTextAlignment(TextAlignment.CENTER)
            .setFontSize(14f)
            .setFont(font)
            .setBold()
    }

    private fun regularParagraph(font: PdfFont, text: String): Paragraph {
        return Paragraph(text)
            .setTextAlignment(TextAlignment.CENTER)
            .setFontSize(12f)
            .setFont(font)
    }

    private fun addTableRow(table: Table, font: PdfFont, key: String, value: String) {
        val keyCell = Cell().apply {
            add(boldParagraph(font, key))
        }
        val valueCell = Cell().apply {
            add(regularParagraph(font, value))
        }
        table.addCell(keyCell)
        table.addCell(valueCell)
    }


    private fun regularFont(context: Context): PdfFont {
        val regularFontFile = context.assets.open("fonts/quicksand_regular.ttf")
        return PdfFontFactory.createFont(regularFontFile.readBytes(), PdfEncodings.IDENTITY_H)
    }

    private fun boldFont(context: Context): PdfFont {
        val semiBoldFontFile = context.assets.open("fonts/quicksand_semibold.ttf")
        return PdfFontFactory.createFont(semiBoldFontFile.readBytes(), PdfEncodings.IDENTITY_H)
    }


    private fun setPageTitle(font: PdfFont): Paragraph {
        return Paragraph("Test Details Report").apply {
            setTextAlignment(TextAlignment.CENTER)
            setFontSize(22f)
            setFont(font)
            setFontColor(DeviceRgb(0x10, 0x63, 0x69))
            setBorderBottom(SolidBorder(DeviceRgb(0x10, 0x63, 0x69), 2f))
            setBold()
        }
    }

    private fun setupDocument(file: File): Document {
        val pdfWriter = PdfWriter(file.path)
        val pdfDocument = PdfDocument(pdfWriter).apply { defaultPageSize = pageSize }
        return Document(pdfDocument)
    }

    private fun Long.format(pattern: String): String {
        return SimpleDateFormat(pattern, Locale.getDefault()).format(this)
    }
}