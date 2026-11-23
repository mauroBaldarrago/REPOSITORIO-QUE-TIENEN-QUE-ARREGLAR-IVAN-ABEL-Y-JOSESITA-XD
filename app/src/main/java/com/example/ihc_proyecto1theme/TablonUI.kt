package com.example.ihc_proyecto1theme

import android.util.Log
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import com.example.ihc_proyecto1theme.model.ObjetoModel
import com.example.ihc_proyecto1theme.model.PerfilModel
import kotlinx.coroutines.launch

private val TextoPrincipal = Color(0xFF1F2937)

@Composable
fun TablonScreen(
    navController: NavController,
    perfil: PerfilModel,
    onCerrarSesion: () -> Unit,
    vm: TablonViewModel = viewModel()
) {
    val rol = Rol.desde(perfil.rol)
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val lista by vm.lista.collectAsState()
    val aplicados by vm.aplicados.collectAsState()
    val catalogos by vm.catalogos.collectAsState()
    var mostrarFiltros by rememberSaveable { mutableStateOf(false) }
    var objetoSeleccionado by remember { mutableStateOf<ObjetoModel?>(null) }

    val irA: (String) -> Unit = { ruta ->
        scope.launch { drawerState.close() }
        navController.navigate(ruta)
    }

    FondoDecorado {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                PanelLateral(perfil = perfil, rol = rol, onIrA = irA, onCerrarSesion = onCerrarSesion)
            }
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.align(Alignment.CenterStart)
                        ) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = "Menú",
                                tint = TextoPrincipal,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        IconButton(
                            onClick = { vm.recargar() },
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Actualizar tablón",
                                tint = Primario
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Campaign,
                                contentDescription = null,
                                tint = Primario,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = "Tablón de avisos",
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextoPrincipal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        BadgedBox(
                            badge = {
                                if (aplicados.cantidadActivos > 0) {
                                    Badge { Text("${aplicados.cantidadActivos}") }
                                }
                            },
                            modifier = Modifier.padding(end = 6.dp, top = 6.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    vm.abrirFiltros()
                                    mostrarFiltros = true
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.heightIn(min = 48.dp)
                            ) {
                                Icon(Icons.Default.FilterList, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Filtros", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    Segmentado(
                        opciones = OrdenTablon.entries.map { it.texto },
                        seleccion = aplicados.orden.ordinal,
                        onClick = { vm.cambiarOrden(OrdenTablon.entries[it]) }
                    )

                    ChipsActivos(aplicados = aplicados, catalogos = catalogos, vm = vm)

                    Box(modifier = Modifier.weight(1f)) {
                        when (val estado = lista) {
                            EstadoLista.Cargando -> ListaEsqueleto()
                            is EstadoLista.Error -> EstadoError(
                                mensaje = estado.mensaje,
                                onReintentar = { vm.recargar() }
                            )
                            is EstadoLista.Datos -> {
                                if (estado.objetos.isEmpty()) {
                                    EstadoVacio(
                                        hayFiltros = aplicados.cantidadActivos > 0,
                                        onLimpiar = { vm.limpiarTodo() }
                                    )
                                } else {
                                    Column {
                                        Text(
                                            text = if (estado.objetos.size == 1) "1 resultado"
                                            else "${estado.objetos.size} resultados",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Neutro
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        LazyColumn(
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            items(estado.objetos, key = { it.id }) { objeto ->
                                                TarjetaObjeto(
                                                    objeto = objeto,
                                                    lugar = textoLugar(objeto, catalogos),
                                                    onClick = { objetoSeleccionado = objeto }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                BarraInferior(
                    onTablon = { },
                    onReportar = { navController.navigate("reportar") },
                    onSolicitudes = { navController.navigate("solicitudes") }
                )
            }
        }
    }

    if (mostrarFiltros) {
        HojaFiltros(vm = vm, catalogos = catalogos, onCerrar = { mostrarFiltros = false })
    }

    objetoSeleccionado?.let { obj ->
        HojaDetalleObjeto(
            objeto = obj,
            catalogos = catalogos,
            onCerrar = { objetoSeleccionado = null },
            onIrASolicitudes = {
                objetoSeleccionado = null
                navController.navigate("solicitudes")
            }
        )
    }
}

private fun textoLugar(objeto: ObjetoModel, catalogos: Catalogos): String {
    val pabellon = catalogos.pabellones.firstOrNull { it.id == objeto.pabellonId }?.nombre
    val partes = listOfNotNull(pabellon, objeto.lugarDetalle?.takeIf { it.isNotBlank() })
    if (partes.isNotEmpty()) return partes.joinToString(", ")
    return catalogos.sedes.firstOrNull { it.id == objeto.sedeId }?.nombre ?: "Ubicación no indicada"
}

// =====================================================================
//  HOJA DE DETALLE Y RECLAMO DEL OBJETO
// =====================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HojaDetalleObjeto(
    objeto: ObjetoModel,
    catalogos: Catalogos,
    onCerrar: () -> Unit,
    onIrASolicitudes: () -> Unit
) {
    val repo = remember { ObjetosRepository() }
    val scope = rememberCoroutineScope()
    val estadoHoja = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var enviando by remember { mutableStateOf(false) }
    var errorMensaje by remember { mutableStateOf("") }
    var solicitudEnviada by remember { mutableStateOf(false) }

    val categoria = CategoriaObjeto.desde(objeto.categoria)
    val estado = EstadoObjeto.desde(objeto.estado)
    val sedeNombre = catalogos.sedes.firstOrNull { it.id == objeto.sedeId }?.nombre
    val facultadNombre = catalogos.facultades.firstOrNull { it.id == objeto.facultadId }?.nombre
    val lugarTexto = textoLugar(objeto, catalogos)

    ModalBottomSheet(
        onDismissRequest = onCerrar,
        sheetState = estadoHoja,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(categoria.icono, contentDescription = null, tint = Primario, modifier = Modifier.size(20.dp))
                    Text(
                        text = categoria.texto,
                        style = MaterialTheme.typography.labelLarge,
                        color = Primario,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onCerrar) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar detalle")
                }
            }

            // Fotografía ampliada
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Primario.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                val foto = objeto.fotos.firstOrNull()
                if (foto != null) {
                    SubcomposeAsyncImage(
                        model = urlFoto(foto),
                        contentDescription = "Foto de ${objeto.titulo}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        loading = {
                            CircularProgressIndicator(color = Primario, modifier = Modifier.size(32.dp))
                        },
                        error = {
                            Icon(categoria.icono, contentDescription = null, tint = Primario, modifier = Modifier.size(48.dp))
                        }
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(categoria.icono, contentDescription = null, tint = Primario, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Sin fotografía adjunta", color = Neutro, style = MaterialTheme.typography.bodySmall)
                    }
                }

                // Marca visual para evitar confusiones con objetos que ya tienen un reclamo.
                if (estado == EstadoObjeto.RECLAMADO || estado == EstadoObjeto.ENTREGADO) {
                    Text(
                        text = if (estado == EstadoObjeto.ENTREGADO) "ENTREGADO" else "RECLAMADO",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .rotate(-18f)
                            .background(Color.Black.copy(alpha = 0.58f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 22.dp, vertical = 10.dp),
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = objeto.titulo,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextoPrincipal,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                InsigniaEstado(estado)
            }

            if (!objeto.descripcion.isNullOrBlank()) {
                Text(
                    text = objeto.descripcion,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextoPrincipal
                )
            }

            HorizontalDivider(color = Color(0xFFE5E7EB))

            // Detalles de ubicación y fecha
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Place, contentDescription = null, tint = Primario, modifier = Modifier.size(20.dp))
                    Text(
                        text = buildString {
                            if (sedeNombre != null) append("$sedeNombre · ")
                            append(lugarTexto)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextoPrincipal
                    )
                }

                if (facultadNombre != null) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.School, contentDescription = null, tint = Primario, modifier = Modifier.size(20.dp))
                        Text(
                            text = "Facultad: $facultadNombre",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextoPrincipal
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = Neutro, modifier = Modifier.size(20.dp))
                    Text(
                        text = "Reportado ${fechaRelativa(objeto.fechaHallazgo).lowercase()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Neutro
                    )
                }
            }

            if (errorMensaje.isNotEmpty()) {
                Banner(texto = errorMensaje, color = ErrorRojo, icono = "⚠")
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (solicitudEnviada) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Exito.copy(alpha = 0.10f), RoundedCornerShape(14.dp))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Exito)
                        Text(
                            text = "¡Solicitud enviada!",
                            color = Exito,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Text(
                        text = "Tu reclamo entró en verificación. Podrás ver tu código y punto de recojo en la pestaña Solicitudes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoPrincipal,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = onIrASolicitudes,
                        colors = ButtonDefaults.buttonColors(containerColor = Primario),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                    ) {
                        Text("Ir a mis solicitudes", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            } else {
                Button(
                    onClick = {
                        errorMensaje = ""
                        enviando = true
                        scope.launch {
                            try {
                                repo.solicitarObjeto(objeto.id)
                                solicitudEnviada = true
                            } catch (e: IllegalStateException) {
                                errorMensaje = e.message ?: "No se pudo procesar la solicitud."
                            } catch (e: Exception) {
                                Log.e("Tablon", "Error al crear solicitud", e)
                                val msg = e.message.orEmpty()
                                errorMensaje = if (msg.contains("row-level security", ignoreCase = true)) {
                                    "No tienes permisos en la base de datos para crear solicitudes."
                                } else {
                                    "Error al enviar la solicitud. Intenta de nuevo."
                                }
                            } finally {
                                enviando = false
                            }
                        }
                    },
                    enabled = !enviando && estado != EstadoObjeto.RECLAMADO && estado != EstadoObjeto.ENTREGADO,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primario),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                ) {
                    if (enviando) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Enviando solicitud...", color = Color.White, fontWeight = FontWeight.SemiBold)
                    } else {
                        Text(
                            text = when (estado) {
                                EstadoObjeto.RECLAMADO -> "Objeto ya reclamado"
                                EstadoObjeto.ENTREGADO -> "Objeto ya entregado"
                                else -> "¡Es mío! Solicitar devolución"
                            },
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

// =====================================================================
//  MENÚ LATERAL
// =====================================================================
@Composable
private fun PanelLateral(
    perfil: PerfilModel,
    rol: Rol,
    onIrA: (String) -> Unit,
    onCerrarSesion: () -> Unit
) {
    ModalDrawerSheet {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_unsa),
                contentDescription = "Logo UNSA",
                modifier = Modifier.size(96.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Objetos Perdidos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextoPrincipal
            )
            Text(
                text = "UNSA",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Primario
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Sesión",
                style = MaterialTheme.typography.labelLarge,
                color = Neutro
            )
            Text(
                text = perfil.email,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = rol.texto,
                color = Primario,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .background(Primario.copy(alpha = 0.12f), RoundedCornerShape(999.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
        HorizontalDivider()

        if (rol == Rol.VIGILANTE) {
            NavigationDrawerItem(
                label = { Text("Panel de vigilancia") },
                selected = false,
                onClick = { onIrA("vigilante") },
                icon = { Icon(Icons.Default.Shield, contentDescription = null) },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
        if (rol == Rol.ADMIN) {
            NavigationDrawerItem(
                label = { Text("Panel de administración") },
                selected = false,
                onClick = { onIrA("admin") },
                icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null) },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }

        NavigationDrawerItem(
            label = { Text("Cerrar sesión", color = ErrorRojo) },
            selected = false,
            onClick = onCerrarSesion,
            icon = {
                Icon(
                    Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = null,
                    tint = ErrorRojo
                )
            },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

// =====================================================================
//  COMPONENTES REUTILIZABLES
// =====================================================================
@Composable
fun Segmentado(
    opciones: List<String>,
    seleccion: Int?,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val forma = RoundedCornerShape(50)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(Color.White, forma)
            .border(1.dp, Primario.copy(alpha = 0.18f), forma)
            .padding(4.dp)
    ) {
        opciones.forEachIndexed { i, texto ->
            val activo = i == seleccion
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(forma)
                    .background(if (activo) Primario else Color.Transparent)
                    .selectable(selected = activo, role = Role.RadioButton, onClick = { onClick(i) }),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = texto,
                    color = if (activo) Color.White else TextoPrincipal,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun ChipsActivos(aplicados: FiltrosTablon, catalogos: Catalogos, vm: TablonViewModel) {
    if (aplicados.cantidadActivos == 0) return

    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        aplicados.facultadId?.let { id ->
            ChipFiltro(
                texto = catalogos.facultades.firstOrNull { it.id == id }?.nombre ?: "Facultad",
                onQuitar = { vm.quitarFacultad() }
            )
        }
        aplicados.sedeId?.let { id ->
            ChipFiltro(
                texto = catalogos.sedes.firstOrNull { it.id == id }?.nombre ?: "Sede",
                onQuitar = { vm.quitarSede() }
            )
        }
        aplicados.categorias.forEach { categoria ->
            ChipFiltro(texto = categoria.texto, onQuitar = { vm.quitarCategoria(categoria) })
        }
        if (aplicados.fecha != FiltroFecha.TODAS) {
            val texto = when (aplicados.fecha) {
                FiltroFecha.HOY -> "Hoy"
                FiltroFecha.SIETE_DIAS -> "Últimos 7 días"
                FiltroFecha.TREINTA_DIAS -> "Últimos 30 días"
                else -> textoRango(aplicados)
            }
            ChipFiltro(texto = texto, onQuitar = { vm.quitarFecha() })
        }
    }
}

private fun textoRango(f: FiltrosTablon): String {
    val desde = f.rangoDesdeMs ?: return "Rango de fechas"
    val hasta = f.rangoHastaMs ?: desde
    return if (desde == hasta) formatoFechaSelector(desde)
    else "${formatoFechaSelector(desde)} - ${formatoFechaSelector(hasta)}"
}

@Composable
private fun ChipFiltro(texto: String, onQuitar: () -> Unit) {
    Row(
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .heightIn(min = 40.dp)
            .background(Primario.copy(alpha = 0.12f), RoundedCornerShape(999.dp))
            .clickable(onClickLabel = "Quitar filtro $texto", onClick = onQuitar)
            .padding(start = 14.dp, end = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(texto, color = Primario, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Icon(
            Icons.Default.Close,
            contentDescription = "Quitar filtro",
            tint = Primario,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun InsigniaEstado(estado: EstadoObjeto) {
    Row(
        modifier = Modifier
            .background(estado.color.copy(alpha = 0.12f), RoundedCornerShape(999.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(estado.icono, contentDescription = null, tint = estado.color, modifier = Modifier.size(16.dp))
        Text(estado.texto, color = estado.color, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

// =====================================================================
//  TARJETA DEL OBJETO
// =====================================================================
@Composable
fun TarjetaObjeto(objeto: ObjetoModel, lugar: String, onClick: () -> Unit) {
    val categoria = CategoriaObjeto.desde(objeto.categoria)
    val estado = EstadoObjeto.desde(objeto.estado)

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Primario.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                val foto = objeto.fotos.firstOrNull()
                if (foto != null) {
                    SubcomposeAsyncImage(
                        model = urlFoto(foto),
                        contentDescription = "Foto de ${objeto.titulo}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        loading = {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
                                    color = Primario
                                )
                            }
                        },
                        error = {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(
                                    categoria.icono,
                                    contentDescription = categoria.texto,
                                    tint = Primario,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    )
                } else {
                    Icon(
                        categoria.icono,
                        contentDescription = categoria.texto,
                        tint = Primario,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = objeto.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextoPrincipal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Place, contentDescription = null, tint = Primario, modifier = Modifier.size(16.dp))
                    Text(
                        text = lugar,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Neutro,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = Neutro, modifier = Modifier.size(16.dp))
                    Text(
                        text = fechaRelativa(objeto.fechaHallazgo),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Neutro
                    )
                }
                InsigniaEstado(estado)
            }

            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Primario
            )
        }
    }
}

// =====================================================================
//  ESTADOS: CARGANDO / VACÍO / ERROR
// =====================================================================
@Composable
private fun ListaEsqueleto() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(4) { TarjetaEsqueleto() }
    }
}

@Composable
private fun TarjetaEsqueleto() {
    val transicion = rememberInfiniteTransition(label = "esqueleto")
    val alfa by transicion.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "alfa"
    )
    val gris = Color(0xFFCBD5E1).copy(alpha = alfa)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.size(80.dp).background(gris, RoundedCornerShape(12.dp)))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.width(160.dp).height(18.dp).background(gris, RoundedCornerShape(6.dp)))
                Box(modifier = Modifier.width(200.dp).height(14.dp).background(gris, RoundedCornerShape(6.dp)))
                Box(modifier = Modifier.width(110.dp).height(22.dp).background(gris, RoundedCornerShape(999.dp)))
            }
        }
    }
}

@Composable
private fun EstadoVacio(hayFiltros: Boolean, onLimpiar: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.SearchOff, contentDescription = null, tint = Neutro, modifier = Modifier.size(56.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = if (hayFiltros) "No hay objetos con estos filtros" else "Aún no hay objetos publicados",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextoPrincipal,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (hayFiltros) "Prueba quitando alguno de los filtros." else "Cuando alguien reporte un objeto aparecerá aquí.",
            color = Neutro,
            textAlign = TextAlign.Center
        )
        if (hayFiltros) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onLimpiar,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primario),
                modifier = Modifier.heightIn(min = 48.dp)
            ) { Text("Limpiar filtros", color = Color.White, fontWeight = FontWeight.SemiBold) }
        }
    }
}

@Composable
private fun EstadoError(mensaje: String, onReintentar: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Banner(texto = mensaje, color = ErrorRojo, icono = "⚠")
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onReintentar,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primario),
            modifier = Modifier.heightIn(min = 48.dp)
        ) { Text("Reintentar", color = Color.White, fontWeight = FontWeight.SemiBold) }
    }
}

// =====================================================================
//  HOJA DE FILTROS
// =====================================================================
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun HojaFiltros(vm: TablonViewModel, catalogos: Catalogos, onCerrar: () -> Unit) {
    val borrador by vm.borrador.collectAsState()
    val conteo by vm.conteo.collectAsState()
    var pidiendoRango by remember { mutableStateOf(false) }
    val estadoHoja = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onCerrar,
        sheetState = estadoHoja,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Filtros", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextoPrincipal)
                IconButton(onClick = onCerrar) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar filtros")
                }
            }

            SelectorLista(
                etiqueta = "Facultad",
                textoTodos = "Todas las facultades",
                opciones = catalogos.facultades.map { it.id to it.nombre },
                seleccionado = borrador.facultadId,
                onSeleccion = { id -> vm.editarBorrador { it.copy(facultadId = id) } }
            )

            SelectorLista(
                etiqueta = "Sede",
                textoTodos = "Todas las sedes",
                opciones = catalogos.sedes.map { it.id to it.nombre },
                seleccionado = borrador.sedeId,
                onSeleccion = { id -> vm.editarBorrador { it.copy(sedeId = id) } }
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                EtiquetaCampo("Tipo de objeto")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoriaObjeto.entries.forEach { categoria ->
                        val activa = categoria in borrador.categorias
                        FilterChip(
                            selected = activa,
                            onClick = {
                                vm.editarBorrador {
                                    it.copy(
                                        categorias = if (activa) it.categorias - categoria
                                        else it.categorias + categoria
                                    )
                                }
                            },
                            label = { Text(categoria.texto) },
                            leadingIcon = {
                                Icon(categoria.icono, contentDescription = null, modifier = Modifier.size(18.dp))
                            },
                            modifier = Modifier.heightIn(min = 40.dp)
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                EtiquetaCampo("Fecha")
                val indiceFecha = when (borrador.fecha) {
                    FiltroFecha.HOY -> 0
                    FiltroFecha.SIETE_DIAS -> 1
                    FiltroFecha.TREINTA_DIAS -> 2
                    FiltroFecha.RANGO -> 3
                    FiltroFecha.TODAS -> null
                }
                Segmentado(
                    opciones = listOf("Hoy", "7 días", "30 días", "Rango"),
                    seleccion = indiceFecha,
                    onClick = { i ->
                        val nueva = listOf(
                            FiltroFecha.HOY, FiltroFecha.SIETE_DIAS,
                            FiltroFecha.TREINTA_DIAS, FiltroFecha.RANGO
                        )[i]
                        if (nueva == borrador.fecha) {
                            vm.editarBorrador {
                                it.copy(fecha = FiltroFecha.TODAS, rangoDesdeMs = null, rangoHastaMs = null)
                            }
                        } else {
                            vm.editarBorrador { it.copy(fecha = nueva) }
                            if (nueva == FiltroFecha.RANGO) pidiendoRango = true
                        }
                    }
                )
                if (borrador.fecha == FiltroFecha.RANGO && borrador.rangoDesdeMs != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(textoRango(borrador), color = Neutro)
                        TextButton(onClick = { pidiendoRango = true }) { Text("Cambiar") }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                EtiquetaCampo("Orden")
                Segmentado(
                    opciones = OrdenTablon.entries.map { it.texto },
                    seleccion = borrador.orden.ordinal,
                    onClick = { i -> vm.editarBorrador { it.copy(orden = OrdenTablon.entries[i]) } }
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Info, contentDescription = null, tint = Neutro, modifier = Modifier.size(18.dp))
                Text("Todos los filtros son opcionales", color = Neutro, fontSize = 14.sp)
            }

            Button(
                onClick = {
                    vm.aplicarBorrador()
                    onCerrar()
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primario),
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
            ) {
                val n = conteo
                Text(
                    text = when (n) {
                        null -> "Aplicar"
                        1L -> "Aplicar (1 resultado)"
                        else -> "Aplicar ($n resultados)"
                    },
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            TextButton(
                onClick = { vm.limpiarBorrador() },
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
            ) { Text("Limpiar todo", fontWeight = FontWeight.SemiBold) }
        }
    }

    if (pidiendoRango) {
        DialogoRango(
            onConfirmar = { desde, hasta ->
                vm.editarBorrador {
                    it.copy(fecha = FiltroFecha.RANGO, rangoDesdeMs = desde, rangoHastaMs = hasta)
                }
                pidiendoRango = false
            },
            onCancelar = {
                if (borrador.rangoDesdeMs == null) {
                    vm.editarBorrador { it.copy(fecha = FiltroFecha.TODAS) }
                }
                pidiendoRango = false
            }
        )
    }
}

@Composable
private fun EtiquetaCampo(texto: String) {
    Text(texto, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextoPrincipal)
}

@Composable
private fun SelectorLista(
    etiqueta: String,
    textoTodos: String,
    opciones: List<Pair<Int, String>>,
    seleccionado: Int?,
    onSeleccion: (Int?) -> Unit
) {
    var abierto by remember { mutableStateOf(false) }
    val actual = opciones.firstOrNull { it.first == seleccionado }?.second ?: textoTodos

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        EtiquetaCampo(etiqueta)
        Box {
            OutlinedButton(
                onClick = { abierto = true },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
            ) {
                Text(
                    text = actual,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Start,
                    color = TextoPrincipal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextoPrincipal)
            }
            DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
                DropdownMenuItem(
                    text = { Text(textoTodos) },
                    onClick = {
                        onSeleccion(null)
                        abierto = false
                    }
                )
                opciones.forEach { (id, nombre) ->
                    DropdownMenuItem(
                        text = { Text(nombre) },
                        onClick = {
                            onSeleccion(id)
                            abierto = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogoRango(onConfirmar: (Long, Long) -> Unit, onCancelar: () -> Unit) {
    val estado = rememberDateRangePickerState()
    DatePickerDialog(
        onDismissRequest = onCancelar,
        confirmButton = {
            TextButton(
                enabled = estado.selectedStartDateMillis != null,
                onClick = {
                    val inicio = estado.selectedStartDateMillis
                    if (inicio != null) {
                        onConfirmar(inicio, estado.selectedEndDateMillis ?: inicio)
                    }
                }
            ) { Text("Aceptar") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    ) {
        DateRangePicker(
            state = estado,
            modifier = Modifier.height(500.dp),
            showModeToggle = false
        )
    }
}

// =====================================================================
//  BARRA INFERIOR
// =====================================================================
@Composable
fun BarraInferior(
    onTablon: () -> Unit,
    onReportar: () -> Unit,
    onSolicitudes: () -> Unit
) {
    Surface(color = Color.White, shadowElevation = 8.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ItemBarra(Icons.Default.Notifications, "Tablón", Primario, onTablon)
            Column(
                modifier = Modifier.clickable { onReportar() },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .background(Primario, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Reportar objeto",
                        tint = Color.White,
                        modifier = Modifier.size(38.dp)
                    )
                }
                Text("Reportar", fontSize = 11.sp, color = Primario, fontWeight = FontWeight.SemiBold)
            }
            ItemBarra(Icons.Default.Email, "Solicitudes", Neutro, onSolicitudes)
        }
    }
}

@Composable
private fun ItemBarra(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    texto: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icono, contentDescription = texto, tint = color)
        Text(texto, fontSize = 11.sp, color = color, fontWeight = FontWeight.SemiBold)
    }
}
