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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.quizapp.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController) {
    var userName by remember { mutableStateOf("Usuário") }

    LaunchedEffect(key1 = Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            FirebaseFirestore.getInstance().collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    userName = document.getString("name") ?: "Usuário"
                }
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
                    FirebaseAuth.getInstance().signOut()
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