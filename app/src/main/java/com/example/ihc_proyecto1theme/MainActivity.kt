package com.example.ihc_proyecto1theme

import android.content.Context
import androidx.compose.runtime.SideEffect
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ihc_proyecto1theme.model.PerfilModel
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.security.MessageDigest
import java.util.UUID

// Paleta institucional UNSA (Guinda y Dorado)
val Primario = Color(0xFF6C1323)
val GuindaOscuro = Color(0xFF4A0D18)
val DoradoUnsa = Color(0xFFC59B27)
val Exito = Color(0xFF1B7F4B)
val Alerta = Color(0xFFB45309)
val ErrorRojo = Color(0xFFB42318)
val Neutro = Color(0xFF52494B)
val Fondo = Color(0xFFFAF7F7)

private val EsquemaColorUnsa = lightColorScheme(
    primary = Primario,
    onPrimary = Color.White,
    secondary = DoradoUnsa,
    onSecondary = Color(0xFF2B1D00),
    tertiary = GuindaOscuro,
    background = Fondo,
    surface = Color.White,
    onSurface = Color(0xFF1F1A1B),
    error = ErrorRojo
)

const val WEB_CLIENT_ID = "111619497202-2a8q3udk3nhi61ndqkhbaiplae58cuhi.apps.googleusercontent.com"
const val DOMINIO = "@unsa.edu.pe"

private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(colorScheme = EsquemaColorUnsa) {
                AppRaiz()
            }
        }
    }
}

@Composable
fun AppRaiz() {
    val estadoSesion by SupabaseClient.client.auth.sessionStatus.collectAsState()

    // Último usuario con sesión activa. Se guarda con rememberSaveable, así sobrevive
    // a rotaciones y a que Android cierre la app mientras está abierta la cámara.
    var ultimoUsuario by rememberSaveable { mutableStateOf<String?>(null) }

    // Mientras Supabase refresca la sesión (Initializing o RefreshFailure) se mantiene
    // la app en pantalla. Solo se vuelve al login si la sesión realmente se cerró.
    val usuarioActivo: String? = when (val estado = estadoSesion) {
        is SessionStatus.Authenticated -> estado.session.user?.id ?: ""
        is SessionStatus.NotAuthenticated -> null
        else -> ultimoUsuario
    }
    SideEffect { ultimoUsuario = usuarioActivo }

    // Importante: AppAutenticada se llama en UN solo lugar. Si se llamara desde dos
    // ramas distintas, Compose destruiría y recrearía toda la navegación al cambiar de rama.
    when {
        usuarioActivo != null -> AppAutenticada(userId = usuarioActivo)
        estadoSesion is SessionStatus.NotAuthenticated -> LoginScreen()
        else -> PantallaCarga()
    }
}

@Composable
fun AppAutenticada(userId: String) {
    val sesionVm: SesionViewModel = viewModel(key = "sesion_$userId")
    val estado by sesionVm.estado.collectAsState()
    val context = LocalContext.current

    when (val e = estado) {
        EstadoPerfil.Cargando -> PantallaCarga()
        is EstadoPerfil.Error -> PantallaErrorPerfil(
            mensaje = e.mensaje,
            onReintentar = { sesionVm.cargar() },
            onSalir = { cerrarSesion(context) }
        )
        is EstadoPerfil.Listo -> NavegacionPorRol(e.perfil)
    }
}

@Composable
fun NavegacionPorRol(perfil: PerfilModel) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val rol = Rol.desde(perfil.rol)
    val salir: () -> Unit = { cerrarSesion(context) }

    val inicio = when (rol) {
        Rol.ESTUDIANTE -> "tablon"
        Rol.VIGILANTE -> "vigilante"
        Rol.ADMIN -> "admin"
    }

    NavHost(navController = navController, startDestination = inicio) {
        composable("tablon") {
            TablonScreen(navController = navController, perfil = perfil, onCerrarSesion = salir)
        }
        composable("reportar") {
            ReportarScreen(
                perfil = perfil,
                onVolver = { navController.popBackStack() },
                onReporteExitoso = { navController.popBackStack() }
            )
        }
        composable("solicitudes") {
            SolicitudesScreen(
                onVolver = { navController.popBackStack() }
            )
        }
        composable("vigilante") {
            if (rol.esStaff) {
                PantallaRol(
                    titulo = "Panel de vigilancia",
                    detalle = "Gestión de objetos por recibir, entregas, escáner QR y mapa de casilleros.",
                    perfil = perfil,
                    onVerTablon = { navController.navigate("tablon") },
                    onSalir = salir
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.navigate("tablon") {
                        popUpTo("vigilante") { inclusive = true }
                    }
                }
            }
        }
        composable("admin") {
            if (rol == Rol.ADMIN) {
                PantallaRol(
                    titulo = "Panel de administración",
                    detalle = "Gestión de roles institucionales, denuncias y catálogos de sedes.",
                    perfil = perfil,
                    onVerTablon = { navController.navigate("tablon") },
                    onSalir = salir
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.navigate("tablon") {
                        popUpTo("admin") { inclusive = true }
                    }
                }
            }
        }
    }
}

fun cerrarSesion(context: Context) {
    appScope.launch {
        try {
            CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.e("Login", "Error al limpiar credenciales de Google", e)
        }
        try {
            SupabaseClient.client.auth.signOut()
        } catch (e: Exception) {
            Log.e("Login", "Error al cerrar sesión en Supabase", e)
        }
    }
}

sealed class ResultadoLogin {
    data class Exitoso(val correo: String) : ResultadoLogin()
    object DominioInvalido : ResultadoLogin()
    object ErrorConfiguracionGoogle : ResultadoLogin()
    object ErrorServidor : ResultadoLogin()
    object ErrorConexion : ResultadoLogin()
    object ErrorConfiguracionSupabase : ResultadoLogin()
    object Cancelado : ResultadoLogin()
    object Fallo : ResultadoLogin()
}

private fun generarNonce(): Pair<String, String> {
    val crudo = UUID.randomUUID().toString()
    val hash = MessageDigest.getInstance("SHA-256")
        .digest(crudo.toByteArray())
        .joinToString("") { "%02x".format(it) }
    return crudo to hash
}

suspend fun iniciarSesionConGoogle(context: Context): ResultadoLogin {
    // Verifica que Supabase tenga sus datos de conexión configurados.
    if (BuildConfig.SUPABASE_URL.isBlank() || BuildConfig.SUPABASE_ANON_KEY.isBlank()) {
        Log.e("Login", "Faltan SUPABASE_URL o SUPABASE_ANON_KEY en local.properties")
        return ResultadoLogin.ErrorConfiguracionSupabase
    }

    return try {
        val (nonceCrudo, nonceHash) = generarNonce()

        val opcionGoogle = GetSignInWithGoogleOption.Builder(WEB_CLIENT_ID)
            .setHostedDomainFilter(DOMINIO.removePrefix("@"))
            .setNonce(nonceHash)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(opcionGoogle)
            .build()

        val resultado = CredentialManager.create(context).getCredential(context, request)
        val credencial = GoogleIdTokenCredential.createFrom(resultado.credential.data)
        val correo = credencial.id

        if (!correo.endsWith(DOMINIO, ignoreCase = true)) {
            return ResultadoLogin.DominioInvalido
        }

        SupabaseClient.client.auth.signInWith(IDToken) {
            idToken = credencial.idToken
            provider = Google
            nonce = nonceCrudo
        }
        ResultadoLogin.Exitoso(correo)

    } catch (e: GetCredentialCancellationException) {
        val detalle = e.errorMessage?.toString().orEmpty()
        Log.e("Login", "Flujo cancelado o bloqueado por Play Services: $detalle", e)
        if (detalle.contains("reauth failed", ignoreCase = true) || detalle.contains("[16]")) {
            ResultadoLogin.ErrorConfiguracionGoogle
        } else {
            ResultadoLogin.Cancelado
        }
    } catch (e: GetCredentialException) {
        Log.e("Login", "Error de CredentialManager (${e.type}): ${e.errorMessage}", e)
        ResultadoLogin.ErrorConfiguracionGoogle
    } catch (e: Exception) {
        Log.e("Login", "Error al iniciar sesión", e)
        val mensaje = e.message.orEmpty()

        when {
            mensaje.contains("unsa.edu.pe", ignoreCase = true) -> ResultadoLogin.DominioInvalido
            mensaje.contains("Database error", ignoreCase = true) -> ResultadoLogin.ErrorServidor
            e is UnknownHostException || e is SocketTimeoutException || e is IOException ->
                ResultadoLogin.ErrorConexion
            else -> ResultadoLogin.Fallo
        }
    }
}

@Composable
fun LoginScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var cargando by rememberSaveable { mutableStateOf(false) }
    var mensajeError by rememberSaveable { mutableStateOf("") }

    FondoDecorado {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                color = Primario.copy(alpha = 0.08f),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, DoradoUnsa.copy(alpha = 0.55f))
            ) {
                Text(
                    text = "UNIVERSIDAD NACIONAL DE SAN AGUSTÍN",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Primario,
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .size(152.dp)
                    .background(Color.White, CircleShape)
                    .border(2.5.dp, DoradoUnsa, CircleShape)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_unsa),
                    contentDescription = "Escudo de la Universidad Nacional de San Agustín",
                    modifier = Modifier.size(124.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Objetos Perdidos",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F1A1B)
            )
            Text(
                text = "UNSA",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Primario,
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            HorizontalDivider(
                modifier = Modifier.width(64.dp),
                thickness = 3.dp,
                color = DoradoUnsa
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Reporta objetos perdidos y recupera los que han sido hallados dentro de las sedes universitarias.",
                style = MaterialTheme.typography.bodyMedium,
                color = Neutro,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    mensajeError = ""
                    cargando = true
                    scope.launch {
                        when (iniciarSesionConGoogle(context)) {
                            is ResultadoLogin.Exitoso -> Unit
                            ResultadoLogin.DominioInvalido -> {
                                mensajeError = "Acceso restringido: usa tu correo institucional $DOMINIO"
                            }
                            ResultadoLogin.ErrorConfiguracionGoogle -> {
                                mensajeError = "Error con tu cuenta de Google en este dispositivo. Verifica tu sesión o PIN de bloqueo."
                            }
                            ResultadoLogin.ErrorServidor -> {
                                mensajeError = "Tu correo es válido, pero hubo un error al registrar tu perfil en el servidor."
                            }
                            ResultadoLogin.ErrorConexion -> {
                                mensajeError = "No se pudo contactar al servidor. Verifica tu conexión e inténtalo nuevamente."
                            }
                            ResultadoLogin.ErrorConfiguracionSupabase -> {
                                mensajeError = "Falta configurar la conexión con el servidor. Contacta al responsable del proyecto."
                            }
                            ResultadoLogin.Cancelado -> {
                                mensajeError = ""
                            }
                            ResultadoLogin.Fallo -> {
                                mensajeError = "No se pudo iniciar sesión. Inténtalo nuevamente. Si el problema continúa, revisa el registro de errores."
                            }
                        }
                        cargando = false
                    }
                },
                enabled = !cargando,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, DoradoUnsa.copy(alpha = 0.6f)),
                colors = ButtonDefaults.buttonColors(containerColor = Primario),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 54.dp)
                    .semantics { contentDescription = "Ingresar con cuenta institucional UNSA" }
            ) {
                if (cargando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = DoradoUnsa
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Ingresando...", color = Color.White, fontWeight = FontWeight.SemiBold)
                } else {
                    Text(
                        text = "Ingresar con Google",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_google),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Exclusivo para correos institucionales $DOMINIO",
                style = MaterialTheme.typography.labelMedium,
                color = Neutro,
                textAlign = TextAlign.Center
            )

            if (mensajeError.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Banner(
                    texto = mensajeError,
                    color = ErrorRojo,
                    icono = "⚠"
                )
            }
        }
    }
}

@Composable
fun PantallaCarga() {
    FondoDecorado {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .semantics { contentDescription = "Cargando sesión" },
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Primario)
        }
    }
}

@Composable
fun PantallaErrorPerfil(mensaje: String, onReintentar: () -> Unit, onSalir: () -> Unit) {
    FondoDecorado {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Banner(texto = mensaje, color = ErrorRojo, icono = "⚠")
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onReintentar,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primario),
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
            ) { Text("Reintentar", color = Color.White, fontWeight = FontWeight.SemiBold) }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onSalir,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Primario.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
            ) { Text("Cerrar sesión", color = Primario) }
        }
    }
}

@Composable
fun PantallaRol(
    titulo: String,
    detalle: String,
    perfil: PerfilModel,
    onVerTablon: () -> Unit,
    onSalir: () -> Unit
) {
    FondoDecorado {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F1A1B),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${Rol.desde(perfil.rol).texto} · ${perfil.email}",
                color = Primario,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(detalle, color = Neutro, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onVerTablon,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primario),
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
            ) { Text("Ver tablón", color = Color.White, fontWeight = FontWeight.SemiBold) }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onSalir,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, ErrorRojo.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
            ) { Text("Cerrar sesión", color = ErrorRojo) }
        }
    }
}

@Composable
fun Banner(texto: String, color: Color, icono: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color.copy(alpha = 0.10f), RoundedCornerShape(12.dp))
            .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icono, color = color, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(
            text = texto,
            color = color,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun FondoDecorado(content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Primario.copy(alpha = 0.08f),
                radius = size.width * 0.55f,
                center = Offset(x = 0f, y = size.height * 0.02f)
            )
            drawCircle(
                color = DoradoUnsa.copy(alpha = 0.10f),
                radius = size.width * 0.35f,
                center = Offset(x = size.width * 0.15f, y = -size.height * 0.02f)
            )
            drawCircle(
                color = Primario.copy(alpha = 0.08f),
                radius = size.width * 0.6f,
                center = Offset(x = size.width, y = size.height * 0.98f)
            )
            drawCircle(
                color = DoradoUnsa.copy(alpha = 0.09f),
                radius = size.width * 0.35f,
                center = Offset(x = size.width * 0.85f, y = size.height * 1.02f)
            )
        }
        content()
    }
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    MaterialTheme(colorScheme = EsquemaColorUnsa) {
        LoginScreen()
    }
}
