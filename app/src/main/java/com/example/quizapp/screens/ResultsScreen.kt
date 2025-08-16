package com.example.quizapp.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.quizapp.UserScore
import com.example.quizapp.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ResultsScreen(navController: NavController, score: Int, totalQuestions: Int) {

    LaunchedEffect(key1 = Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val db = FirebaseFirestore.getInstance()
            // Salva no histórico
            val quizAttempt = hashMapOf(
                "userId" to userId,
                "score" to score,
                "totalQuestions" to totalQuestions,
                "timestamp" to System.currentTimeMillis()
            )
            db.collection("history").add(quizAttempt)

            // Atualiza o ranking
            val rankingRef = db.collection("ranking").document(userId)
            val userRef = db.collection("users").document(userId)

            db.runTransaction { transaction ->
                val userSnapshot = transaction.get(userRef)
                val userName = userSnapshot.getString("name") ?: "Usuário Anónimo"
                val rankingSnapshot = transaction.get(rankingRef)

                if (rankingSnapshot.exists()) {
                    val newTotalScore = (rankingSnapshot.getLong("totalScore") ?: 0L) + score
                    transaction.update(rankingRef, "totalScore", newTotalScore)
                } else {
                    val newUserScore = UserScore(userId, userName, score.toLong())
                    transaction.set(rankingRef, newUserScore)
                }
                null
            }.addOnFailureListener { Log.e("Firestore", "Transaction failed: $it") }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Você acertou $score de $totalQuestions perguntas!", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = {
            navController.navigate(Screen.Dashboard.route) {
                popUpTo(Screen.Dashboard.route) { inclusive = true }
            }
        }) {
            Text("Voltar ao Início")
        }
    }
}