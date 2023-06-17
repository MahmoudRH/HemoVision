package com.mahmoudhabib.cbctest.presentation.common

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.mahmoudhabib.cbctest.presentation.theme.quicksandFamily

@Composable
fun ConfirmationBox(
    state: State<Boolean>,
    title: String = "Warning",
    text: String = "",
    confirmButtonText: String = "Confirm",
    dismissButtonText: String = "Cancel",
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    if (state.value) {
        AlertDialog(
            onDismissRequest = {
                onDismiss()
            },
            title = {
                Text(text = title, fontFamily = quicksandFamily, fontWeight = FontWeight.SemiBold)
            },
            text = {
                Text(text = text, fontFamily = quicksandFamily)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onConfirm()
                    }
                ) {
                    Text(
                        confirmButtonText,
                        fontFamily = quicksandFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onDismiss()
                    }
                ) {
                    Text(
                        dismissButtonText,
                        fontFamily = quicksandFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        )
    }
}