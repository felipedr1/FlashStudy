package com.FlashStudy.data.database

import com.FlashStudy.data.dto.BaralhoDto
import com.FlashStudy.data.dto.CreateBaralhoDTO
import com.FlashStudy.data.mapper.toDomain
import com.FlashStudy.data.mapper.toDto
import com.FlashStudy.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

object BaralhoService {

    private val client = OkHttpClient()
    private val json   = Json { ignoreUnknownKeys = true }
    private val JSON   = "application/json".toMediaType()

    private const val BASE = "http://10.0.2.2:8080/baralhos"   // ajuste se mudar host

    /*--------------------------- GET /baralhos ---------------------------*/
    suspend fun getAll(): List<BaralhoBancoDados> = withContext(Dispatchers.IO) {
        val req  = Request.Builder().url(BASE).get().build()
        val body = client.newCall(req).execute().body?.string()
            ?: error("Corpo vazio")

        json.decodeFromString<List<BaralhoDto>>(body)
            .map { it.toDomain() }
    }

    /*--------------------------- GET /baralhos/{id} ----------------------*/
    suspend fun getById(id: String): BaralhoBancoDados = withContext(Dispatchers.IO) {
        val req  = Request.Builder().url("$BASE/$id").get().build()
        val body = client.newCall(req).execute().body?.string()
            ?: error("Corpo vazio")

        json.decodeFromString<BaralhoDto>(body).toDomain()
    }

    /*--------------------------- POST /baralhos --------------------------*/
    suspend fun create(baralho: BaralhoBancoDados): BaralhoBancoDados = withContext(Dispatchers.IO) {
        // 1) monte o DTO de criação, sem 'id'
        val createDto = CreateBaralhoDTO(
            titulo   = baralho.titulo,
            cartas   = baralho.cartas.map { it.toDto() },
            idUsuario = baralho.idUsuario.toString()
        )

        // 2) serialize só o CreateBaralhoDto
        val bodyReq = json.encodeToString(createDto)
            .toRequestBody(JSON)

        val request = Request.Builder()
            .url(BASE)
            .post(bodyReq)
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            error("Falha ao criar baralho: HTTP ${response.code}")
        }

        // 3) desserialize o body de resposta no BaralhoDto completo (que inclui 'id')
        val respBody = response.body?.string() ?: error("Body vazio")
        val createdDto = json.decodeFromString<BaralhoDto>(respBody)

        // 4) converta para seu modelo de domínio
        createdDto.toDomain()
    }

    /*--------------------------- PUT /baralhos/{id} ----------------------*/
    suspend fun update(baralho: BaralhoBancoDados): BaralhoBancoDados = withContext(Dispatchers.IO) {
        val bodyReq = json.encodeToString(baralho.toDto())
            .toRequestBody(JSON)

        val req  = Request.Builder()
            .url("$BASE/${baralho.id}")
            .put(bodyReq)
            .build()

        val body = client.newCall(req).execute().body?.string()
            ?: error("Corpo vazio")

        json.decodeFromString<BaralhoDto>(body).toDomain()
    }

    /*--------------------------- DELETE /baralhos/{id} -------------------*/
    suspend fun delete(id: String): Boolean = withContext(Dispatchers.IO) {
        val req  = Request.Builder().url("$BASE/$id").delete().build()
        val res  = client.newCall(req).execute()
        res.code == 204 || res.code == 200
    }
}