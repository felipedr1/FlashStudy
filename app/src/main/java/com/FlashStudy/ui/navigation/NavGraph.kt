package com.FlashStudy.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object CardsScreen : Screen("cards/{baralhoId}") {
        fun createRoute(baralhoId: String): String = "cards/$baralhoId"
    }
    object EditBaralhoScreen : Screen("edit/{baralhoId}") {
        fun createRoute(baralhoId: String): String = "edit/$baralhoId"
    }
    object LocationScreen : Screen("location")
}