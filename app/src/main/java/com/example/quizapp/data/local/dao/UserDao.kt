package com.example.quizapp.data.local.dao // Crie este pacote

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.quizapp.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para a entidade UserEntity.
 * Define métodos para interagir com a tabela 'users' no banco de dados Room.
 */
@Dao
interface UserDao {
    /**
     * Insere um usuário. Se o usuário já existir (mesmo uid), substitui o antigo.
     * @param user O usuário a ser inserido ou atualizado.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUser(user: UserEntity)

    /**
     * Busca um usuário pelo seu UID.
     * Retorna um Flow, permitindo que a UI observe mudanças nos dados do usuário.
     * @param uid O ID do usuário a ser buscado.
     * @return Um Flow que emite o UserEntity ou null se não encontrado.
     */
    @Query("SELECT * FROM users WHERE uid = :uid LIMIT 1")
    fun getUserById(uid: String): Flow<UserEntity?>

    /**
     * Busca o nome de um usuário pelo seu UID.
     * @param uid O ID do usuário.
     * @return O nome do usuário ou null se não encontrado.
     */
    @Query("SELECT name FROM users WHERE uid = :uid LIMIT 1")
    suspend fun getUserName(uid: String): String?

    // Você pode adicionar um método para deletar um usuário se necessário
    // @Query("DELETE FROM users WHERE uid = :uid")
    // suspend fun deleteUser(uid: String)
}