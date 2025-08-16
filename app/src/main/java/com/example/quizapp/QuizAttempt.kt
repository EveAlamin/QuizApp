package com.example.quizapp

data class QuizAttempt(
    val userId: String = "",
    val score: Int = 0,
    val totalQuestions: Int = 0,
    val timestamp: Long = 0
)