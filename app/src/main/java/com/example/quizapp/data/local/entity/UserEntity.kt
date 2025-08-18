package com.example.quizapp.data.local.entity // Crie este pacote

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representa a tabela 'users' no banco de dados Room.
 * Armazena informações básicas do usuário.
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val uid: String, // ID do Firebase Auth, servirá como chave primária
    val name: String?,
    val email: String?
    // Você pode adicionar um campo como 'lastRefreshed: Long' para controlar a atualização do Firebase
)
