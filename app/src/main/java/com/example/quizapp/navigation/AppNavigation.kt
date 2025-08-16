package com.example.quizapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.quizapp.screens.DashboardScreen
import com.example.quizapp.screens.HistoryScreen
import com.example.quizapp.screens.LoginScreen
import com.example.quizapp.screens.QuizScreen
import com.example.quizapp.screens.RankingScreen
import com.example.quizapp.screens.RegisterScreen
import com.example.quizapp.screens.ResultsScreen
import com.example.quizapp.screens.SplashScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    // Define a rota inicial baseada no estado de login do usuário
    val startDestination = if (FirebaseAuth.getInstance().currentUser != null) {
        Screen.Dashboard.route
    } else {
        Screen.Splash.route
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(Screen.Register.route) {
            RegisterScreen(navController = navController)
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController = navController)
        }
        composable(Screen.Quiz.route) {
            QuizScreen(navController = navController)
        }
        composable("${Screen.Results.route}/{score}/{totalQuestions}") { backStackEntry ->
            val score = backStackEntry.arguments?.getString("score")?.toInt() ?: 0
            val totalQuestions = backStackEntry.arguments?.getString("totalQuestions")?.toInt() ?: 0
            ResultsScreen(navController = navController, score = score, totalQuestions = totalQuestions)
        }
        composable(Screen.History.route) {
            HistoryScreen(navController = navController)
        }
        composable(Screen.Ranking.route) {
            RankingScreen(navController = navController)
        }
    }
}