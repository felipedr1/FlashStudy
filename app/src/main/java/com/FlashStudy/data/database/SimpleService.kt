package com.FlashStudy.data.database

// SimpleService.kt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

object SimpleService {
    private val client = OkHttpClient()

    /**
     * Faz GET em "http://10.0.2.2:8080/hello" e devolve o corpo como String.
     * Chame a partir de uma coroutine (por exemplo, dentro de viewModelScope.launch { ... }).
     */
    suspend fun hello(): String = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("http://10.0.2.2:8080")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("Erro ${response.code}")
            response.body?.string() ?: ""
        }
    }
}