package com.FlashStudy.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateBaralhoDTO(
    val titulo: String,
    val cartas: List<CartaDto>,
    @SerialName("id_usuario") val idUsuario: String
)