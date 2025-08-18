package com.example.quizapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext // Necessário para acessar Application
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.quizapp.QuizApplication // Importa a classe Application
import com.example.quizapp.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
// import com.google.firebase.firestore.FirebaseFirestore // Não é mais necessário aqui diretamente
import kotlinx.coroutines.flow.collectLatest // Para coletar o Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController) {
    var userName by remember { mutableStateOf("Usuário") } // Valor padrão
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid // Pega o userId uma vez

    // Obter instância do repositório
    val application = LocalContext.current.applicationContext as QuizApplication
    val userRepository = remember { application.userRepository }

    // 1. Observa o Flow do Room para atualizações do nome do usuário
    LaunchedEffect(key1 = userId) { // Re-executa se o userId mudar
        if (userId != null) {
            userRepository.getUser(userId).collectLatest { userEntity -> // collectLatest para cancelar coletas anteriores se userId mudar
                userEntity?.name?.let { nameFromDb ->
                    if (nameFromDb.isNotBlank()) { // Verifica se o nome do banco não é vazio
                        userName = nameFromDb
                    }
                }
            }
        } else {
            // Se o userId for null (usuário deslogou enquanto na tela), redefina o nome
            userName = "Usuário"
        }
    }

    // 2. Dispara a busca e cache dos dados do Firebase (irá atualizar o Flow acima se houver mudanças)
    LaunchedEffect(key1 = userId) { // Re-executa se o userId mudar
        if (userId != null) {
            // Não precisa tratar o resultado diretamente aqui, pois o Flow acima pegará as atualizações
            userRepository.fetchAndCacheUserData(userId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Olá, $userName!", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DashboardButton(
                text = "Iniciar Novo Quiz",
                icon = Icons.Default.PlayArrow,
                onClick = { navController.navigate(Screen.Quiz.route) }
            )
            Spacer(modifier = Modifier.height(16.dp))
            DashboardButton(
                text = "Histórico",
                icon = Icons.Default.List,
                onClick = { navController.navigate(Screen.History.route) }
            )
            Spacer(modifier = Modifier.height(16.dp))
            DashboardButton(
                text = "Ranking",
                icon = Icons.Default.Star,
                onClick = { navController.navigate(Screen.Ranking.route) }
            )
            Spacer(modifier = Modifier.height(32.dp))
            DashboardButton(
                text = "Sair",
                icon = Icons.Default.ExitToApp,
                onClick = {
                    auth.signOut() // Usa a instância já pega
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                isDestructive = true
            )
        }
    }
}

@Composable
private fun DashboardButton(text: String, icon: ImageVector, onClick: () -> Unit, isDestructive: Boolean = false) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        shape = RoundedCornerShape(16.dp),
        colors = if (isDestructive) {
            ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        } else {
            ButtonDefaults.buttonColors()
        }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            modifier = Modifier.size(24.dp),
            tint = if (isDestructive) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, fontSize = 18.sp, color = if (isDestructive) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimary)
    }
}
