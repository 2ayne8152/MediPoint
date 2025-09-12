package com.example.medipoint.Data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [MedicalInfoEntity::class, Alerts::class], version = 1, exportSchema = false)
// Increment version if you change schema in the future and provide migrations
abstract class MediPointDatabase : RoomDatabase() {

    abstract fun medicalInfoDao(): MedicalInfoDao // This provides your DAO
    abstract fun alertsDao(): AlertsDao

    companion object {
        @Volatile // Ensures that the value of INSTANCE is always up-to-date and the same to all execution threads
        private var INSTANCE: MediPointDatabase? = null

        fun getDatabase(context: Context): MediPointDatabase {
            // If the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) { // synchronized block to prevent race conditions
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MediPointDatabase::class.java,
                    "medipoint_database" // This is the filename for your local SQLite database
                )
                    // Wipes and rebuilds instead of migrating if no Migration object.
                    // Migration is not covered in this example.
                    // .fallbackToDestructiveMigration() // Use with caution for development only
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }

    }
}