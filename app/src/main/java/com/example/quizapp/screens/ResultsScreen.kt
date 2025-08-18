package com.example.quizapp.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope // Necessário para coroutines
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext // Necessário para acessar Application
import androidx.compose.ui.text.font.FontWeight
// import androidx.compose.ui.text.style.TextAlign // Não usado diretamente, pode remover se não houver outros usos
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.quizapp.QuizApplication // Importa a classe Application
// import com.example.quizapp.UserScore // Não será mais criado diretamente aqui se a lógica do ranking for para o repo
import com.example.quizapp.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
// import com.google.firebase.firestore.FirebaseFirestore // Será usado dentro dos repositórios
import kotlinx.coroutines.launch // Para lançar coroutines

@Composable
fun ResultsScreen(navController: NavController, score: Int, totalQuestions: Int) {
    val auth = FirebaseAuth.getInstance()
    val coroutineScope = rememberCoroutineScope() // Para operações assíncronas

    // Obter instâncias dos repositórios
    val application = LocalContext.current.applicationContext as QuizApplication
    val quizHistoryRepository = remember { application.quizHistoryRepository }
    val userRepository = remember { application.userRepository } // Para a lógica de ranking

    LaunchedEffect(key1 = Unit) { // Executa uma vez quando a tela é composta
        val userId = auth.currentUser?.uid
        if (userId != null) {
            // 1. Salvar a tentativa do quiz usando o repositório
            // Esta função já salva no Room e depois tenta sincronizar com o Firebase
            coroutineScope.launch {
                try {
                    val localAttemptId = quizHistoryRepository.saveQuizAttempt(userId, score, totalQuestions)
                    Log.d("ResultsScreen", "Tentativa salva localmente com ID: $localAttemptId")
                    // A sincronização com o Firebase é tratada dentro do saveQuizAttempt
                } catch (e: Exception) {
                    Log.e("ResultsScreen", "Falha ao salvar tentativa de quiz: ${e.message}", e)
                    // Considerar mostrar uma mensagem ao usuário ou logar o erro
                }
            }

            // 2. Atualizar o ranking do usuário usando o repositório
            // Esta é uma função hipotética que você precisaria adicionar ao seu UserRepository
            // ou a um RankingRepository dedicado.
            coroutineScope.launch {
                try {
                    // Exemplo de chamada a uma função no UserRepository para atualizar o ranking
                    userRepository.updateUserRanking(userId, score)
                    Log.d("ResultsScreen", "Ranking do usuário $userId atualizado com score: $score")
                } catch (e: Exception) {
                    Log.e("ResultsScreen", "Falha ao atualizar ranking: ${e.message}", e)
                    // Lidar com falha na atualização do ranking
                }
            }
        } else {
            Log.w("ResultsScreen", "UserID nulo, não foi possível salvar resultados ou atualizar ranking.")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Parabéns!",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Você acertou",
            fontSize = 20.sp
        )
        Text(
            text = "$score de $totalQuestions",
            fontSize = 48.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "perguntas!",
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = {
                navController.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.Results.route) { inclusive = true } // PopUpTo ResultsScreen
                    popUpTo(Screen.Quiz.route) { inclusive = true }   // E QuizScreen
                    launchSingleTop = true // Para garantir que o Dashboard não seja empilhado múltiplas vezes
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Voltar ao Início", fontSize = 18.sp)
        }
    }
}


