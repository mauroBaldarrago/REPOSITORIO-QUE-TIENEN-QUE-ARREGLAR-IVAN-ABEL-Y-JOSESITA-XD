package com.example.ihc_proyecto1theme

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
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
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.max

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
    var lugarDetalle by rememberSaveable { mutableStateOf("") }

    // Se guardan solo los IDs (rememberSaveable) y los objetos se buscan en las listas:
    // así la selección no se pierde si la pantalla se recrea al volver de la cámara.
    var sedeIdSel by rememberSaveable { mutableStateOf<Int?>(null) }
    var pabellonIdSel by rememberSaveable { mutableStateOf<Int?>(null) }
    var facultadIdSel by rememberSaveable { mutableStateOf<Int?>(null) }
    val sedeSeleccionada = sedes.firstOrNull { it.id == sedeIdSel }
    val pabellonSeleccionado = pabellones.firstOrNull { it.id == pabellonIdSel }
    val facultadSeleccionada = facultades.firstOrNull { it.id == facultadIdSel }

    // FOTO: se guarda la RUTA del archivo (texto) con rememberSaveable.
    // Antes se guardaba un Uri con remember a secas, que se perdía cuando Android
    // recreaba la pantalla o cerraba la app mientras estaba abierta la cámara.
    var rutaFoto by rememberSaveable { mutableStateOf<String?>(null) }
    var rutaFotoPendiente by rememberSaveable { mutableStateOf<String?>(null) }
    val fotoUri: Uri? = rutaFoto
        ?.let { File(it) }
        ?.takeIf { it.exists() && it.length() > 0 }
        ?.let { Uri.fromFile(it) }

    var enviando by remember { mutableStateOf(false) }
    var errorMensaje by remember { mutableStateOf("") }
    var exito by rememberSaveable { mutableStateOf(false) }
    var pasoActual by rememberSaveable { mutableStateOf(0) }

    // Galería: se copia la imagen a un archivo propio de la app para no depender
    // de un permiso temporal que puede vencer.
    val selectorFoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val copia = copiarACache(context, uri)
                if (copia != null) {
                    rutaFoto = copia.absolutePath
                    errorMensaje = ""
                } else {
                    errorMensaje = "No se pudo cargar la imagen elegida. Intenta con otra."
                }
            }
        }
    }

    // Cámara: la foto se guarda en el archivo que se creó antes de abrirla.
    val tomarFoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { guardada ->
        val ruta = rutaFotoPendiente
        val archivo = ruta?.let { File(it) }
        when {
            guardada && archivo != null && archivo.exists() && archivo.length() > 0 -> {
                rutaFoto = ruta
                errorMensaje = ""
            }
            guardada -> {
                errorMensaje = "La fotografía no se pudo guardar. Inténtalo de nuevo."
            }
            else -> {
                errorMensaje = "No se tomó la fotografía. Puedes intentarlo nuevamente."
            }
        }
        rutaFotoPendiente = null
    }

    LaunchedEffect(Unit) {
        try {
            sedes = repo.sedes()
            facultades = repo.facultades()
            pabellones = repo.pabellones()
            if (sedeIdSel == null) {
                perfil.sedeId?.let { idSede ->
                    if (sedes.any { it.id == idSede }) sedeIdSel = idSede
                }
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
            // Indicador sencillo del asistente de tres pasos
            Text("Paso ${pasoActual + 1} de 3", fontWeight = FontWeight.Bold, color = Primario)
            androidx.compose.material3.LinearProgressIndicator(
                progress = { (pasoActual + 1) / 3f },
                modifier = Modifier.fillMaxWidth(),
                color = Primario,
                trackColor = Primario.copy(alpha = 0.12f)
            )
            Text(
                text = listOf("Información del objeto", "Ubicación del hallazgo", "Confirmar reporte")[pasoActual],
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F1A1B)
            )

            if (pasoActual == 0) {
                Text("Fotografía del objeto", fontWeight = FontWeight.SemiBold, color = Color(0xFF1F1A1B))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Primario.copy(alpha = 0.35f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (fotoUri != null) {
                        AsyncImage(
                            model = fotoUri,
                            contentDescription = "Fotografía del objeto",
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
                            Text("Agrega una fotografía del objeto", color = Primario, fontWeight = FontWeight.Medium)
                            Text("Puedes tomarla ahora o elegirla de tu galería", style = MaterialTheme.typography.labelSmall, color = Neutro)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            try {
                                val carpeta = File(context.cacheDir, "images").apply { mkdirs() }
                                val archivo = File.createTempFile("reporte_objeto_", ".jpg", carpeta)
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    archivo
                                )
                                // Se anota la ruta ANTES de abrir la cámara (queda guardada aunque
                                // Android recree la pantalla mientras se toma la foto).
                                rutaFotoPendiente = archivo.absolutePath
                                tomarFoto.launch(uri)
                            } catch (e: Exception) {
                                Log.e("Reportar", "No se pudo abrir la cámara", e)
                                errorMensaje = "No se pudo abrir la cámara. Intenta elegir una foto de la galería."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Primario),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tomar foto")
                    }

                    Button(
                        onClick = {
                            selectorFoto.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Neutro),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Galería")
                    }
                }

                if (fotoUri != null) {
                    Text(
                        text = "Fotografía adjunta",
                        color = Exito,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
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

            }

            if (pasoActual == 1) {
                Text("¿Dónde lo encontraste?", fontWeight = FontWeight.SemiBold, color = Color(0xFF1F1A1B))

                DropdownSelector(
                    label = "Sede / Área *",
                    opciones = sedes,
                    seleccionado = sedeSeleccionada,
                    textoItem = { it.nombre },
                    onSeleccionar = {
                        sedeIdSel = it.id
                        pabellonIdSel = null
                    }
                )

                if (pabellonesFiltrados.isNotEmpty()) {
                    DropdownSelector(
                        label = "Pabellón (opcional)",
                        opciones = pabellonesFiltrados,
                        seleccionado = pabellonSeleccionado,
                        textoItem = { it.nombre },
                        onSeleccionar = { pabellonIdSel = it.id }
                    )
                }

                DropdownSelector(
                    label = "Facultad cercana (opcional)",
                    opciones = facultades,
                    seleccionado = facultadSeleccionada,
                    textoItem = { it.nombre },
                    onSeleccionar = { facultadIdSel = it.id }
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

            }

            if (pasoActual == 2) {
                Text("Revisa la información antes de registrar el hallazgo.", color = Neutro)
                androidx.compose.material3.Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("${categoriaSeleccionada.texto}: $titulo", fontWeight = FontWeight.Bold)
                        if (descripcion.isNotBlank()) Text(descripcion)
                        Text("Sede: ${sedeSeleccionada?.nombre ?: "No seleccionada"}")
                        pabellonSeleccionado?.let { Text("Pabellón: ${it.nombre}") }
                        facultadSeleccionada?.let { Text("Facultad: ${it.nombre}") }
                        if (lugarDetalle.isNotBlank()) Text("Referencia: $lugarDetalle")
                        Text(if (fotoUri != null) "Fotografía adjunta" else "Sin fotografía")
                    }
                }
            }

            if (errorMensaje.isNotEmpty()) {
                Banner(texto = errorMensaje, color = ErrorRojo, icono = "⚠")
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                if (pasoActual > 0) {
                    Button(
                        onClick = { errorMensaje = ""; pasoActual-- },
                        colors = ButtonDefaults.buttonColors(containerColor = Neutro),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).heightIn(min = 52.dp)
                    ) { Text("Anterior", color = Color.White) }
                }
                Button(
                    onClick = {
                        if (pasoActual == 0) {
                            if (titulo.trim().length < 3) {
                                errorMensaje = "Ingresa un título descriptivo (mínimo 3 caracteres)."
                                return@Button
                            }
                            errorMensaje = ""
                            pasoActual = 1
                            return@Button
                        }
                        if (pasoActual == 1) {
                            if (sedeSeleccionada == null) {
                                errorMensaje = "Selecciona la sede donde encontraste el objeto."
                                return@Button
                            }
                            errorMensaje = ""
                            pasoActual = 2
                            return@Button
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
                    modifier = Modifier.weight(1f).heightIn(min = 52.dp)
                ) {
                    if (enviando) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Registrando...", color = Color.White, fontWeight = FontWeight.SemiBold)
                    } else {
                        Text(if (pasoActual < 2) "Continuar" else "Registrar hallazgo", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ---------------------------------------------------------------------
//  Manejo de imágenes
// ---------------------------------------------------------------------

/** Copia una imagen elegida de la galería a un archivo propio de la app. */
private suspend fun copiarACache(context: Context, uri: Uri): File? = withContext(Dispatchers.IO) {
    try {
        val carpeta = File(context.cacheDir, "images").apply { mkdirs() }
        val destino = File.createTempFile("reporte_galeria_", ".jpg", carpeta)
        context.contentResolver.openInputStream(uri)?.use { entrada ->
            destino.outputStream().use { salida -> entrada.copyTo(salida) }
        } ?: return@withContext null
        if (destino.length() > 0) destino else null
    } catch (e: Exception) {
        Log.e("Reportar", "Error al copiar la imagen de la galería", e)
        null
    }
}

/** Reduce la imagen a 1280 px como máximo, corrige la rotación y la convierte a JPEG. */
private suspend fun convertirUriAJpeg(context: Context, uri: Uri): ByteArray? = withContext(Dispatchers.IO) {
    try {
        val maxDimension = 1280
        val bitmap: Bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = if (uri.scheme == "file" && uri.path != null) {
                ImageDecoder.createSource(File(uri.path!!))
            } else {
                ImageDecoder.createSource(context.contentResolver, uri)
            }
            // ImageDecoder aplica solo la rotación EXIF y permite reducir el tamaño al decodificar
            ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                val mayor = max(info.size.width, info.size.height)
                if (mayor > maxDimension) {
                    val escala = maxDimension.toFloat() / mayor
                    decoder.setTargetSize(
                        (info.size.width * escala).toInt().coerceAtLeast(1),
                        (info.size.height * escala).toInt().coerceAtLeast(1)
                    )
                }
            }
        } else {
            decodificarAndroidAntiguo(context, uri, maxDimension) ?: return@withContext null
        }

        ByteArrayOutputStream().use { salida ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, salida)
            salida.toByteArray()
        }
    } catch (e: Exception) {
        Log.e("Reportar", "Error al convertir imagen a JPEG", e)
        null
    }
}

/** Android 7 y 8 (sin ImageDecoder): reduce, rota según EXIF y limita el tamaño. */
private fun decodificarAndroidAntiguo(context: Context, uri: Uri, maxDimension: Int): Bitmap? {
    val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, limites) }

    var muestra = 1
    while (max(limites.outWidth, limites.outHeight) / muestra > maxDimension * 2) muestra *= 2

    val opciones = BitmapFactory.Options().apply { inSampleSize = muestra }
    var bitmap = context.contentResolver.openInputStream(uri)?.use {
        BitmapFactory.decodeStream(it, null, opciones)
    } ?: return null

    val grados = context.contentResolver.openInputStream(uri)?.use { entrada ->
        when (ExifInterface(entrada).getAttributeInt(
            ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
        )) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    } ?: 0
    if (grados != 0) {
        val matriz = Matrix().apply { postRotate(grados.toFloat()) }
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matriz, true)
    }

    val mayor = max(bitmap.width, bitmap.height)
    if (mayor > maxDimension) {
        val escala = maxDimension.toFloat() / mayor
        bitmap = Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * escala).toInt().coerceAtLeast(1),
            (bitmap.height * escala).toInt().coerceAtLeast(1),
            true
        )
    }
    return bitmap
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