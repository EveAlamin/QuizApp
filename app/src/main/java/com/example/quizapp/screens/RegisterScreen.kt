package com.example.quizapp.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.quizapp.QuizApplication // Importa a classe Application
import com.example.quizapp.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
// import com.google.firebase.firestore.FirebaseFirestore // Não é mais necessário aqui diretamente
import kotlinx.coroutines.launch // Para lançar coroutines
import kotlinx.coroutines.withContext // Para mudar o contexto da coroutine
import kotlinx.coroutines.Dispatchers // Para especificar o contexto Main para UI

@Composable
fun RegisterScreen(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance() // Ainda necessário para a autenticação

    // Obter instância do repositório da classe Application
    val application = LocalContext.current.applicationContext as QuizApplication
    val userRepository = remember { application.userRepository }

    // Obter um CoroutineScope para lançar operações assíncronas
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Crie sua conta",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "É rápido e fácil",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(48.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Senha") },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    val description = if (passwordVisible) "Ocultar senha" else "Mostrar senha"
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, description)
                    }
                }
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    if (name.isNotBlank() && email.isNotBlank() && password.isNotBlank()) {
                        isLoading = true
                        // 1. Cria o usuário no Firebase Authentication
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val firebaseUser = auth.currentUser
                                    firebaseUser?.let { fbUser ->
                                        // 2. Se a autenticação for bem-sucedida,
                                        // usa o repositório para salvar os dados do usuário
                                        // (no Firebase Firestore E no Room localmente)
                                        coroutineScope.launch { // Operação de IO, lançar em coroutine
                                            try {
                                                userRepository.saveUserToFirebaseAndCache(
                                                    userId = fbUser.uid,
                                                    name = name.trim(),
                                                    email = email.trim()
                                                )
                                                // 3. Após salvar, navega para o Dashboard no contexto principal
                                                withContext(Dispatchers.Main) {
                                                    isLoading = false
                                                    navController.navigate(Screen.Dashboard.route) {
                                                        popUpTo(Screen.Register.route) { inclusive = true }
                                                        popUpTo(Screen.Login.route) { inclusive = true }
                                                    }
                                                }
                                            } catch (e: Exception) {
                                                // Lidar com possíveis erros ao salvar no repositório
                                                withContext(Dispatchers.Main) {
                                                    isLoading = false
                                                    Toast.makeText(context, "Falha ao salvar dados do usuário: ${e.message}", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        }
                                    } ?: run {
                                        // Caso raro: task.isSuccessful mas auth.currentUser é null
                                        isLoading = false
                                        Toast.makeText(context, "Falha ao obter usuário após registro.", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    // Falha na autenticação do Firebase
                                    isLoading = false
                                    val errorMessage = task.exception?.message ?: "Falha no registro."
                                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                                }
                            }
                    } else {
                        Toast.makeText(context, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading // Desabilita o botão enquanto estiver carregando
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Registrar", fontSize = 18.sp)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Já tem uma conta? Faça login",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable {
                    if (!isLoading) { // Evita navegação enquanto uma operação está em progresso
                        navController.popBackStack()
                    }
                }
            )
        }
    }
}

