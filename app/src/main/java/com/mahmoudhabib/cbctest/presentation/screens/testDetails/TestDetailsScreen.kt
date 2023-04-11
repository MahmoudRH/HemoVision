package com.mahmoudhabib.cbctest.presentation.screens.testDetails

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.mahmoudhabib.cbctest.presentation.common.ConfirmationBox
import com.mahmoudhabib.cbctest.presentation.theme.quicksandFamily
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestDetailsScreen(
    viewModel: TestDetailsViewModel = hiltViewModel(),
    navigateBack: () -> Unit,
) {

    val testDetails = viewModel.testDetails.value
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.onEvent(TestDetailsEvent.LoadTestDetails)
    }

    Column {
        TopAppBar(
            title = { Text(text = "Test Details", fontFamily = quicksandFamily) },
            navigationIcon = {
                IconButton(onClick = navigateBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = null
                    )
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                    .compositeOver(MaterialTheme.colorScheme.background)
            ), actions = {
                Row(
                    modifier = Modifier
                        .padding(end = 6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .clickable {
                            viewModel.onEvent(TestDetailsEvent.ShowDeleteConfirmationDialog)
                        }
                        .padding(vertical = 5.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(text = "Delete", fontFamily = quicksandFamily)
                }
            }
        )

        testDetails?.let {
            Column(
                Modifier
                    .padding(horizontal = 16.dp)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    text = it.title,
                    fontFamily = quicksandFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 22.sp
                )
                Divider(Modifier.fillMaxWidth().padding(vertical = 12.dp))

                TestDetail(title = "Red Blood Cells Count: ", value = it.redBloodCells.format())
                TestDetail(title = "White Blood Cells Count: ", value = it.whiteBloodCells.format())
                TestDetail(title = "Platelets Count: ", value = it.platelets.format())

                Divider(Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp))

                DateDetail(title = "Day: ", value = it.date.format("EEEE"))
                DateDetail(title = "Date: ", value = it.date.format("dd-MM-yyyy"))
                DateDetail(title = "Time: ", value = it.date.format("hh:mm:ss aa"))

            }
        }
        ConfirmationBox(
            state = viewModel.alertDialogState,
            text = "\"${testDetails?.title}\" will be permanently deleted!",
            onConfirm = {
                viewModel.onEvent(TestDetailsEvent.DeleteTestDetails)
                if (viewModel.isDeleteSuccess.value) navigateBack()
            },
            onDismiss = { viewModel.onEvent(TestDetailsEvent.DismissDeletion) })
    }
}

@Composable
private fun TestDetail(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    style: TextStyle = TextStyle(fontFamily = quicksandFamily),
) {

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .border(
                0.5.dp,
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.tertiary
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = title, style = style)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = value, style = style)
    }

}

@Composable
private fun DateDetail(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    style: TextStyle = TextStyle(fontFamily = quicksandFamily),
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = style
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            modifier = Modifier.weight(2f),
            text = value,
            style = style
        )
    }
}

private fun Float.format(fractions: Int = 2): String {
    return DecimalFormat().apply { maximumFractionDigits = fractions }.format(this)
}

private fun Long.format(pattern: String): String {
    return SimpleDateFormat(pattern, Locale.getDefault()).format(this)
}