package com.mahmoudhabib.cbctest.presentation.screens.testDetails

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.capitalize
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
    LaunchedEffect(Unit) {
        viewModel.onEvent(TestDetailsEvent.LoadTestDetails)
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)) {
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
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp)

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
                Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    text = it.title.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                    fontFamily = quicksandFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                )
                Divider(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                , color = MaterialTheme.colorScheme.primary.copy(0.25f))

                TestDetail(title = "Red Blood Cells Count: ", value = it.redBloodCells.format())
                TestDetail(title = "White Blood Cells Count: ", value = it.whiteBloodCells.format())
                TestDetail(title = "Platelets Count: ", value = it.platelets.format())

                Divider(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 8.dp)
                    , color = MaterialTheme.colorScheme.primary.copy(0.25f))

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
    style: TextStyle = TextStyle(
        fontFamily = quicksandFamily,
        color = MaterialTheme.colorScheme.onBackground,
        fontSize = 16.sp
    ),
) {

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .border(
                0.65.dp,
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
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
    style: TextStyle = TextStyle(
        fontFamily = quicksandFamily,
        color = MaterialTheme.colorScheme.onBackground,
        fontSize = 16.sp
    ),
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