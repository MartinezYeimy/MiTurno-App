package com.miturno.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.miturno.app.data.model.Point
import com.miturno.app.data.model.ServiceItem
import com.miturno.app.data.model.Turn
import com.miturno.app.data.model.User

@Database(
    entities = [Point::class, ServiceItem::class, User::class, Turn::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): AppDao

    companion object {
        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "miturno.db")
                .fallbackToDestructiveMigration()
                .build()
    }
}
