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
import kotlinx.coroutines.launch // Para lançar coroutines
import kotlinx.coroutines.withContext // Para mudar o contexto da coroutine
import kotlinx.coroutines.Dispatchers // Para especificar o contexto Main para UI

@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current

    // Obter instância do repositório
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
                text = "Bem-vindo de volta!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Faça login para continuar",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(48.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it.trim() }, // Adiciona trim para remover espaços
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
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
                },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    if (email.isNotBlank() && password.isNotBlank()) { // Validação melhorada
                        isLoading = true
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val userId = auth.currentUser?.uid
                                    if (userId != null) {
                                        // Após o login bem-sucedido, busca e cacheia os dados do usuário
                                        coroutineScope.launch {
                                            try {
                                                userRepository.fetchAndCacheUserData(userId)
                                                // Navega para o Dashboard APÓS buscar os dados
                                                withContext(Dispatchers.Main) {
                                                    isLoading = false
                                                    navController.navigate(Screen.Dashboard.route) {
                                                        popUpTo(Screen.Login.route) { inclusive = true }
                                                    }
                                                }
                                            } catch (e: Exception) {
                                                // Mesmo que o cache falhe, o login foi bem-sucedido.
                                                // Pode-se optar por navegar ou mostrar um erro específico de cache.
                                                // Por simplicidade, navegamos, mas logamos o erro.
                                                withContext(Dispatchers.Main) {
                                                    isLoading = false
                                                    Toast.makeText(context, "Login bem-sucedido, mas falha ao sincronizar dados locais: ${e.message}", Toast.LENGTH_LONG).show()
                                                    navController.navigate(Screen.Dashboard.route) {
                                                        popUpTo(Screen.Login.route) { inclusive = true }
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        // Caso raro: task.isSuccessful mas auth.currentUser?.uid é null
                                        isLoading = false
                                        Toast.makeText(context, "Falha ao obter ID do usuário após login.", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    isLoading = false
                                    val errorMessage = task.exception?.message ?: "Falha na autenticação."
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
                    Text("Login", fontSize = 18.sp)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Não tem uma conta? Registre-se",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable {
                    if (!isLoading) { // Evita navegação enquanto uma operação está em progresso
                        navController.navigate(Screen.Register.route)
                    }
                }
            )
        }
    }
}

