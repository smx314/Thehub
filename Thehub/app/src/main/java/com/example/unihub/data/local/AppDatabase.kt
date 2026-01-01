package com.example.unihub.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.unihub.data.local.dao.ExpenseDao
import com.example.unihub.data.local.dao.FocusDao
import com.example.unihub.data.local.dao.ScheduleDao
import com.example.unihub.data.model.Expense
import com.example.unihub.data.model.FocusRecord
import com.example.unihub.data.model.Schedule

@Database(entities = [Expense::class, Schedule::class, FocusRecord::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun focusDao(): FocusDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "unihub_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
