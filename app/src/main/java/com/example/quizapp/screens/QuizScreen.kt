package com.example.quizapp.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.quizapp.Question
import com.example.quizapp.navigation.Screen
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(navController: NavController) {
    var questions by remember { mutableStateOf<List<Question>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var selectedOption by remember { mutableStateOf("") }
    val context = LocalContext.current

    // ADIÇÃO: Estado para mensagem de erro
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(key1 = Unit) {
        isLoading = true // Garante que isLoading seja true no início da busca
        errorMessage = null // Limpa erros anteriores
        FirebaseFirestore.getInstance().collection("quizzes").document("geral").collection("questions")
            .get()
            .addOnSuccessListener { result ->
                questions = result.map { it.toObject(Question::class.java) }.shuffled()
                isLoading = false
            }
            .addOnFailureListener { exception -> // Captura a exceção
                // ALTERAÇÃO: Define a mensagem de erro
                errorMessage = "Falha ao carregar perguntas: ${exception.message}"
                isLoading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (questions.isNotEmpty() && !isLoading) { // Condição melhorada
                        Text("Pergunta ${currentQuestionIndex + 1} de ${questions.size}")
                    } else if (isLoading) {
                        Text("Carregando perguntas...")
                    } else {
                        Text("Quiz") // Título para erro ou lista vazia
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator()
                // ALTERAÇÃO: Exibe a mensagem de erro se houver
            } else if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            } else if (questions.isNotEmpty()) {
                val currentQuestion = questions[currentQuestionIndex]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = currentQuestion.questionText,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    currentQuestion.options.forEach { option ->
                        OptionItem(
                            text = option,
                            isSelected = selectedOption == option,
                            onOptionSelected = { selectedOption = option }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
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
                                Toast.makeText(context, "Por favor, selecione uma opção", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(if (currentQuestionIndex < questions.size - 1) "Próxima" else "Finalizar", fontSize = 18.sp)
                    }
                }
            } else {
                // ALTERAÇÃO: Mensagem mais específica se não houver erro mas a lista estiver vazia
                Text(
                    "Nenhuma pergunta encontrada para este quiz. Tente novamente mais tarde.",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun OptionItem(text: String, isSelected: Boolean, onOptionSelected: () -> Unit) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { onOptionSelected() }
            .padding(16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(text = text, color = contentColor, fontSize = 16.sp)
    }
}
