package com.example.data

import android.content.Context
import androidx.room.*
import com.example.model.ChatMessageEntity
import com.example.model.PersonaEntity
import com.example.model.SessionGradeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClinicalDao {
    @Query("SELECT * FROM personas")
    fun getAllPersonas(): Flow<List<PersonaEntity>>

    @Query("SELECT * FROM personas WHERE id = :id")
    suspend fun getPersonaById(id: Long): PersonaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPersona(persona: PersonaEntity): Long

    @Query("DELETE FROM personas WHERE id = :id")
    suspend fun deletePersona(id: Long)

    @Query("SELECT * FROM chat_messages WHERE personaId = :personaId ORDER BY timestamp ASC")
    fun getMessagesForPersona(personaId: Long): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Query("SELECT * FROM session_grades ORDER BY timestamp DESC")
    fun getAllGrades(): Flow<List<SessionGradeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrade(grade: SessionGradeEntity)
}

@Database(entities = [PersonaEntity::class, ChatMessageEntity::class, SessionGradeEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clinicalDao(): ClinicalDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "clinical_role_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
