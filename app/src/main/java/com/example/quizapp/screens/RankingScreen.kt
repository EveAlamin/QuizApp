package com.example.quizapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    LaunchedEffect(key1 = Unit) {
        FirebaseFirestore.getInstance().collection("ranking")
            .orderBy("totalScore", Query.Direction.DESCENDING)
            .limit(100) // Limita aos 100 melhores para não sobrecarregar
            .get()
            .addOnSuccessListener { result ->
                rankingList = result.map { it.toObject(UserScore::class.java) }
                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
                // Aqui você pode mostrar uma mensagem de erro
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Ranking") })
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
            } else if (rankingList.isEmpty()) {
                Text("O ranking ainda está vazio.")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$rank. ${userScore.name}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${userScore.totalScore} pts",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}