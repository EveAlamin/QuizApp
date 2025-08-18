package com.example.quizapp.data.local // Crie este pacote

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.quizapp.data.local.dao.QuizAttemptDao
import com.example.quizapp.data.local.dao.UserDao
import com.example.quizapp.data.local.entity.QuizAttemptEntity
import com.example.quizapp.data.local.entity.UserEntity

/**
 * Classe principal do banco de dados Room para a aplicação.
 * Define as entidades que o banco de dados contém e fornece acesso aos DAOs.
 *
 * @version 1 - Versão inicial do schema do banco de dados. Incrementar ao fazer alterações no schema.
 */
@Database(
    entities = [UserEntity::class, QuizAttemptEntity::class],
    version = 1, // Incremente se mudar o schema (adicionar/remover tabelas/colunas)
    exportSchema = false // Para este exemplo, pode manter false. Defina true para exportar o schema para controle de versão.
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun quizAttemptDao(): QuizAttemptDao

    companion object {
        // A anotação @Volatile garante que o valor de INSTANCE seja sempre atualizado
        // e visível para todos os threads de execução.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Retorna uma instância singleton do AppDatabase.
         * Usa o padrão de inicialização double-checked locking para segurança de thread.
         *
         * @param context O contexto da aplicação.
         * @return A instância singleton do AppDatabase.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) { // Bloco synchronized para evitar criação de múltiplas instâncias
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "quiz_app_database" // Nome do arquivo do banco de dados
                )
                    // .addMigrations(MIGRATION_1_2, ...) // Adicionar migrações aqui se necessário
                    .fallbackToDestructiveMigration() // Em desenvolvimento, se não houver migração, recria o BD. REMOVER em produção se dados são críticos.
                    .build()
                INSTANCE = instance
                instance // Retorna a instância criada
            }
        }
    }
}
