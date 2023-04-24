package com.mahmoudhabib.cbctest.presentation.screens.addTest

import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.mahmoudhabib.cbctest.presentation.theme.quicksandFamily


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewTestScreen(viewModel: AddNewTestViewModel = hiltViewModel(), navigateBack: () -> Unit) {

    val screenState = viewModel.addTestState.value
    val context = LocalContext.current

    val pickMedia =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia()) { uri ->
            viewModel.onEvent(AddTestEvent.SetImageUri(uri))
        }
    LazyColumn(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        item {
            TopAppBar(navigationIcon = {
                IconButton(onClick = navigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack, contentDescription = "back"
                    )
                }

            }, title = {
                Text(
                    text = "Make a new test", style = TextStyle(
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = quicksandFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 22.sp
                    )
                )
            }, colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp),
            )
            )
        }
        item {
            OutlinedTextField(
                value = screenState.titleText,
                onValueChange = { viewModel.onEvent(AddTestEvent.ChangeTitleText(it)) },
                label = { Text(text = "Title") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 24.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
            )
        }
        item {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                onClick = {
                    pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "back"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Select Image", fontFamily = quicksandFamily, fontSize = 16.sp)
            }
        }

        item {
            AnimatedVisibility(visible = screenState.showImagePreview) {
                Column {
                    Image(
                        painter = rememberAsyncImagePainter(
                            ImageRequest
                                .Builder(LocalContext.current)
                                .data(data = screenState.selectedImageUri)
                                .build()
                        ),
                        contentDescription = "Preview",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                0.65.dp,
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                        contentScale = ContentScale.Fit
                    )

                    Button(
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.End),
                        onClick = {
                            try {
                                screenState.selectedImageUri?.let {
                                    val contentResolver = context.contentResolver
                                    val bitmap = if (Build.VERSION.SDK_INT < 28) {
                                        MediaStore.Images.Media.getBitmap(contentResolver, it)
                                    } else {
                                        val src = ImageDecoder.createSource(contentResolver, it)
                                        ImageDecoder.decodeBitmap(src) { decoder, info, source ->
                                            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                                            decoder.isMutableRequired = true
                                        }
                                    }
                                    viewModel.onEvent(AddTestEvent.ProcessImage(bitmap))
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        },
                    ) {
                        Text(text = "Go", fontFamily = quicksandFamily, fontSize = 16.sp)
                    }
                }
            }
        }

    }


}