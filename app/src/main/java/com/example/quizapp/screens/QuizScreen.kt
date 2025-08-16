package com.example.quizapp.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.quizapp.Question
import com.example.quizapp.navigation.Screen
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun QuizScreen(navController: NavController) {
    var questions by remember { mutableStateOf<List<Question>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var selectedOption by remember { mutableStateOf("") }
    val context = LocalContext.current

    LaunchedEffect(key1 = Unit) {
        FirebaseFirestore.getInstance().collection("quizzes").document("geral").collection("questions")
            .get()
            .addOnSuccessListener { result ->
                questions = result.map { it.toObject(Question::class.java) }.shuffled()
                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (questions.isNotEmpty()) {
        val currentQuestion = questions[currentQuestionIndex]

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(text = currentQuestion.questionText, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(16.dp))

            currentQuestion.options.forEach { option ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = (option == selectedOption),
                            onClick = { selectedOption = option }
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (option == selectedOption),
                        onClick = { selectedOption = option }
                    )
                    Text(text = option, modifier = Modifier.padding(start = 16.dp))
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    if (selectedOption.isNotEmpty()) {
                        if (selectedOption == currentQuestion.correctAnswer) {
                            score++
                        }
                        if (currentQuestionIndex < questions.size - 1) {
                            currentQuestionIndex++
                            selectedOption = ""
                        } else {
                            navController.navigate("${Screen.Results.route}/$score/${questions.size}") {
                                popUpTo(Screen.Dashboard.route)
                            }
                        }
                    } else {
                        Toast.makeText(context, "Please select an answer", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(if (currentQuestionIndex < questions.size - 1) "Próxima" else "Finalizar")
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Falha ao carregar perguntas. Tente novamente.")
        }
    }
}