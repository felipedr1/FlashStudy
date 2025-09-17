package com.FlashStudy.data.mapper

import com.FlashStudy.data.dto.BaralhoDto
import com.FlashStudy.data.dto.CartaDto
import com.FlashStudy.data.model.BaralhoBancoDados
import com.FlashStudy.data.model.Card
import com.FlashStudy.data.model.CardType

/* ---------- de REMOTE p/ DOMÍNIO ---------- */

fun BaralhoDto.toDomain(): BaralhoBancoDados =
    BaralhoBancoDados(
        id          = id,
        titulo      = titulo,
        cartas      = cartas.map { it.toDomain() }.toMutableList(),
        idUsuario   = idUsuario
    )

fun CartaDto.toDomain(): Card = Card(
    topico         = topico,
    tipo           = CardType.fromBackend(tipo),   // ← trocado
    pergunta       = pergunta,
    resposta       = resposta,
    alternativas   = alternativas,
    localizacao    = localizacao.ifBlank { null },
    proximaRevisao = proximaRevisao
)

/* ---------- de DOMÍNIO p/ REMOTE (POST/PUT) ---------- */

fun BaralhoBancoDados.toDto(): BaralhoDto =
    BaralhoDto(
        id          = id,
        titulo      = titulo,
        cartas      = cartas.map { it.toDto() },
        idUsuario   = idUsuario
    )

fun Card.toDto(): CartaDto = CartaDto(
    topico         = topico,
    tipo           = tipo.backendValue,            // ← usa o valor que o backend espera
    pergunta       = pergunta,
    resposta       = resposta,
    alternativas   = alternativas,
    localizacao    = localizacao ?: "",
    proximaRevisao = proximaRevisao
)