package com.example.quizapp

data class UserScore(
    val userId: String = "",
    val name: String = "",
    val totalScore: Long = 0 // Alterado para Long para evitar problemas com números grandes
)