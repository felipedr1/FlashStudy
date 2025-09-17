package com.FlashStudy.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.FlashStudy.data.model.Difficulty

@Composable
fun DifficultyDialog(
    onDifficultySelected: (Difficulty) -> Unit
) {
    Dialog(
        onDismissRequest = { /* Não permite fechar sem selecionar */ },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Como foi esta pergunta?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "Sua resposta ajuda a ajustar o algoritmo de revisão",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Difficulty.values().forEach { difficulty ->
                        DifficultyOption(
                            difficulty = difficulty,
                            onClick = { onDifficultySelected(difficulty) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DifficultyOption(
    difficulty: Difficulty,
    onClick: () -> Unit
) {
    var isHovered by remember { mutableStateOf(false) }

    val backgroundColor = when (difficulty) {
        Difficulty.IMPOSSIBLE -> Color(0xFFE53935)
        Difficulty.HARD -> Color(0xFFFF9800)
        Difficulty.MEDIUM -> Color(0xFF2196F3)
        Difficulty.EASY -> Color(0xFF4CAF50)
    }

    val animatedBgColor by animateColorAsState(
        targetValue = if (isHovered) backgroundColor.copy(alpha = 0.15f) else Color.Transparent,
        animationSpec = tween(300)
    )

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = backgroundColor,
                shape = RoundedCornerShape(8.dp)
            ),
        shape = RoundedCornerShape(8.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(animatedBgColor)
                .padding(vertical = 16.dp, horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = difficulty.label,
                color = backgroundColor,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}