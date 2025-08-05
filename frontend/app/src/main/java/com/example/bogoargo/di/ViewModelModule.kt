package com.example.bogoargo.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object ViewModelModule {
    
    // ViewModelScoped dependencies can be added here if needed
    // For example, if you have use cases or interactors that should be scoped to ViewModel lifecycle
}