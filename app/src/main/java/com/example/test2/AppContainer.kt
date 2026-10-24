package com.example.test2

import android.content.Context
import com.example.test2.data.local.AppDatabase
import com.example.test2.data.repository.AuthRepository
import com.example.test2.session.SessionManager

/** Lightweight service locator used by Activities/ViewModels in this small project. */
class AppContainer(context: Context) {
    val database: AppDatabase = AppDatabase.getInstance(context)
    val authRepository: AuthRepository = AuthRepository(database)
    val sessionManager: SessionManager = SessionManager(context)
}