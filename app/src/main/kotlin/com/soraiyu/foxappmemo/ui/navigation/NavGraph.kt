package com.soraiyu.foxappmemo.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.soraiyu.foxappmemo.ui.screen.addedit.AddEditScreen
import com.soraiyu.foxappmemo.ui.screen.installedapps.InstalledAppsScreen
import com.soraiyu.foxappmemo.ui.screen.main.MainScreen

private const val ROUTE_MAIN = "main"
private const val ROUTE_ADD_EDIT = "add_edit"
private const val ROUTE_INSTALLED_APPS = "installed_apps"
private const val ARG_PACKAGE_NAME = "packageName"
private const val ARG_APP_NAME = "appName"

@Composable
fun FoxAppMemoNavGraph(sharedText: String? = null) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = ROUTE_MAIN) {
        composable(ROUTE_MAIN) {
            MainScreen(
                onNavigateToAddEdit = { packageName ->
                    if (packageName != null) {
                        val encoded = Uri.encode(packageName)
                        navController.navigate("$ROUTE_ADD_EDIT?$ARG_PACKAGE_NAME=$encoded")
                    } else {
                        navController.navigate(ROUTE_ADD_EDIT)
                    }
                },
                onNavigateToInstalledApps = {
                    navController.navigate(ROUTE_INSTALLED_APPS)
                },
            )
        }

        composable(
            route = "$ROUTE_ADD_EDIT?$ARG_PACKAGE_NAME={$ARG_PACKAGE_NAME}&$ARG_APP_NAME={$ARG_APP_NAME}",
            arguments = listOf(
                navArgument(ARG_PACKAGE_NAME) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument(ARG_APP_NAME) {
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

        composable(ROUTE_INSTALLED_APPS) {
            InstalledAppsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddEdit = { packageName, appName ->
                    val encodedPkg = Uri.encode(packageName)
                    val encodedName = Uri.encode(appName)
                    navController.navigate(
                        "$ROUTE_ADD_EDIT?$ARG_PACKAGE_NAME=$encodedPkg&$ARG_APP_NAME=$encodedName",
                    )
                },
            )
        }
    }
}
