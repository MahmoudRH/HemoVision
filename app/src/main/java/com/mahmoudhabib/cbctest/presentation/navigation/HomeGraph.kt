package com.mahmoudhabib.cbctest.presentation.navigation

import androidx.navigation.*
import androidx.navigation.compose.composable
import com.mahmoudhabib.cbctest.presentation.screens.addTest.AddNewTestScreen
import com.mahmoudhabib.cbctest.presentation.screens.home.HomeScreen
import com.mahmoudhabib.cbctest.presentation.navigation.HomeScreenDestination.Companion.HomeGraphRoute
import com.mahmoudhabib.cbctest.presentation.screens.search.SearchScreen
import com.mahmoudhabib.cbctest.presentation.screens.testDetails.TestDetailsScreen

sealed class HomeScreenDestination(val route: String) {
    object Add : HomeScreenDestination("ADD_NEW_TEST_SCREEN")

    object TestDetails : HomeScreenDestination("TEST_DETAILS/{testID}") {
        fun createRoute(testID: Int): String {
            return "TEST_DETAILS/$testID"
        }
    }

    object ALl : HomeScreenDestination("ALL_SCREEN")

    object Search : HomeScreenDestination("SEARCH_SCREEN")

    companion object {
        const val HomeGraphRoute = "HOME_GRAPH"
    }
}

fun NavGraphBuilder.homeGraph(navController: NavController) {
    navigation(
        startDestination = HomeScreenDestination.ALl.route,
        route = HomeGraphRoute
    ) {
        composable(HomeScreenDestination.ALl.route) {
            HomeScreen(
                navigateToAddNewTestScreen = { navController.navigate(HomeScreenDestination.Add.route) },
                navigateToSearchScreen = { navController.navigate(HomeScreenDestination.Search.route) },
                navigateToTestDetails = { testId -> navController.navigate(HomeScreenDestination.TestDetails.createRoute(testId)) })
        }
        composable(HomeScreenDestination.Add.route) {
            AddNewTestScreen(
                navigateBack = { navController.popBackStack() }
            )
        }
        composable(HomeScreenDestination.Search.route) {
            SearchScreen(
                navigateBack = { navController.popBackStack() },
                navigateToTestDetails = { testId -> navController.navigate(HomeScreenDestination.TestDetails.createRoute(testId)) }
            )
        }
        composable(
            HomeScreenDestination.TestDetails.route,
            arguments = listOf(navArgument("testID") { type = NavType.IntType })
        ) {
            val testID = it.arguments?.getInt("testID")
            testID?.let {
                TestDetailsScreen(
                    navigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}