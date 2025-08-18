package com.example.quizapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign // Importar TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.quizapp.UserScore
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankingScreen(navController: NavController) {
    var rankingList by remember { mutableStateOf<List<UserScore>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // ADIÇÃO: Estado para mensagem de erro
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(key1 = Unit) {
        isLoading = true // Garante que isLoading seja true no início da busca
        errorMessage = null // Limpa erros anteriores
        FirebaseFirestore.getInstance().collection("ranking")
            .orderBy("totalScore", Query.Direction.DESCENDING)
            .limit(100)
            .get()
            .addOnSuccessListener { result ->
                rankingList = result.map { it.toObject(UserScore::class.java) }
                isLoading = false
            }
            .addOnFailureListener { exception -> // Captura a exceção
                // ALTERAÇÃO: Define a mensagem de erro
                errorMessage = "Falha ao carregar o ranking: ${exception.message}"
                isLoading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ranking Global") },
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
            } else if (rankingList.isEmpty()) {
                // Mensagem específica se não houver erro, mas a lista estiver vazia
                Text(
                    "O ranking ainda está vazio.",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(rankingList) { index, userScore ->
                        RankingItem(rank = index + 1, userScore = userScore)
                    }
                }
            }
        }
    }
}

@Composable
fun RankingItem(rank: Int, userScore: UserScore) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$rank.",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = userScore.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${userScore.totalScore} pts",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

