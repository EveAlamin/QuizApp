package com.example.quizapp // No seu pacote raiz

import android.app.Application
import com.example.quizapp.data.local.AppDatabase
import com.example.quizapp.data.repository.QuizHistoryRepository
import com.example.quizapp.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class QuizApplication : Application() {
    // Usar lazy para que o banco de dados e os repositórios
    // sejam criados apenas quando forem realmente necessários.
    val database by lazy { AppDatabase.getDatabase(this) }

    val firebaseAuth by lazy { FirebaseAuth.getInstance() }
    val firestore by lazy { FirebaseFirestore.getInstance() }

    val userRepository by lazy {
        UserRepository(database.userDao(), firestore, firebaseAuth)
    }
    val quizHistoryRepository by lazy {
        QuizHistoryRepository(database.quizAttemptDao(), firestore)
    }

    // Você também pode querer um WorkManager para sincronização em background
    // val workManager by lazy { WorkManager.getInstance(applicationContext) }

    override fun onCreate() {
        super.onCreate()
        // Aqui você pode iniciar uma sincronização inicial se houver conexão
         CoroutineScope(Dispatchers.IO).launch {
             quizHistoryRepository.syncAllUnsyncedAttempts()
         }
    }
}