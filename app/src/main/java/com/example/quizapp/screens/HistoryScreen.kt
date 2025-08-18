package com.example.quizapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext // Necessário para Application
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.quizapp.QuizApplication // Importa a classe Application
import com.example.quizapp.QuizAttempt // Sua classe de modelo original para a UI
// import com.google.firebase.firestore.FirebaseFirestore // Não mais usado diretamente
// import com.google.firebase.firestore.Query // Não mais usado diretamente
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.map // Para mapear o Flow
import java.text.SimpleDateFormat
import java.util.* // Para Date e Locale
// kotlinx.coroutines.launch não é explicitamente usado aqui se syncAllUnsyncedAttempts não for lançado em um novo escopo.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: NavController) { // navController não é usado, pode ser removido se não houver planos futuros
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid

    // Obter instância do repositório
    val application = LocalContext.current.applicationContext as QuizApplication
    val quizHistoryRepository = remember { application.quizHistoryRepository }

    var isLoading by remember { mutableStateOf(true) } // Controla o indicador de carregamento inicial
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Coleta o histórico do Room como um estado.
    // A UI será atualizada automaticamente quando o DB Room mudar.
    val historyListState = if (userId != null) {
        quizHistoryRepository.getLocalUserHistory(userId)
            .map { entities -> // Mapear List<QuizAttemptEntity> para List<QuizAttempt>
                entities.map { entity ->
                    QuizAttempt( // Seu data class original usado pelo HistoryItem
                        userId = entity.userId, // Se QuizAttempt não tiver userId, pode omitir
                        score = entity.score,
                        totalQuestions = entity.totalQuestions,
                        timestamp = entity.timestamp
                        // firebaseDocId = entity.firebaseDocId // Se QuizAttempt tiver, pode adicionar
                    )
                }
            }
            .collectAsState(initial = emptyList()) // Estado inicial enquanto o Flow não emitiu
    } else {
        // Se não houver userId, o estado conterá uma lista vazia.
        remember { mutableStateOf(emptyList<QuizAttempt>()) }
    }
    // historyList sempre reflete o valor atual de historyListState.
    // Se userId for null, historyList será emptyList() devido à lógica acima.
    val historyList = historyListState.value

    // Lógica para carregar do Firebase e sincronizar
    LaunchedEffect(key1 = userId) {
        if (userId != null) {
            isLoading = true // Inicia o carregamento
            errorMessage = null
            try {
                // 1. Busca o histórico remoto e atualiza o cache do Room.
                // O Flow acima (historyListState) pegará as atualizações.
                quizHistoryRepository.fetchRemoteHistoryAndCache(userId)

                // 2. Tenta sincronizar quaisquer tentativas não enviadas.
                quizHistoryRepository.syncAllUnsyncedAttempts()

            } catch (e: Exception) {
                errorMessage = "Falha ao carregar o histórico do servidor."
                // O histórico local ainda será exibido se disponível.
            } finally {
                isLoading = false // Termina o carregamento após as operações de rede
            }
        } else {
            // Se userId for null (usuário deslogou), não precisamos limpar a lista explicitamente aqui.
            // A recomposição de `historyListState` para o branch `else` já fará com que
            // `historyList` (que é `historyListState.value`) seja uma lista vazia.
            isLoading = false
            errorMessage = "Usuário não autenticado."
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seu Histórico") },
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
            // A lógica de isLoading agora considera a primeira emissão do Flow também
            if (isLoading && historyList.isEmpty() && errorMessage == null) {
                CircularProgressIndicator()
            } else if (errorMessage != null) {
                Text(text = errorMessage!!)
            } else if (historyList.isEmpty()) {
                // Esta condição será verdadeira se o usuário não estiver autenticado (userId == null)
                // ou se o usuário autenticado não tiver histórico.
                Text(text = if (userId == null) "Usuário não autenticado." else "Nenhum histórico encontrado.")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(historyList) { attempt ->
                        HistoryItem(attempt = attempt) // HistoryItem continua usando QuizAttempt
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItem(attempt: QuizAttempt) { // Continua recebendo QuizAttempt
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Pontuação: ${attempt.score}/${attempt.totalQuestions}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            val sdf = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault())
            // O timestamp já é Long, então a criação do Date é a mesma
            val date = Date(attempt.timestamp)
            Text(
                text = sdf.format(date),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
