package com.exemple.reveil.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [User::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun joueurDao(): UserDao

    private class AppDatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                INSTANCE?.joueurDao()?.let { dao ->
                    // Ajout de vous trois
                    dao.inscrire(User(uid = "uid_aliona_1", pseudo = "Aliona", email = "aliona@test.com"))
                    dao.inscrire(User(uid = "uid_ines_2", pseudo = "Inès", email = "ines@test.com"))
                    dao.inscrire(User(uid = "uid_kaouthar_3", pseudo = "Kaouthar", email = "kaouthar@test.com"))

                    // Ajout de deux amis fictifs
                    dao.inscrire(User(uid = "uid_ami_1", pseudo = "BobLÉponge", email = "bob@test.com"))
                    dao.inscrire(User(uid = "uid_ami_2", pseudo = "AliceMerveille", email = "alice@test.com"))
                }
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "reveil_database"
                )
                    .addCallback(AppDatabaseCallback())
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}