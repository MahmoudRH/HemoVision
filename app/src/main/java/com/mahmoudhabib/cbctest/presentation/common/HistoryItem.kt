package com.mahmoudhabib.cbctest.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mahmoudhabib.cbctest.domain.model.TestResult
import com.mahmoudhabib.cbctest.presentation.theme.quicksandFamily
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryItem(
    item: TestResult = TestResult(title = "Title", date = 1680963409000),
    onClickHistoryItem: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .fillMaxWidth(),
        onClick = onClickHistoryItem,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = item.title,
                style = TextStyle(
                    color = MaterialTheme.colorScheme.tertiary,
                    fontFamily = quicksandFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 22.sp
                )
            )

            Row(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CustomChip("RBC", item.redBloodCells)
                CustomChip("WBC", item.whiteBloodCells)
                CustomChip("PLT", item.platelets)
            }

            Text(
                text = SimpleDateFormat("MMM dd", Locale.getDefault()).format(item.date),
                style = TextStyle(
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.67f),
                    fontFamily = quicksandFamily,
                    fontWeight = FontWeight.Light,
                    fontSize = 18.sp
                ),
                modifier = Modifier.align(Alignment.End)
            )

        }
    }
}

@Composable
private fun CustomChip(key: String, value: Float) {
    Text(
        text = "$key: ${DecimalFormat().apply { maximumFractionDigits = 2 }.format(value)}",
        fontFamily = quicksandFamily,
        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.87f),
        modifier = Modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}