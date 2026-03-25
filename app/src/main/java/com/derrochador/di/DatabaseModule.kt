package com.derrochador.di

import android.content.Context
import androidx.room.Room
import com.derrochador.data.dao.TransactionDao
import com.derrochador.data.database.DerrochadorDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): DerrochadorDatabase {
        return Room.databaseBuilder(
            context,
            DerrochadorDatabase::class.java,
            DerrochadorDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    fun provideTransactionDao(database: DerrochadorDatabase): TransactionDao {
        return database.transactionDao()
    }
}
