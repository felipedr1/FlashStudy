package com.FlashStudy.data.repository

import com.FlashStudy.data.dao.UsuarioDao
import com.FlashStudy.data.model.Usuario
import kotlin.uuid.ExperimentalUuidApi

class UsuarioRepository(private val usuarioDao: UsuarioDao) {

    @OptIn(ExperimentalUuidApi::class)
    suspend fun getOrCreateUsuarioUuid(): String {
        val existingUuid = usuarioDao.getUuid()
        if (existingUuid != null) {
            return existingUuid
        }

        val novoUsuario = Usuario()
        usuarioDao.insertUsuario(novoUsuario)

        return novoUsuario.uuid
    }

    suspend fun getUsuarioUuid(): String? {
        return usuarioDao.getUuid()
    }
}