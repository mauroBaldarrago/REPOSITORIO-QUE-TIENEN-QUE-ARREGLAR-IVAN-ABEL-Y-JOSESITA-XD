package com.example.ihc_proyecto1theme

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.ihc_proyecto1theme.model.FacultadModel
import com.example.ihc_proyecto1theme.model.ObjetoModel
import com.example.ihc_proyecto1theme.model.PabellonModel
import com.example.ihc_proyecto1theme.model.PerfilModel
import com.example.ihc_proyecto1theme.model.SedeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ReportarScreen(
    perfil: PerfilModel,
    onVolver: () -> Unit,
    onReporteExitoso: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { ObjetosRepository() }

    var sedes by remember { mutableStateOf<List<SedeModel>>(emptyList()) }
    var facultades by remember { mutableStateOf<List<FacultadModel>>(emptyList()) }
    var pabellones by remember { mutableStateOf<List<PabellonModel>>(emptyList()) }

    var titulo by rememberSaveable { mutableStateOf("") }
    var descripcion by rememberSaveable { mutableStateOf("") }
    var categoriaSeleccionada by rememberSaveable { mutableStateOf(CategoriaObjeto.MOCHILAS_BOLSOS) }
    var sedeSeleccionada by remember { mutableStateOf<SedeModel?>(null) }
    var pabellonSeleccionado by remember { mutableStateOf<PabellonModel?>(null) }
    var facultadSeleccionada by remember { mutableStateOf<FacultadModel?>(null) }
    var lugarDetalle by rememberSaveable { mutableStateOf("") }
    var fotoUri by remember { mutableStateOf<Uri?>(null) }

    var enviando by remember { mutableStateOf(false) }
    var errorMensaje by remember { mutableStateOf("") }
    var exito by remember { mutableStateOf(false) }

    val selectorFoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            fotoUri = uri
            errorMensaje = ""
        }
    }

    LaunchedEffect(Unit) {
        try {
            sedes = repo.sedes()
            facultades = repo.facultades()
            pabellones = repo.pabellones()
            perfil.sedeId?.let { idSede ->
                sedeSeleccionada = sedes.firstOrNull { it.id == idSede }
            }
        } catch (e: Exception) {
            Log.e("Reportar", "Error cargando catálogos", e)
            errorMensaje = "No se pudieron cargar los catálogos de sedes y facultades."
        }
    }

    val pabellonesFiltrados = remember(sedeSeleccionada, pabellones) {
        sedeSeleccionada?.let { sede -> pabellones.filter { it.sedeId == sede.id } } ?: emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reportar objeto hallado", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Fondo)
            )
        },
        containerColor = Fondo
    ) { padding ->
        if (exito) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Exito,
                    modifier = Modifier.size(72.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "¡Reporte registrado!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Acércate al módulo de vigilancia de tu sede o pabellón para entregar el objeto y que sea publicado en el tablón.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Neutro,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onReporteExitoso,
                    colors = ButtonDefaults.buttonColors(containerColor = Primario),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                ) {
                    Text("Volver al tablón", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Fotografía del objeto", fontWeight = FontWeight.SemiBold, color = Color(0xFF1F1A1B))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, Primario.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                    .clickable {
                        selectorFoto.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                if (fotoUri != null) {
                    AsyncImage(
                        model = fotoUri,
                        contentDescription = "Foto seleccionada",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = null,
                            tint = Primario,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Toca para seleccionar una foto", color = Primario, fontWeight = FontWeight.Medium)
                        Text("Ayuda al dueño a reconocerlo rápido", style = MaterialTheme.typography.labelSmall, color = Neutro)
                    }
                }
            }

            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("¿Qué encontraste? *") },
                placeholder = { Text("Ej. Mochila negra Lenovo, Carné universitario...") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Características (opcional)") },
                placeholder = { Text("Color, marca, estado o detalles visibles...") },
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Text("Categoría *", fontWeight = FontWeight.SemiBold, color = Color(0xFF1F1A1B))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CategoriaObjeto.entries.forEach { cat ->
                    val seleccionada = categoriaSeleccionada == cat
                    FilterChip(
                        selected = seleccionada,
                        onClick = { categoriaSeleccionada = cat },
                        label = { Text(cat.texto) },
                        leadingIcon = {
                            Icon(cat.icono, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Primario,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White
                        )
                    )
                }
            }

            Text("¿Dónde lo encontraste?", fontWeight = FontWeight.SemiBold, color = Color(0xFF1F1A1B))

            DropdownSelector(
                label = "Sede / Área *",
                opciones = sedes,
                seleccionado = sedeSeleccionada,
                textoItem = { it.nombre },
                onSeleccionar = {
                    sedeSeleccionada = it
                    pabellonSeleccionado = null
                }
            )

            if (pabellonesFiltrados.isNotEmpty()) {
                DropdownSelector(
                    label = "Pabellón (opcional)",
                    opciones = pabellonesFiltrados,
                    seleccionado = pabellonSeleccionado,
                    textoItem = { it.nombre },
                    onSeleccionar = { pabellonSeleccionado = it }
                )
            }

            DropdownSelector(
                label = "Facultad cercana (opcional)",
                opciones = facultades,
                seleccionado = facultadSeleccionada,
                textoItem = { it.nombre },
                onSeleccionar = { facultadSeleccionada = it }
            )

            OutlinedTextField(
                value = lugarDetalle,
                onValueChange = { lugarDetalle = it },
                label = { Text("Referencia exacta") },
                placeholder = { Text("Ej. Aula 204, bancas frente a biblioteca...") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            if (errorMensaje.isNotEmpty()) {
                Banner(texto = errorMensaje, color = ErrorRojo, icono = "⚠")
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = {
                    when {
                        titulo.trim().length < 3 -> {
                            errorMensaje = "Ingresa un título descriptivo (mínimo 3 caracteres)."
                            return@Button
                        }
                        sedeSeleccionada == null -> {
                            errorMensaje = "Selecciona la sede donde encontraste el objeto."
                            return@Button
                        }
                    }

                    errorMensaje = ""
                    enviando = true
                    scope.launch {
                        try {
                            val urlsFotos = mutableListOf<String>()
                            fotoUri?.let { uri ->
                                val bytesJpeg = convertirUriAJpeg(context, uri)
                                    ?: throw IllegalStateException("No se pudo procesar la imagen seleccionada.")
                                val urlPublica = repo.subirFotoObjeto(bytesJpeg, "image/jpeg")
                                urlsFotos.add(urlPublica)
                            }

                            val formatoIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                                timeZone = TimeZone.getTimeZone("UTC")
                            }
                            val ahoraIso = formatoIso.format(Date())

                            val nuevoObjeto = ObjetoModel(
                                titulo = titulo.trim(),
                                descripcion = descripcion.trim().ifEmpty { null },
                                categoria = categoriaSeleccionada.codigo,
                                facultadId = facultadSeleccionada?.id,
                                sedeId = sedeSeleccionada?.id,
                                pabellonId = pabellonSeleccionado?.id,
                                lugarDetalle = lugarDetalle.trim().ifEmpty { null },
                                lat = pabellonSeleccionado?.lat,
                                lng = pabellonSeleccionado?.lng,
                                fechaHallazgo = ahoraIso,
                                fechaRegistro = ahoraIso,
                                estado = EstadoObjeto.PENDIENTE_ENTREGA.codigo,
                                fotos = urlsFotos
                            )

                            repo.registrarObjeto(nuevoObjeto)
                            exito = true
                        } catch (e: Exception) {
                            Log.e("Reportar", "Error al guardar reporte", e)
                            val detalle = e.message.orEmpty()
                            errorMensaje = when {
                                detalle.contains("row-level security", ignoreCase = true) ->
                                    "No tienes permisos en la base de datos para registrar este objeto."
                                detalle.contains("Payload too large", ignoreCase = true) ->
                                    "La imagen es demasiado pesada (máximo 5 MB)."
                                else ->
                                    "No se pudo guardar el reporte. Revisa tu conexión e intenta de nuevo."
                            }
                        } finally {
                            enviando = false
                        }
                    }
                },
                enabled = !enviando,
                colors = ButtonDefaults.buttonColors(containerColor = Primario),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
            ) {
                if (enviando) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Registrando...", color = Color.White, fontWeight = FontWeight.SemiBold)
                } else {
                    Text("Registrar hallazgo", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private suspend fun convertirUriAJpeg(context: Context, uri: Uri): ByteArray? = withContext(Dispatchers.IO) {
    try {
        val bitmapOriginal = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        } else {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } ?: return@withContext null

        val maxDimension = 1280
        val ancho = bitmapOriginal.width
        val alto = bitmapOriginal.height
        val bitmapFinal = if (ancho > maxDimension || alto > maxDimension) {
            val escala = minOf(maxDimension.toFloat() / ancho, maxDimension.toFloat() / alto)
            Bitmap.createScaledBitmap(
                bitmapOriginal,
                (ancho * escala).toInt(),
                (alto * escala).toInt(),
                true
            )
        } else {
            bitmapOriginal
        }

        ByteArrayOutputStream().use { salida ->
            bitmapFinal.compress(Bitmap.CompressFormat.JPEG, 85, salida)
            salida.toByteArray()
        }
    } catch (e: Exception) {
        Log.e("Reportar", "Error al convertir imagen a JPEG", e)
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> DropdownSelector(
    label: String,
    opciones: List<T>,
    seleccionado: T?,
    textoItem: (T) -> String,
    onSeleccionar: (T) -> Unit
) {
    var expandido by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expandido,
        onExpandedChange = { expandido = !expandido }
    ) {
        OutlinedTextField(
            value = seleccionado?.let(textoItem) ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false }
        ) {
            opciones.forEach { item ->
                DropdownMenuItem(
                    text = { Text(textoItem(item)) },
                    onClick = {
                        onSeleccionar(item)
                        expandido = false
                    }
                )
            }
        }
    }
}