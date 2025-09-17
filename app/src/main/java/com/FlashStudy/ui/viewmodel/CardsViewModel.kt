// ui/viewmodel/CardsViewModel.kt
package com.FlashStudy.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.FlashStudy.data.model.Card
import com.FlashStudy.data.model.Difficulty
import com.FlashStudy.data.repository.BaralhoBackendRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CardsViewModel : ViewModel() {

    private val repo = BaralhoBackendRepository()
    private var baralhoId: String = ""
    private var _wrongAwnser : Boolean = false

    // Lista completa de cartas do baralho
    private val _cards = MutableStateFlow<List<Card>>(emptyList())
    val cards: StateFlow<List<Card>> = _cards.asStateFlow()

    // Carta atualmente exibida
    private val _currentCard = MutableStateFlow<Card?>(null)
    val currentCard: StateFlow<Card?> = _currentCard.asStateFlow()

    // Índice da carta na lista
    private val _cardIndex = MutableStateFlow(0)
    val cardIndex: StateFlow<Int> = _cardIndex.asStateFlow()

    // Total de cartas
    private val _totalCards = MutableStateFlow(0)
    val totalCards: StateFlow<Int> = _totalCards.asStateFlow()

    // Controle de resposta e revisão
    private val _selectedAnswer = MutableStateFlow<Int?>(null)
    val selectedAnswer: StateFlow<Int?> = _selectedAnswer.asStateFlow()

    private val _isAnswerRevealed = MutableStateFlow(false)
    val isAnswerRevealed: StateFlow<Boolean> = _isAnswerRevealed.asStateFlow()

    private val _showDifficultyDialog = MutableStateFlow(false)
    val showDifficultyDialog: StateFlow<Boolean> = _showDifficultyDialog.asStateFlow()

    private val _selectedDifficulty = MutableStateFlow<Difficulty?>(null)
    val selectedDifficulty: StateFlow<Difficulty?> = _selectedDifficulty.asStateFlow()

    private val _correctAnswers = MutableStateFlow(0)
    val correctAnswers: StateFlow<Int> = _correctAnswers.asStateFlow()

    private val _showSummary = MutableStateFlow(false)
    val showSummary: StateFlow<Boolean> = _showSummary.asStateFlow()

    /**
     * Busca as cartas do baralho e ordena pela proximaRevisao (da menor para a maior)
     */
    fun fetchCards(baralhoId: String) {
        // grava o id para uso posterior
        this.baralhoId = baralhoId

        viewModelScope.launch {
            try {
                val baralho = repo.obter(baralhoId)    // GET /baralhos/{id}

                // Ordenar cartas pela proximaRevisao (da mais próxima para a mais distante)
                val sortedCards = baralho.cartas.sortedBy {
                    parseIsoDate(it.proximaRevisao)
                }

                _cards.value = sortedCards
                _totalCards.value = sortedCards.size
                resetSession()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Converte string ISO para Date para permitir comparação/ordenação
     */
    private fun parseIsoDate(isoDateString: String): Date {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            format.parse(isoDateString) ?: Date()
        } catch (e: Exception) {
            Date() // Em caso de erro, retorna data atual
        }
    }

    fun selectAnswer(answerIndex: Int) {
        _selectedAnswer.value = answerIndex
        _isAnswerRevealed.value = true
        _currentCard.value?.let { card ->
            if (answerIndex == card.resposta) {
                _correctAnswers.value += 1
            } else {
                _wrongAwnser = true
            }
        }
        _showDifficultyDialog.value = true
    }

    fun selectDifficulty(difficulty: Difficulty) {
        _selectedDifficulty.value = difficulty
        _showDifficultyDialog.value = false

        val idx = _cardIndex.value
        val current = _currentCard.value ?: return

        // 1) calcula nova data ISO
        var hours = when (difficulty) {
            Difficulty.EASY   -> 96.0
            Difficulty.MEDIUM -> 72.0
            Difficulty.HARD   -> 48.0
            Difficulty.IMPOSSIBLE -> 24.0
        }

        if(_wrongAwnser) {
            hours *= 0.5
            _wrongAwnser = false // Resetar para próxima carta
        }

        val cal = Calendar.getInstance().apply { add(Calendar.HOUR, hours.toInt()) }
        val isoFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val nextRev = isoFmt.format(cal.time)

        // 2) atualiza a carta localmente
        val updatedCard = current.copy(proximaRevisao = nextRev)
        val updatedList = _cards.value.toMutableList().apply {
            this[idx] = updatedCard
        }

        // Reordenar a lista após atualização
        val sortedList = updatedList.sortedBy { parseIsoDate(it.proximaRevisao) }
        _cards.value = sortedList

        // Encontrar o novo índice da carta atual após ordenação
        val newIndex = sortedList.indexOf(updatedCard)
        if (newIndex >= 0) {
            _cardIndex.value = newIndex
            _currentCard.value = updatedCard
        }

        // 3) persiste no backend o deck inteiro
        viewModelScope.launch {
            try {
                val baralho = repo.obter(baralhoId)
                // Atualize a carta no baralho original
                val updatedBaralhoCards = baralho.cartas.toMutableList()
                val originalCardIndex = updatedBaralhoCards.indexOfFirst { it.pergunta == current.pergunta }
                if (originalCardIndex >= 0) {
                    updatedBaralhoCards[originalCardIndex] = updatedCard
                }

                val updatedBaralho = baralho.copy(cartas = updatedBaralhoCards)
                val persisted = repo.atualizar(updatedBaralho)  // PUT /baralhos/{id}

                // Reordenar novamente após persistência
                val persistedSorted = persisted.cartas.sortedBy { parseIsoDate(it.proximaRevisao) }
                _cards.value = persistedSorted

                // Reajustar o índice e a carta atual
                val persistedCurrentIndex = persistedSorted.indexOfFirst { it.pergunta == updatedCard.pergunta }
                if (persistedCurrentIndex >= 0) {
                    _cardIndex.value = persistedCurrentIndex
                    _currentCard.value = persistedSorted[persistedCurrentIndex]
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun nextCard() {
        val next = _cardIndex.value + 1
        if (next < _cards.value.size) {
            _cardIndex.value = next
            _currentCard.value = _cards.value[next]
            _selectedAnswer.value = null
            _isAnswerRevealed.value = false
        } else {
            _currentCard.value = null
            _showSummary.value = true
        }
    }

    fun resetSession() {
        _correctAnswers.value = 0
        _cardIndex.value = 0
        _currentCard.value = _cards.value.firstOrNull()
        _selectedAnswer.value = null
        _isAnswerRevealed.value = false
        _selectedDifficulty.value = null
        _showSummary.value = false
        _wrongAwnser = false
    }
}