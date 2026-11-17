package com.example.ihc_proyecto1theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitudesScreen(onVolver: () -> Unit) {
    val repo = remember { ObjetosRepository() }
    val scope = rememberCoroutineScope()

    var lista by remember { mutableStateOf<List<SolicitudConObjeto>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf("") }

    fun cargar() {
        cargando = true
        error = ""
        scope.launch {
            try {
                lista = repo.misSolicitudes()
            } catch (e: Exception) {
                error = "No se pudieron cargar tus solicitudes."
            } finally {
                cargando = false
            }
        }
    }

    LaunchedEffect(Unit) { cargar() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis solicitudes", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { cargar() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Fondo)
            )
        },
        containerColor = Fondo
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                cargando -> {
                    CircularProgressIndicator(
                        color = Primario,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                error.isNotEmpty() -> {
                    Box(modifier = Modifier.padding(24.dp).align(Alignment.Center)) {
                        Banner(texto = error, color = ErrorRojo, icono = "⚠")
                    }
                }
                lista.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = Neutro.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Aún no tienes solicitudes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Cuando reclames un objeto desde el tablón, aquí podrás ver el estado de verificación y tu código de recojo.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Neutro,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(lista, key = { it.solicitud.id }) { item ->
                            TarjetaSolicitud(item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaSolicitud(item: SolicitudConObjeto) {
    val sol = item.solicitud
    val obj = item.objeto
    val categoria = CategoriaObjeto.desde(obj?.categoria)

    val (textoEstado, colorEstado, iconoEstado) = when (sol.estado) {
        "aprobada", "lista_recojo" -> Triple("Aprobada para recojo", Exito, Icons.Default.CheckCircle)
        "rechazada" -> Triple("Rechazada", ErrorRojo, Icons.Default.ErrorOutline)
        "entregada" -> Triple("Entregada", Neutro, Icons.Default.Inventory2)
        else -> Triple("En verificación", Alerta, Icons.Default.HourglassTop)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = categoria.icono,
                        contentDescription = null,
                        tint = Primario,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = categoria.texto,
                        style = MaterialTheme.typography.labelMedium,
                        color = Neutro
                    )
                }

                Surface(
                    color = colorEstado.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(iconoEstado, contentDescription = null, tint = colorEstado, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = textoEstado,
                            color = colorEstado,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = obj?.titulo ?: "Objeto reclamado",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )

            if (sol.intentos > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Intentos de validación usados: ${sol.intentos}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Neutro
                )
            }

            // Bloque destacado con el código y punto de recojo si ya fue aprobada
            if (!sol.codigoRecojo.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Primario.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.QrCode2, contentDescription = null, tint = Primario)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Código de recojo en caseta:", style = MaterialTheme.typography.labelSmall, color = Neutro)
                            Text(
                                text = sol.codigoRecojo,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Primario
                            )
                        }
                    }

                    if (!sol.lugarRecojo.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Place, contentDescription = null, tint = Neutro, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Punto de entrega: ${sol.lugarRecojo}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF1F2937),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    if (!sol.expiraEn.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Válido hasta: ${sol.expiraEn.take(10)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Alerta
                        )
                    }
                }
            }
        }
    }
}
