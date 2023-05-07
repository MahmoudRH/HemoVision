package com.mahmoudhabib.cbctest.presentation.screens.search

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mahmoudhabib.cbctest.presentation.common.HistoryItem
import com.mahmoudhabib.cbctest.presentation.common.EmptyListScreen
import com.mahmoudhabib.cbctest.presentation.common.LoadingScreen


@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    navigateBack: () -> Unit,
    navigateToTestDetails: (testId: Int) -> Unit,
) {
    val focusRequester = FocusRequester()

    Column(Modifier.fillMaxSize()) {
        SearchTopBar(
            onNavigateBack = navigateBack,
            hint = "Search..",
            focusRequester = focusRequester,
            searchWord = viewModel.searchWord,
            onSearch = { viewModel.search() }
        )
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            LazyColumn(
                contentPadding = PaddingValues(vertical = 10.dp)
            ) {
                items(viewModel.resultsList, key = { it.id }) {
                    HistoryItem(it) { navigateToTestDetails(it.id) }
                }
            }
            EmptyListScreen(
                visibility = viewModel.isResultsListEmpty.value,
                text = "Sorry, We Couldn't Find Any Results.. ",
                fontSize = 20.sp
            )
            LoadingScreen(visibility = viewModel.isLoading.value)
        }
    }

    DisposableEffect(Unit) {
        focusRequester.requestFocus()
        onDispose { }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTopBar(
    onNavigateBack: () -> Unit = {},
    searchWord: MutableState<String>,
    onSearch: () -> Unit = {},
    hint: String,
    focusRequester: FocusRequester,
) {
    TopAppBar(
        title = {
            SearchTextField(
                searchWord = searchWord,
                hint = hint,
                focusRequester = focusRequester
            ) {
                if (searchWord.value.isNotEmpty())
                    onSearch()
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp),
        ),
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = null)
            }
        },
        actions = {
            AnimatedVisibility(
                searchWord.value.trim().isNotEmpty(),
                enter = fadeIn(), exit = fadeOut()
            ) {
                IconButton(
                    onClick = { searchWord.value = "" }
                ) {
                    Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = "Clear"
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun SearchTextField(
    searchWord: MutableState<String>,
    hint: String,
    focusRequester: FocusRequester,
    onSearch: () -> Unit = {},
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    BasicTextField(
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .onFocusChanged {
                if (it.isFocused)
                    keyboardController?.show()
            },
        value = searchWord.value,
        onValueChange = { searchWord.value = it },
        singleLine = true,
        decorationBox = { innerTextField ->
            AnimatedVisibility(
                searchWord.value.isEmpty(),
                enter = fadeIn(), exit = fadeOut()
            ) {
                Text(text = hint, color = Color.Gray, fontSize = 18.sp)
            }
            innerTextField()
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = {
            keyboardController?.hide()
            onSearch()
        }),
        textStyle = TextStyle(
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 18.sp
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
    )
}