package com.example.todolist.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.todotest.data.database.TodoItem
import com.todotest.data.database.TodoItemDao

@Database(entities = [TodoItem::class], version = 1, exportSchema = false)
abstract class TodoItemDatabase : RoomDatabase() {

    abstract fun todoDao(): TodoItemDao

    companion object {
        private var DB_INSTANCE: TodoItemDatabase? = null

        fun getInstance(context: Context): TodoItemDatabase? {
            if (DB_INSTANCE == null) {
                synchronized(TodoItemDatabase::class) {
                    DB_INSTANCE = Room.databaseBuilder(context,
                        TodoItemDatabase::class.java,
                        "todo_db")
                        .build()
                }
            }

            return DB_INSTANCE
        }
    }

}