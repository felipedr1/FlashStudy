package com.FlashStudy.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Entity(tableName = "usuario")
data class Usuario(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 1,
    @OptIn(ExperimentalUuidApi::class)
    val uuid: String = Uuid.random().toString()
)