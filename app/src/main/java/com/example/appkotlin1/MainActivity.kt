package com.example.appkotlin1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.appkotlin1.ui.theme.Appkotlin1Theme
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()

        enableEdgeToEdge()
        setContent {
            Appkotlin1Theme {
                var currentUser by remember { mutableStateOf(auth.currentUser) }

                if (currentUser == null) {
                    LoginScreen(
                        onLoginSuccess = { currentUser = auth.currentUser }
                    )
                } else {
                    CruceFerroviarioScreen(
                        onLogout = {
                            auth.signOut()
                            currentUser = null
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var mensajeError by remember { mutableStateOf("") }
    val auth = FirebaseAuth.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Acceso IoT - Cruce Ferroviario",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnSuccessListener { onLoginSuccess() }
                        .addOnFailureListener { e ->
                            mensajeError = "Error: ${e.message}"
                        }
                } else {
                    mensajeError = "Por favor completa todos los campos"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Iniciar Sesión")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = {
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnSuccessListener { onLoginSuccess() }
                        .addOnFailureListener { e ->
                            mensajeError = "Error registro: ${e.message}"
                        }
                } else {
                    mensajeError = "Ingresa correo y contraseña para registrarte"
                }
            }
        ) {
            Text("¿No tienes cuenta? Regístrate aquí")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (mensajeError.isNotEmpty()) {
            Text(
                text = mensajeError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CruceFerroviarioScreen(onLogout: () -> Unit) {
    var claveConfirmacion by remember { mutableStateOf("") }
    val claveCorrecta = "1234"

    val ciudades = listOf("Calama", "Antofagasta", "Santiago")
    var selectedCiudad by remember { mutableStateOf("Calama") }

    val callesPorCiudad = mapOf(
        "Calama" to listOf("Granaderos", "Chorrillos", "Balmaceda", "Avenida Cobre", "Vivar", "Abaroa"),
        "Antofagasta" to listOf("Avenida Angamos", "Prat", "Esmeralda", "Uribe", "Pedro Aguirre Cerda"),
        "Santiago" to listOf("Alameda", "Providencia", "Apoquindo", "Vicuña Mackenna", "Moneda")
    )

    val callesSeleccionadas = remember { mutableStateMapOf<String, Boolean>() }
    var mensajeEstado by remember { mutableStateOf("Sistema en espera") }

    val database = FirebaseDatabase.getInstance("https://appfirebase-631b4-default-rtdb.firebaseio.com/")
    val myRef = database.getReference("cruce_ferroviario")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Control Cruce Ferroviario - Chile") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Salir", color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = claveConfirmacion,
                        onValueChange = { claveConfirmacion = it },
                        label = { Text("Clave de Autorización") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = "Mapa Ciudad",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Mapa Cruce ($selectedCiudad)",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (claveConfirmacion.isEmpty()) {
                                    mensajeEstado = "Ingrese clave de autorización"
                                } else if (claveConfirmacion != claveCorrecta) {
                                    mensajeEstado = "Clave incorrecta: Usuario no autorizado"
                                } else {
                                    val callesActivas = callesSeleccionadas.filter { it.value }.keys.toList()
                                    val datosControl = mapOf(
                                        "autorizado" to true,
                                        "ciudad" to selectedCiudad,
                                        "callesAfectadas" to callesActivas,
                                        "timestamp" to System.currentTimeMillis()
                                    )

                                    myRef.push().setValue(datosControl)
                                        .addOnSuccessListener {
                                            mensajeEstado = "¡Activación autorizada y guardada!"
                                        }
                                        .addOnFailureListener { e ->
                                            mensajeEstado = "Error: ${e.message}"
                                        }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Aceptar")
                        }

                        OutlinedButton(
                            onClick = {
                                claveConfirmacion = ""
                                callesSeleccionadas.clear()
                                mensajeEstado = "Operación cancelada"
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Rechazar")
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Ciudad de Chile",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            ciudades.forEach { ciudad ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    RadioButton(
                                        selected = (ciudad == selectedCiudad),
                                        onClick = {
                                            selectedCiudad = ciudad
                                            callesSeleccionadas.clear()
                                        }
                                    )
                                    Text(
                                        text = ciudad,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp).fillMaxHeight()) {
                            Text(
                                text = "Calles ($selectedCiudad)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            val listaCalles = callesPorCiudad[selectedCiudad] ?: emptyList()

                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                items(listaCalles) { calle ->
                                    val isChecked = callesSeleccionadas[calle] == true
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp)
                                    ) {
                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = { checked ->
                                                callesSeleccionadas[calle] = checked
                                            }
                                        )
                                        Text(
                                            text = calle,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = mensajeEstado,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}