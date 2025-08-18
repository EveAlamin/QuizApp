package com.example.quizapp.data.repository

import android.util.Log
import com.example.quizapp.QuizAttempt // Sua classe de modelo original
import com.example.quizapp.data.local.dao.QuizAttemptDao
import com.example.quizapp.data.local.entity.QuizAttemptEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class QuizHistoryRepository(
    private val attemptDao: QuizAttemptDao,
    private val firestore: FirebaseFirestore
) {
    private val TAG = "QuizHistoryRepository"

    /**
     * Obtém o histórico de tentativas do usuário a partir do Room.
     * A UI observa este Flow. A sincronização com o Firebase pode ser acionada separadamente.
     */
    fun getLocalUserHistory(userId: String): Flow<List<QuizAttemptEntity>> {
        return attemptDao.getAttemptsForUser(userId)
    }

    /**
     * Salva uma nova tentativa de quiz.
     * Primeiro salva localmente no Room, depois tenta sincronizar com o Firebase.
     * @param userId ID do usuário.
     * @param score Pontuação.
     * @param totalQuestions Total de perguntas.
     * @return O ID local da tentativa inserida.
     */
    suspend fun saveQuizAttempt(userId: String, score: Int, totalQuestions: Int): Long {
        val timestamp = System.currentTimeMillis()
        val localAttempt = QuizAttemptEntity(
            userId = userId,
            score = score,
            totalQuestions = totalQuestions,
            timestamp = timestamp,
            isSyncedWithFirebase = false // Marcar como não sincronizado inicialmente
        )
        val localId = attemptDao.insertAttempt(localAttempt)
        Log.d(TAG, "Tentativa salva localmente com ID: $localId")

        // Dispara a sincronização em uma coroutine separada para não bloquear
        CoroutineScope(Dispatchers.IO).launch {
            syncAttemptToFirebase(localId, localAttempt.copy(id = localId))
        }
        return localId
    }

    /**
     * Sincroniza uma tentativa específica do Room para o Firebase.
     * @param localAttemptId O ID da tentativa no banco de dados Room.
     * @param attemptToSync A entidade da tentativa a ser sincronizada.
     */
    private suspend fun syncAttemptToFirebase(localAttemptId: Long, attemptToSync: QuizAttemptEntity) {
        if (attemptToSync.isSyncedWithFirebase) {
            Log.d(TAG, "Tentativa ID $localAttemptId já sincronizada.")
            return
        }

        // Criar o objeto para o Firestore (similar ao seu QuizAttempt original ou o hashMap)
        val firestoreAttempt = hashMapOf(
            "userId" to attemptToSync.userId,
            "score" to attemptToSync.score,
            "totalQuestions" to attemptToSync.totalQuestions,
            "timestamp" to attemptToSync.timestamp
        )

        try {
            val documentReference = firestore.collection("history").add(firestoreAttempt).await()
            // Atualiza o registro local com o ID do Firebase e marca como sincronizado
            val updatedLocalAttempt = attemptToSync.copy(
                firebaseDocId = documentReference.id,
                isSyncedWithFirebase = true
            )
            attemptDao.updateAttempt(updatedLocalAttempt)
            Log.i(TAG, "Tentativa ID $localAttemptId sincronizada com Firebase ID: ${documentReference.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Falha ao sincronizar tentativa ID $localAttemptId com Firebase: ${e.message}", e)
            // Implementar lógica de retry ou deixar para uma sincronização em lote posterior
        }
    }

    /**
     * Busca o histórico do Firebase para um usuário e atualiza/insere no Room.
     * Chamado, por exemplo, ao carregar a tela de histórico se o cache estiver vazio ou antigo.
     */
    suspend fun fetchRemoteHistoryAndCache(userId: String) {
        try {
            Log.d(TAG, "Buscando histórico do Firebase para $userId")
            val querySnapshot = firestore.collection("history")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            val firebaseAttempts = querySnapshot.documents.mapNotNull { doc ->
                doc.toObject(QuizAttempt::class.java)?.let { attempt -> // Usando sua classe QuizAttempt para desserializar
                    QuizAttemptEntity( // Convertendo para QuizAttemptEntity
                        firebaseDocId = doc.id,
                        userId = attempt.userId,
                        score = attempt.score,
                        totalQuestions = attempt.totalQuestions,
                        timestamp = attempt.timestamp,
                        isSyncedWithFirebase = true // Veio do Firebase, então está sincronizado
                    )
                }
            }

            if (firebaseAttempts.isNotEmpty()) {
                withContext(Dispatchers.IO) {
                    firebaseAttempts.forEach { entity ->
                        // Verifica se já existe pelo firebaseDocId para evitar duplicatas e apenas atualizar
                        val existing = entity.firebaseDocId?.let { attemptDao.getAttemptByFirebaseId(it)  } // Implementar getAttemptByFirebaseId se necessário
                        if (existing == null) {
                            attemptDao.insertAttempt(entity)
                        } else if (existing.timestamp < entity.timestamp || !existing.isSyncedWithFirebase) { // Exemplo de lógica de atualização
                            attemptDao.updateAttempt(entity.copy(id = existing.id)) // Mantém o ID local
                        }
                    }
                }
                Log.d(TAG, "${firebaseAttempts.size} tentativas do Firebase cacheadas para $userId.")
            } else {
                Log.d(TAG, "Nenhum histórico encontrado no Firebase para $userId.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar histórico do Firebase para $userId: ${e.message}", e)
        }
    }

    /**
     * Sincroniza todas as tentativas não sincronizadas do Room para o Firebase.
     * Pode ser chamado periodicamente, ou ao iniciar o app com conexão.
     */
    suspend fun syncAllUnsyncedAttempts() {
        val unsynced = attemptDao.getUnsyncedAttempts()
        if (unsynced.isNotEmpty()) {
            Log.i(TAG, "Encontradas ${unsynced.size} tentativas não sincronizadas. Sincronizando...")
            unsynced.forEach { attempt ->
                syncAttemptToFirebase(attempt.id, attempt)
            }
            Log.i(TAG, "Sincronização de tentativas não sincronizadas concluída.")
        } else {
            Log.d(TAG, "Nenhuma tentativa não sincronizada para enviar ao Firebase.")
        }
    }
}
