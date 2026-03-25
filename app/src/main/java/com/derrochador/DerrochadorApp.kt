package com.derrochador

import android.app.Application
import com.derrochador.data.AppDatabase
import com.derrochador.data.TransactionRepository

class DerrochadorApp : Application() {

    val database by lazy { AppDatabase.getInstance(this) }
    val repository by lazy { TransactionRepository(database.transactionDao()) }
}
