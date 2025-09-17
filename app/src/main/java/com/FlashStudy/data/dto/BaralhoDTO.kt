package com.FlashStudy.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BaralhoDto(
    @SerialName("id")          val id: String,          // ObjectId -> String
    val titulo: String,
    val cartas: List<CartaDto>,
    @SerialName("id_usuario")  val idUsuario: String
)

@Serializable
data class CartaDto(
    val topico: String,
    val tipo: String,                      // string crua vinda do backend
    val pergunta: String,
    val resposta: Int,
    val alternativas: List<String>,
    val localizacao: String,
    @SerialName("proxima_revisao") val proximaRevisao: String
)