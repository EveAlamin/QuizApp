package com.example.quizapp.data.local.dao // Crie este pacote

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.quizapp.data.local.entity.QuizAttemptEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para a entidade QuizAttemptEntity.
 * Define métodos para interagir com a tabela 'quiz_attempts'.
 */
@Dao
interface QuizAttemptDao {
    /**
     * Insere uma nova tentativa de quiz.
     * @param attempt A tentativa de quiz a ser inserida.
     * @return O ID da linha inserida.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE) // Ignora se já existir um com o mesmo ID (improvável com autogenerate)
    suspend fun insertAttempt(attempt: QuizAttemptEntity): Long

    /**
     * Atualiza uma tentativa de quiz existente.
     * Útil para marcar como sincronizado ou atualizar o firebaseDocId.
     * @param attempt A tentativa de quiz a ser atualizada.
     */
    @Update
    suspend fun updateAttempt(attempt: QuizAttemptEntity)

    /**
     * Busca todas as tentativas de quiz para um usuário específico, ordenadas por data (mais recente primeiro).
     * Retorna um Flow para observação de mudanças.
     * @param userId O ID do usuário.
     * @return Um Flow que emite uma lista de QuizAttemptEntity.
     */
    @Query("SELECT * FROM quiz_attempts WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAttemptsForUser(userId: String): Flow<List<QuizAttemptEntity>>

    /**
     * Busca todas as tentativas que ainda não foram sincronizadas com o Firebase.
     * @return Uma lista de QuizAttemptEntity não sincronizadas.
     */
    @Query("SELECT * FROM quiz_attempts WHERE isSyncedWithFirebase = 0 ORDER BY timestamp ASC") // Pega as mais antigas primeiro
    suspend fun getUnsyncedAttempts(): List<QuizAttemptEntity>

    /**
     * Busca uma tentativa pelo seu ID local.
     * @param attemptId O ID local da tentativa.
     * @return O QuizAttemptEntity ou null.
     */
    @Query("SELECT * FROM quiz_attempts WHERE id = :attemptId LIMIT 1")
    suspend fun getAttemptById(attemptId: Long): QuizAttemptEntity?

    /**
     * (Opcional) Deleta todas as tentativas de um usuário.
     * @param userId O ID do usuário.
     */
    @Query("DELETE FROM quiz_attempts WHERE userId = :userId")
    suspend fun deleteAttemptsForUser(userId: String)

    // Implementar getAttemptByFirebaseId, para ser usada da sequite forma: attemptDao.getAttemptByFirebaseId(it)
    /**
     * Busca uma tentativa pelo seu ID do Firebase.
     * @param firebaseDocId O ID do documento Firebase da tentativa.
     * @return O QuizAttemptEntity ou null se não encontrado.
     */
    @Query("SELECT * FROM quiz_attempts WHERE firebaseDocId = :firebaseDocId LIMIT 1")
    suspend fun getAttemptByFirebaseId(firebaseDocId: String): QuizAttemptEntity?
}
