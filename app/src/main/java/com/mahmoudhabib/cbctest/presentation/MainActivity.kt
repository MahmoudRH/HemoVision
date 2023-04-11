package com.mahmoudhabib.cbctest.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.mahmoudhabib.cbctest.presentation.navigation.HomeScreenDestination
import com.mahmoudhabib.cbctest.presentation.navigation.homeGraph
import com.mahmoudhabib.cbctest.presentation.theme.CBCTestTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()
            if (isDarkTheme.value == null)  isDarkTheme.value = isSystemInDarkTheme()
            CBCTestTheme(isDarkTheme.value!!) {
                NavHost(
                    navController = navController,
                    startDestination = HomeScreenDestination.HomeGraphRoute
                ) {
                    homeGraph(navController)
                }
            }
        }
    }
    companion object{
        val isDarkTheme = mutableStateOf<Boolean?>(null)
    }
}
