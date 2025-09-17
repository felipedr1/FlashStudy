package com.FlashStudy.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.FlashStudy.ui.screens.CardsScreen
import com.FlashStudy.ui.screens.EditBaralhoScreen
import com.FlashStudy.ui.screens.HomeScreen
import com.FlashStudy.ui.screens.LocationScreen
import com.FlashStudy.ui.viewmodel.BaralhoViewModel
import com.FlashStudy.ui.viewmodel.CardsViewModel
import com.FlashStudy.ui.viewmodel.EditBaralhoViewModel
import com.FlashStudy.ui.viewmodel.LocationViewModel
import com.FlashStudy.ui.viewmodel.UsuarioViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    baralhoViewModel: BaralhoViewModel,
    locationViewModel: LocationViewModel,
    editBaralhoViewModel: EditBaralhoViewModel,
    usuarioViewModel: UsuarioViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = baralhoViewModel,
                onBaralhoClick = { baralhoId ->
                    navController.navigate(Screen.CardsScreen.createRoute(baralhoId))
                },
                onEditClick = { baralhoId ->
                    navController.navigate(Screen.EditBaralhoScreen.createRoute(baralhoId))
                },
                onLocationClick = {
                    navController.navigate(Screen.LocationScreen.route)
                }
            )
        }

        composable(
            route = Screen.CardsScreen.route,
            arguments = listOf(
                navArgument("baralhoId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            // agora pegamos a String
            val baralhoId = backStackEntry.arguments?.getString("baralhoId")
                ?: error("baralhoId não encontrado na rota")

            val cardsViewModel: CardsViewModel = viewModel()
            CardsScreen(
                baralhoId = baralhoId,
                navController = navController,
                baralhoViewModel = baralhoViewModel,
                cardsViewModel = cardsViewModel
            )
        }

        composable(
            route = Screen.EditBaralhoScreen.route,
            arguments = listOf(
                navArgument("baralhoId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val baralhoId = backStackEntry.arguments?.getString("baralhoId")
                ?: error("baralhoId não encontrado na rota")
            EditBaralhoScreen(
                baralhoId = baralhoId,
                navController = navController,
                baralhoViewModel = baralhoViewModel,
                editViewModel = editBaralhoViewModel,
                locationViewModel = locationViewModel,
            )
        }

        composable(route = Screen.LocationScreen.route) {
            LocationScreen(
                navController = navController,
                viewModel = locationViewModel
            )
        }
    }
}