package com.example.appkotlin1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.appkotlin1.ui.theme.Appkotlin1Theme
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()
        setContent {
            Appkotlin1Theme {
                CruceFerroviarioScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CruceFerroviarioScreen() {
    var estadoPeaton by remember { mutableStateOf("") }
    var barreraActiva by remember { mutableStateOf(false) }
    var mensajeEstado by remember { mutableStateOf("Sistema en espera") }

    val database = FirebaseDatabase.getInstance("https://appfirebase-631b4-default-rtdb.firebaseio.com/")
    val myRef = database.getReference("cruce_ferroviario")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Control Cruce Ferroviario IoT") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Tarjeta contenedora para organizar los elementos de manera limpia
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Panel de Control",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = estadoPeaton,
                        onValueChange = { estadoPeaton = it },
                        label = { Text("Estado o evento peatonal") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Activar Barrera / LED")
                        Switch(
                            checked = barreraActiva,
                            onCheckedChange = { barreraActiva = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (estadoPeaton.isEmpty()) {
                                mensajeEstado = "Por favor ingrese un valor"
                            } else {
                                val datosControl = mapOf(
                                    "evento" to estadoPeaton,
                                    "barreraAbierta" to barreraActiva,
                                    "timestamp" to System.currentTimeMillis()
                                )

                                myRef.push().setValue(datosControl)
                                    .addOnSuccessListener {
                                        mensajeEstado = "¡Dato guardado con éxito en Firebase!"
                                    }
                                    .addOnFailureListener { e: Exception ->
                                        mensajeEstado = "Error al guardar: ${e.message}"
                                    }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Guardar Datos en Firebase")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Estado del sistema inferior
            Text(
                text = mensajeEstado,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}