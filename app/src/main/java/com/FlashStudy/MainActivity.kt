package com.FlashStudy

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.FlashStudy.data.database.BaralhoService
import com.FlashStudy.data.database.SimpleService
import com.FlashStudy.ui.navigation.AppNavHost
import com.FlashStudy.ui.theme.AnkiAppAndroidTheme
import com.FlashStudy.ui.viewmodel.BaralhoViewModel
import com.FlashStudy.ui.viewmodel.EditBaralhoViewModel
import com.FlashStudy.ui.viewmodel.LocationViewModel
import com.FlashStudy.ui.viewmodel.UsuarioViewModel
import kotlinx.coroutines.launch

// MainActivity.kt (atualizado para lidar com permissões)
class MainActivity : ComponentActivity() {

    private val baralhoViewModel: BaralhoViewModel by viewModels()
    private val locationViewModel: LocationViewModel by viewModels()
    private val editBaralhoViewModel: EditBaralhoViewModel by viewModels()
    private val usuarioViewModel: UsuarioViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            Log.d("SimpleService", "TESTE ==================================================")
            try {
                val getBaralhos = BaralhoService.getAll();
                val resposta = SimpleService.hello()
                Log.d("BaralhoService", "Resposta do backend: $getBaralhos")
            } catch (e: Exception) {
                Log.e("BaralhoService", "Falha ao chamar backend", e)
            }
        }

        setContent {
            AnkiAppAndroidTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavHost(
                        navController = navController,
                        baralhoViewModel = baralhoViewModel,
                        locationViewModel = locationViewModel,
                        editBaralhoViewModel = editBaralhoViewModel,
                        usuarioViewModel = usuarioViewModel
                    )
                }
            }
        }
    }
}