package com.soraiyu.foxappmemo.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.soraiyu.foxappmemo.ui.screen.addedit.AddEditScreen
import com.soraiyu.foxappmemo.ui.screen.main.MainScreen

private const val ROUTE_MAIN = "main"
private const val ROUTE_ADD_EDIT = "add_edit"
private const val ARG_PACKAGE_NAME = "packageName"

@Composable
fun FoxAppMemoNavGraph(sharedText: String? = null) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = ROUTE_MAIN) {
        composable(ROUTE_MAIN) {
            MainScreen(
                onNavigateToAddEdit = { packageName ->
                    if (packageName != null) {
                        navController.navigate("$ROUTE_ADD_EDIT?$ARG_PACKAGE_NAME=$packageName")
                    } else {
                        navController.navigate(ROUTE_ADD_EDIT)
                    }
                },
            )
        }
        composable(
            route = "$ROUTE_ADD_EDIT?$ARG_PACKAGE_NAME={$ARG_PACKAGE_NAME}",
            arguments = listOf(
                navArgument(ARG_PACKAGE_NAME) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) { backStackEntry ->
            val packageName = backStackEntry.arguments?.getString(ARG_PACKAGE_NAME)
            AddEditScreen(
                onNavigateBack = { navController.popBackStack() },
                sharedText = if (packageName == null) sharedText else null,
            )
        }
    }
}
