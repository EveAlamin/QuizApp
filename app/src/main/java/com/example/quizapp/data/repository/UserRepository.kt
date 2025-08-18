package com.example.quizapp.data.repository

import android.util.Log
import com.example.quizapp.data.local.dao.UserDao
import com.example.quizapp.data.local.entity.UserEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val userDao: UserDao,
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) {
    private val TAG = "UserRepository"

    /**
     * Obtém os dados do usuário. Primeiro tenta do cache local (Room),
     * depois (ou em paralelo) atualiza a partir do Firebase se necessário.
     * @param userId O ID do usuário.
     * @return Um Flow que emite o UserEntity.
     */
    fun getUser(userId: String): Flow<UserEntity?> {
        // Retorna o Flow do DAO, que vai emitir dados do Room.
        // A lógica de atualização do Firebase pode ser disparada separadamente.
        return userDao.getUserById(userId)
    }

    /**
     * Busca o nome do usuário. Tenta do cache, se não encontrar, busca no Firebase
     * e atualiza o cache.
     */
    suspend fun getUserName(userId: String): String? {
        var name = userDao.getUserName(userId)
        if (name == null) {
            Log.d(TAG, "Nome não encontrado no cache para $userId, buscando no Firebase.")
            fetchAndCacheUserData(userId) // Tenta buscar do Firebase e salvar
            name = userDao.getUserName(userId) // Tenta novamente do cache
        }
        return name
    }


    /**
     * Busca os dados do usuário do Firebase e salva/atualiza no Room.
     * Chamado no login/registro ou quando se quer forçar uma atualização.
     * @param userId O ID do usuário.
     */
    suspend fun fetchAndCacheUserData(userId: String) {
        try {
            val document = firestore.collection("users").document(userId).get().await()
            if (document.exists()) {
                val name = document.getString("name")
                val email = document.getString("email") // Ou firebaseAuth.currentUser?.email
                val userEntity = UserEntity(uid = userId, name = name, email = email)
                userDao.insertOrUpdateUser(userEntity)
                Log.d(TAG, "Dados do usuário $userId cacheados/atualizados do Firebase.")
            } else {
                Log.w(TAG, "Documento do usuário $userId não encontrado no Firebase.")
                // Opcional: Se o usuário está autenticado mas não tem doc no Firestore,
                // pode criar um UserEntity com dados do Auth.
                firebaseAuth.currentUser?.let { fbUser ->
                    if(fbUser.uid == userId) {
                        val userEntity = UserEntity(
                            uid = userId,
                            name = fbUser.displayName,
                            email = fbUser.email
                        )
                        userDao.insertOrUpdateUser(userEntity)
                        Log.d(TAG, "Dados do usuário $userId (do Auth) cacheados.")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar dados do usuário $userId do Firebase: ${e.message}", e)
        }
    }

    /**
     * Salva os dados do usuário no Firebase (geralmente após o registro).
     * Também salva/atualiza no cache local.
     */
    suspend fun saveUserToFirebaseAndCache(userId: String, name: String, email: String) {
        val userMap = hashMapOf("name" to name, "email" to email)
        try {
            firestore.collection("users").document(userId).set(userMap).await()
            val userEntity = UserEntity(uid = userId, name = name, email = email)
            userDao.insertOrUpdateUser(userEntity)
            Log.d(TAG, "Usuário $userId salvo no Firebase e cache.")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao salvar usuário $userId no Firebase: ${e.message}", e)
            // Considerar salvar apenas localmente se o Firebase falhar e tentar sincronizar depois
        }
    }


// --- Definição da função updateUserRanking no UserRepository (Exemplo) ---
// Você precisaria adicionar algo assim no seu UserRepository.kt:

    suspend fun updateUserRanking(userId: String, scoreGained: Int) {
        val rankingRef = firestore.collection("ranking").document(userId)
        val userRef = firestore.collection("users").document(userId) // Para buscar o nome

        try {
            firestore.runTransaction { transaction ->
                val userSnapshot = transaction.get(userRef)
                val userName = userSnapshot.getString("name") ?: firebaseAuth.currentUser?.displayName ?: "Usuário Anônimo"

                val rankingSnapshot = transaction.get(rankingRef)

                if (rankingSnapshot.exists()) {
                    val currentTotalScore = rankingSnapshot.getLong("totalScore") ?: 0L
                    val newTotalScore = currentTotalScore + scoreGained
                    transaction.update(rankingRef, "totalScore", newTotalScore)
                    // Se o nome no ranking puder mudar, você pode querer atualizá-lo aqui também
                    // transaction.update(rankingRef, "name", userName)
                    Log.d("UserRepository", "Ranking atualizado para usuário $userId. Novo totalScore: $newTotalScore")
                } else {
                    val newUserScore = mapOf(
                        "userId" to userId,
                        "name" to userName,
                        "totalScore" to scoreGained.toLong()
                    )
                    transaction.set(rankingRef, newUserScore)
                    Log.d("UserRepository", "Novo ranking criado para usuário $userId com totalScore: $scoreGained")
                }
                null // Transações bem-sucedidas retornam null ou o resultado desejado
            }.await() // Usar .await() para esperar a conclusão da transação em uma suspend function
        } catch (e: Exception) {
            Log.e("UserRepository", "Falha na transação de atualização do ranking para $userId: ${e.message}", e)
            throw e // Re-lança a exceção para ser tratada pela tela
        }
    }

}