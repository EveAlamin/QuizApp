package com.example.quizapp.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash_screen")
    object Login : Screen("login_screen")
    object Register : Screen("register_screen")
    object Dashboard : Screen("dashboard_screen")
    object Quiz : Screen("quiz_screen")
    object Results : Screen("results_screen")
    object History : Screen("history_screen")
    object Ranking : Screen("ranking_screen")
}