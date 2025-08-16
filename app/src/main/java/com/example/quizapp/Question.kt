package com.example.quizapp

data class Question(
    val questionText: String = "",
    val options: List<String> = emptyList(),
    val correctAnswer: String = ""
)