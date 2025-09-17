package com.FlashStudy.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.FlashStudy.data.database.AppDatabase
import com.FlashStudy.data.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UsuarioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: UsuarioRepository

    private val _usuarioUuid = MutableStateFlow<String?>(null)
    val usuarioUuid: StateFlow<String?> = _usuarioUuid.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = UsuarioRepository(database.usuarioDao())
        loadUsuarioUuid()
    }

    fun loadUsuarioUuid() {
        viewModelScope.launch {
            _usuarioUuid.value = repository.getOrCreateUsuarioUuid()
        }
    }

    fun getUsuarioUuid(): String? {
        return _usuarioUuid.value
    }
}