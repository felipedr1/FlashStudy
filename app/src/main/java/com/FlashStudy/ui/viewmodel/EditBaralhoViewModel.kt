// ui/viewmodel/EditBaralhoViewModel.kt
package com.FlashStudy.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.FlashStudy.data.database.BaralhoService
import com.FlashStudy.data.model.Card
import com.FlashStudy.data.model.CardType
import com.FlashStudy.data.model.BaralhoBancoDados
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class EditBaralhoViewModel : ViewModel() {

    private val _mongoBaralho = MutableStateFlow<BaralhoBancoDados?>(null)
    val mongoBaralho: StateFlow<BaralhoBancoDados?> = _mongoBaralho.asStateFlow()

    private val _showAddCardDialog = MutableStateFlow(false)
    val showAddCardDialog: StateFlow<Boolean> = _showAddCardDialog.asStateFlow()

    private val _editingCard = MutableStateFlow<Card?>(null)
    val editingCard: StateFlow<Card?> = _editingCard.asStateFlow()

    private val _saveEvent = MutableStateFlow(false)
    val saveEvent: StateFlow<Boolean> = _saveEvent.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    /**
     * Carrega do backend o baralho com o ID informado e popula _mongoBaralho.
     */
    fun loadBaralho(baralhoId: String) {
        viewModelScope.launch {
            try {
                val baralho = BaralhoService.getById(baralhoId)
                _mongoBaralho.value = baralho
            } catch (e: Exception) {
                _snackbarMessage.value = "Falha ao carregar baralho"
                e.printStackTrace()
            }
        }
    }

    fun showAddCardDialog() {
        _editingCard.value = null
        _showAddCardDialog.value = true
    }

    fun hideAddCardDialog() {
        _showAddCardDialog.value = false
        _editingCard.value = null
    }

    fun editCard(card: Card) {
        _editingCard.value = card
        _showAddCardDialog.value = true
    }

    fun addOrUpdateCard(
        topico: String,
        pergunta: String,
        alternativas: List<String>,
        respostaIndex: Int,
        localizacao: String?
    ) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val newCard = Card(
            topico = topico,
            tipo = CardType.MULTIPLE_CHOICE,
            pergunta = pergunta,
            resposta = respostaIndex,
            alternativas = alternativas,
            localizacao = localizacao,
            proximaRevisao = dateFormat.format(Date())
        )

        _mongoBaralho.value?.let { baralho ->
            val newCartas = baralho.cartas.toMutableList()
            _editingCard.value?.let { current ->
                val idx = newCartas.indexOf(current)
                if (idx >= 0) newCartas[idx] = newCard
            } ?: newCartas.add(newCard)

            _mongoBaralho.value = baralho.copy(cartas = newCartas)
        }

        hideAddCardDialog()
    }

    fun deleteCard(card: Card) {
        _mongoBaralho.value?.let { baralho ->
            val newCartas = baralho.cartas.toMutableList().apply { remove(card) }
            _mongoBaralho.value = baralho.copy(cartas = newCartas)
        }
    }

    fun clearSaveEvent() { _saveEvent.value = false }

    fun saveBaralho() {
        _mongoBaralho.value?.let { baralho ->
            viewModelScope.launch {
                try {
                    val atualizado = BaralhoService.update(baralho)
                    _mongoBaralho.value = atualizado       // ← atualiza o Flow local
                    _snackbarMessage.value = "Baralho salvo com sucesso"
                    _saveEvent.value = true
                } catch (e: Exception) {
                    _snackbarMessage.value = "Falha ao salvar baralho"
                }
            }
        }
    }

    fun clearSnackbarMessage() {
        _snackbarMessage.value = null
    }
}