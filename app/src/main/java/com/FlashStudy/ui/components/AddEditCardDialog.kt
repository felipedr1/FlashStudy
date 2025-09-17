// ui/components/AddEditCardDialog.kt (atualizado)
package com.FlashStudy.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.FlashStudy.data.model.Card
import com.FlashStudy.ui.viewmodel.LocationViewModel
import com.FlashStudy.utils.LocationService
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.foundation.layout.Arrangement


@Composable
fun AddEditCardDialog(
    card: Card?,
    locationViewModel: LocationViewModel,
    onDismiss: () -> Unit,
    onConfirm: (topico: String, pergunta: String, alternativas: List<String>, resposta: Int, localizacao: String?) -> Unit
) {
    val context = LocalContext.current
    val locationService = remember { LocationService(context) }
    val savedLocations by locationViewModel.locations.collectAsState()

    var topico by remember { mutableStateOf(card?.topico ?: "") }
    var pergunta by remember { mutableStateOf(card?.pergunta ?: "") }
    var alternativas by remember {
        mutableStateOf(
            card?.alternativas?.toMutableList() ?: mutableListOf("", "", "", "")
        )
    }
    var respostaIndex by remember { mutableStateOf(card?.resposta ?: 0) }
    var localizacao by remember { mutableStateOf(card?.localizacao ?: "") }
    var isLoadingLocation by remember { mutableStateOf(false) }
    var locationStatus by remember { mutableStateOf("") }

    // Detecta localização quando o diálogo abre
    LaunchedEffect(Unit) {
        if (card == null) { // Apenas para novas cartas
            isLoadingLocation = true
            try {
                if (locationService.hasLocationPermission()) {
                    val (currentLat, currentLon) = locationService.getCurrentLocation() ?: return@LaunchedEffect

                    val nearbyLocation = savedLocations.find { savedLocation ->
                        LocationService.LocationUtils.isNearLocation(currentLat, currentLon, savedLocation)
                    }

                    if (nearbyLocation != null) {
                        localizacao = nearbyLocation.name
                        locationStatus = "Local detectado: ${nearbyLocation.name}"
                    } else {
                        localizacao = ""
                        locationStatus = "Não estamos em nenhum local salvo"
                    }
                } else {
                    locationStatus = "Permissão de localização necessária"
                    localizacao = ""
                }
            } catch (e: Exception) {
                locationStatus = "Erro ao detectar localização"
                localizacao = ""
            } finally {
                isLoadingLocation = false
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (card == null) "Adicionar Nova Carta" else "Editar Carta",
                    style = MaterialTheme.typography.headlineSmall
                )

                OutlinedTextField(
                    value = topico,
                    onValueChange = { topico = it },
                    label = { Text("Tópico") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                OutlinedTextField(
                    value = pergunta,
                    onValueChange = { pergunta = it },
                    label = { Text("Pergunta") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                Text(
                    text = "Tipo: Múltipla Escolha",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text("Alternativas")

                alternativas.forEachIndexed { index, alternativa ->
                    OutlinedTextField(
                        value = alternativa,
                        onValueChange = { newValue ->
                            val newAlternativas = alternativas.toMutableList()
                            newAlternativas[index] = newValue
                            alternativas = newAlternativas
                        },
                        label = { Text("Alternativa ${index + 1}") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        trailingIcon = {
                            if (index == respostaIndex) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Resposta Correta",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    )
                }

                Text("Resposta Correta")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    alternativas.forEachIndexed { index, _ ->
                        FilterChip(
                            onClick = { respostaIndex = index },
                            label = { Text("Alternativa ${index + 1}") },
                            selected = respostaIndex == index,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { /* TODO: Implementar seleção de imagem */ }, enabled = false) {
                        Icon(Icons.Default.Image, contentDescription = "Adicionar Imagem")
                    }
                    IconButton(onClick = { /* TODO: Implementar gravação de áudio */ }, enabled = false) {
                        Icon(Icons.Default.Mic, contentDescription = "Gravar Áudio")
                    }
                }

                // Campo de localização com status
                Column {
                    OutlinedTextField(
                        value = localizacao,
                        onValueChange = { localizacao = it },
                        label = { Text("Localização") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        leadingIcon = {
                            if (isLoadingLocation) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else if (localizacao.isNotEmpty()) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Local detectado",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Sem local",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                    )

                    if (locationStatus.isNotEmpty()) {
                        Text(
                            text = locationStatus,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onConfirm(
                                topico,
                                pergunta,
                                alternativas,
                                respostaIndex,
                                localizacao.ifEmpty { null }
                            )
                        },
                        enabled = topico.isNotBlank() &&
                                pergunta.isNotBlank() &&
                                alternativas.all { it.isNotBlank() }
                    ) {
                        Text(if (card == null) "Adicionar" else "Salvar")
                    }
                }
            }
        }
    }
}