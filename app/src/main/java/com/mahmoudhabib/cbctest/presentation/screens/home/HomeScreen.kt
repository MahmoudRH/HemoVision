package com.mahmoudhabib.cbctest.presentation.screens.home

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mahmoudhabib.cbctest.presentation.common.HistoryItem
import com.mahmoudhabib.cbctest.presentation.common.EmptyListScreen
import com.mahmoudhabib.cbctest.presentation.common.LoadingScreen
import com.mahmoudhabib.cbctest.presentation.theme.quicksandFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    navigateToAddNewTestScreen: () -> Unit,
    navigateToSearchScreen: () -> Unit,
    navigateToTestDetails: (testId: Int) -> Unit,
) {
    val homeState = viewModel.homeState.value
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Welcome To CBC",
                        style = TextStyle(
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = quicksandFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 24.sp
                        ),
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp),
                ),
                actions = {
                    IconButton(onClick = navigateToSearchScreen) {
                        Icon(Icons.Default.Search, contentDescription = null)
                    }
                }
            )

        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = navigateToAddNewTestScreen,
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "New Test")
            }
        }
    ) { paddingValues ->
        LazyColumn(contentPadding = paddingValues) {
            item {
                Text(
                    text = "History",
                    style = TextStyle(
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = quicksandFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 28.sp
                    ),
                    modifier = Modifier.padding(top = 24.dp, start = 16.dp)
                )
            }

            items(homeState.testResults) {
                HistoryItem(it) {
                    navigateToTestDetails(it.id)
                }
            }
        }
        EmptyListScreen(
            visibility = homeState.isListEmpty,
            text = "Couldn't find items in history..\n Add Some!"
        )
        LoadingScreen(visibility = homeState.isListLoading)
    }
}