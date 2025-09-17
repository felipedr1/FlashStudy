package com.FlashStudy.data.model

data class BaralhoBancoDados(
    val id: String,
    val titulo: String,
    val cartas: MutableList<Card>,
    val idUsuario: String
)