package com.example.quizapp.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
            val quizAttempt = hashMapOf(
                "userId" to userId,
                "score" to score,
                "totalQuestions" to totalQuestions,
                "timestamp" to System.currentTimeMillis()
            )
            db.collection("history").add(quizAttempt)

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
                    popUpTo(Screen.Dashboard.route) { inclusive = true }
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