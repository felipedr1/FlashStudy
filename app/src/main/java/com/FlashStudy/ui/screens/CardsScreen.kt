// ui/screens/CardsScreen.kt
package com.FlashStudy.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.FlashStudy.data.model.Card
import com.FlashStudy.ui.components.DifficultyDialog
import com.FlashStudy.ui.components.SessionSummary
import com.FlashStudy.ui.viewmodel.BaralhoViewModel
import com.FlashStudy.ui.viewmodel.CardsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardsScreen(
    baralhoId: String,
    navController: NavController,
    baralhoViewModel: BaralhoViewModel,
    cardsViewModel: CardsViewModel = viewModel()
) {
    // Deck metadata (title, etc)
    val baralho by baralhoViewModel.currentBaralho.collectAsState()

    // Card session state
    val currentCard by cardsViewModel.currentCard.collectAsState()
    val selectedAnswer by cardsViewModel.selectedAnswer.collectAsState()
    val isAnswerRevealed by cardsViewModel.isAnswerRevealed.collectAsState()
    val cardIndex by cardsViewModel.cardIndex.collectAsState()
    val totalCards by cardsViewModel.totalCards.collectAsState()
    val showDifficultyDialog by cardsViewModel.showDifficultyDialog.collectAsState()
    val correctAnswers by cardsViewModel.correctAnswers.collectAsState()
    val showSummary by cardsViewModel.showSummary.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // 1) Load deck info & cards from backend
    LaunchedEffect(baralhoId) {
        baralhoViewModel.fetchBaralhoById(baralhoId)
        cardsViewModel.fetchCards(baralhoId)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(baralho?.titulo ?: "Baralho")
                        Text(
                            text = "Carta ${cardIndex + 1} de $totalCards",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Show current card or empty state
            currentCard?.let { card ->
                CardContent(
                    card = card,
                    selectedAnswer = selectedAnswer,
                    isAnswerRevealed = isAnswerRevealed,
                    onAnswerSelected = { cardsViewModel.selectAnswer(it) },
                    onNextCard = { cardsViewModel.nextCard() }
                )
            } ?: if (!showSummary) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Não há cartas disponíveis", textAlign = TextAlign.Center, fontSize = 18.sp)
                }
            } else {

            }

            if (showDifficultyDialog) {
                DifficultyDialog { difficulty ->
                    cardsViewModel.selectDifficulty(difficulty)
                }
            }

            // Difficulty dialog
            if (showDifficultyDialog) {
                DifficultyDialog { difficulty ->
                    cardsViewModel.selectDifficulty(difficulty)
                }
            }

            // Session summary
            if (showSummary) {
                SessionSummary(
                    correctAnswers = correctAnswers,
                    totalCards = totalCards,
                    onRestartSession = { cardsViewModel.resetSession() },
                    onExitSession = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun CardContent(
    card: Card,
    selectedAnswer: Int?,
    isAnswerRevealed: Boolean,
    onAnswerSelected: (Int) -> Unit,
    onNextCard: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Topico banner
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text(
                text = card.topico,
                modifier = Modifier.padding(12.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold
            )
        }

        // Pergunta
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Text(
                text = card.pergunta,
                modifier = Modifier.padding(16.dp),
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
        }

        // Alternativas
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            card.alternativas.forEachIndexed { idx, alt ->
                AlternativeOption(
                    text = alt,
                    isSelected = selectedAnswer == idx,
                    isCorrect = isAnswerRevealed && idx == card.resposta,
                    isIncorrect = isAnswerRevealed && selectedAnswer == idx && idx != card.resposta,
                    isAnswerRevealed = isAnswerRevealed,
                    onClick = { if (!isAnswerRevealed) onAnswerSelected(idx) }
                )
            }
        }

        // Próxima carta ou resumo
        if (isAnswerRevealed) {
            Button(
                onClick = onNextCard,
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
            ) {
                Text("Próxima Carta", fontSize = 16.sp)
            }
        }

        // Localização
        card.localizacao?.let {
            Text(
                text = "📍 $it",
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun AlternativeOption(
    text: String,
    isSelected: Boolean,
    isCorrect: Boolean,
    isIncorrect: Boolean,
    isAnswerRevealed: Boolean,
    onClick: () -> Unit
) {
    val bg by animateColorAsState(
        targetValue = when {
            isCorrect -> Color(0xFF4CAF50).copy(alpha = 0.15f)
            isIncorrect -> Color(0xFFE53935).copy(alpha = 0.15f)
            isSelected && !isAnswerRevealed -> MaterialTheme.colorScheme.primaryContainer
            else -> Color.Transparent
        },
        animationSpec = tween(300)
    )
    val border by animateColorAsState(
        targetValue = when {
            isCorrect -> Color(0xFF4CAF50)
            isIncorrect -> Color(0xFFE53935)
            isSelected && !isAnswerRevealed -> MaterialTheme.colorScheme.primary
            else -> Color.Gray.copy(alpha = 0.5f)
        },
        animationSpec = tween(300)
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(if (isSelected||isAnswerRevealed) 2.dp else 1.dp, border), RoundedCornerShape(8.dp))
            .background(bg, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = text, fontWeight = if (isSelected||isCorrect) FontWeight.Bold else FontWeight.Normal)
            if (isAnswerRevealed) {
                Icon(
                    imageVector = if (isCorrect) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = if (isCorrect) "Correto" else "Incorreto",
                    tint = if (isCorrect) Color(0xFF4CAF50) else Color(0xFFE53935)
                )
            }
        }
    }
}
