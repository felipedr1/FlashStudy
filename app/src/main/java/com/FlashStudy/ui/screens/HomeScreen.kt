// ui/screens/HomeScreen.kt

package com.FlashStudy.ui.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.FlashStudy.data.model.BaralhoBancoDados
import com.FlashStudy.ui.components.NearbyLocationIndicator
import com.FlashStudy.ui.viewmodel.BaralhoViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: BaralhoViewModel,
    onBaralhoClick: (String) -> Unit,
    onEditClick: (String) -> Unit,
    onLocationClick: () -> Unit
) {
    val baralhos by viewModel.baralhos.collectAsState()
    val isDialogOpen by viewModel.isDialogOpen.collectAsState()
    val currentNearbyLocation by viewModel.currentNearbyLocation.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Início", "Analytics", "Compartilhar")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FlashStudy") }, // Título corrigido
                actions = {
                    NearbyLocationIndicator(currentNearbyLocation)

                    IconButton(onClick = onLocationClick) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Locais Favoritos"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showDialog() }) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Baralho")
            }
        },
        // A Barra de Navegação foi adicionada aqui
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        icon = {
                            when (index) {
                                0 -> Icon(Icons.Filled.Home, contentDescription = title)
                                1 -> Icon(Icons.Filled.Analytics, contentDescription = title)
                                2 -> Icon(Icons.Filled.Share, contentDescription = title)
                            }
                        },
                        label = { Text(title) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (baralhos.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhum baralho encontrado.\nClique no + para adicionar.",
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    items(baralhos) { baralho ->
                        BaralhoItem(
                            baralho = baralho,
                            onClick = { onBaralhoClick(baralho.id) },
                            onEditClick = { onEditClick(baralho.id) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            if (isDialogOpen) {
                AddBaralhoDialog(
                    onDismiss = { viewModel.hideDialog() },
                    onConfirm = { titulo ->
                        viewModel.adicionarBaralho(titulo)
                    }
                )
            }
        }
    }
}

@Composable
fun BaralhoItem(
    baralho: BaralhoBancoDados,
    onClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = baralho.titulo,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.CenterStart)
            )

            IconButton(
                onClick = onEditClick,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar Baralho"
                )
            }
        }
    }
}

@Composable
fun AddBaralhoDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var tituloBaralho by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo Baralho") },
        text = {
            Column {
                Text("Digite o título do novo baralho:")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = tituloBaralho,
                    onValueChange = { tituloBaralho = it },
                    label = { Text("Título") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(tituloBaralho) },
                enabled = tituloBaralho.isNotBlank()
            ) {
                Text("Adicionar")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}