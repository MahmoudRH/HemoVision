package com.mahmoudhabib.cbctest.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.mahmoudhabib.cbctest.R

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)

val quicksandFamily = FontFamily(
    Font(R.font.quicksand_semibold, weight = FontWeight.SemiBold),
    Font(R.font.quicksand_regular, weight = FontWeight.Normal),
    Font(R.font.quicksand_light, weight = FontWeight.Light),
)