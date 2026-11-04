package com.apuntate.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Grupo::class, Participante::class, Gasto::class],
    version = 1,
    exportSchema = false
)
abstract class ApuntateDatabase : RoomDatabase() {
    abstract fun apuntateDao(): ApuntateDao

    companion object {
        @Volatile
        private var INSTANCE: ApuntateDatabase? = null

        fun getInstance(context: Context): ApuntateDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ApuntateDatabase::class.java,
                    "apuntate.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
