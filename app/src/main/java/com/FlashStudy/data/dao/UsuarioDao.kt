package com.FlashStudy.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.FlashStudy.data.model.Usuario

@Dao
interface UsuarioDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUsuario(usuario: Usuario): Long

    @Query("SELECT * FROM usuario WHERE id = 1")
    suspend fun getUsuario(): Usuario?

    @Query("SELECT uuid FROM usuario WHERE id = 1")
    suspend fun getUuid(): String?
}