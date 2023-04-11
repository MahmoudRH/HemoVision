package com.mahmoudhabib.cbctest.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.mahmoudhabib.cbctest.presentation.navigation.HomeScreenDestination.Companion.HomeGraphRoute
import com.mahmoudhabib.cbctest.presentation.navigation.homeGraph
import com.mahmoudhabib.cbctest.presentation.theme.CBCTestTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CBCTestTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = HomeGraphRoute) {
                        homeGraph(navController)
                }
            }
        }
    }

}

