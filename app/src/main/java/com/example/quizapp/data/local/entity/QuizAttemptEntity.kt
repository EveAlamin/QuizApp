package com.example.quizapp.data.local.entity // Crie este pacote

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Representa a tabela 'quiz_attempts' no banco de dados Room.
 * Armazena o histórico de tentativas de quiz do usuário.
 *
 * @property id Chave primária local autogerada para a tentativa.
 * @property firebaseDocId (Opcional) ID do documento no Firestore, útil para sincronização.
 * @property userId Chave estrangeira referenciando o UserEntity.uid.
 * @property score Pontuação obtida.
 * @property totalQuestions Número total de perguntas no quiz.
 * @property timestamp Quando a tentativa foi realizada.
 * @property isSyncedWithFirebase Flag para controlar o estado de sincronização com o Firebase.
 */
@Entity(
    tableName = "quiz_attempts",
    foreignKeys = [ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["uid"],      // Coluna na tabela pai (UserEntity)
        childColumns = ["userId"],   // Coluna nesta tabela (QuizAttemptEntity)
        onDelete = ForeignKey.CASCADE // Se um UserEntity for deletado, suas QuizAttemptEntity também serão.
    )],
    indices = [Index(value = ["userId"])] // Index para otimizar consultas por userId
)
data class QuizAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val firebaseDocId: String? = null, // Para mapear para o documento do Firestore
    val userId: String,
    val score: Int,
    val totalQuestions: Int,
    val timestamp: Long,
    var isSyncedWithFirebase: Boolean = false // Inicialmente falso ao salvar localmente
)