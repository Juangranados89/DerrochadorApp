package com.derrochador.di

import android.content.Context
import com.derrochador.agents.BrainAgent
import com.derrochador.domain.usecase.ClassifyExpenseUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AgentModule {

    @Provides
    @Singleton
    fun provideBrainAgent(
        @ApplicationContext context: Context,
        classifyExpenseUseCase: ClassifyExpenseUseCase
    ): BrainAgent {
        return BrainAgent(context, classifyExpenseUseCase)
    }
}
