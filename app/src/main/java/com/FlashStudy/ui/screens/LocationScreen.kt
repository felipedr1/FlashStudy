// ui/screens/LocationScreen.kt (atualizado)
package com.FlashStudy.ui.screens

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.FlashStudy.data.model.Location
import com.FlashStudy.ui.viewmodel.LocationViewModel
import com.FlashStudy.utils.LocationService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationScreen(
    navController: NavController,
    viewModel: LocationViewModel = viewModel()
) {
    val locations by viewModel.locations.collectAsState()
    val isDialogOpen by viewModel.isDialogOpen.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val locationService = remember { LocationService(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Locais Favoritos") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomBar(
                onSaveLocationClick = {
                    if (!locationService.hasLocationPermission()) {
                        locationService.requestLocationPermission(context as Activity)
                    } else {
                        viewModel.showDialog()
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (locations.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhuma localização salva",
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    items(locations) { location ->
                        LocationItem(
                            location = location,
                            onDeleteClick = { viewModel.deleteLocation(location) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            if (isDialogOpen) {
                AddLocationDialog(
                    onDismiss = { viewModel.hideDialog() },
                    onConfirm = { name ->
                        scope.launch {
                            try {
                                val (latitude, longitude) = locationService.getCurrentLocation() ?: return@launch
                                viewModel.addLocation(name, latitude, longitude)
                                viewModel.hideDialog()
                            } catch (e: Exception) {
                                // Mostrar erro
                                viewModel.hideDialog()
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun LocationItem(
    location: Location,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = location.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Lat: ${String.format("%.6f", location.latitude)}, Long: ${String.format("%.6f", location.longitude)}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Excluir",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun BottomBar(onSaveLocationClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp
    ) {
        Button(
            onClick = onSaveLocationClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Salvar local atual",
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun AddLocationDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var locationName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nome do Local") },
        text = {
            OutlinedTextField(
                value = locationName,
                onValueChange = { locationName = it },
                label = { Text("Digite o nome do local") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(locationName) },
                enabled = locationName.isNotBlank()
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}