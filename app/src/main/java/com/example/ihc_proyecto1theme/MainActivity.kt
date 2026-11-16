package com.example.ihc_proyecto1theme

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
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
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID

val Primario = Color(0xFF0B5FA5)
val Exito = Color(0xFF1B7F4B)
val Alerta = Color(0xFFB45309)
val ErrorRojo = Color(0xFFB42318)
val Neutro = Color(0xFF475569)
val Fondo = Color(0xFFF5F7FA)

const val WEB_CLIENT_ID = "111619497202-2a8q3udk3nhi61ndqkhbaiplae58cuhi.apps.googleusercontent.com"
const val DOMINIO = "@unsa.edu.pe"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                AppRaiz()
            }
        }
    }
}

// =====================================================================
//  RAÍZ: decide qué se muestra según la sesión de Supabase
//   - leyendo sesión  -> pantalla de carga
//   - con sesión      -> app (según el rol)
//   - sin sesión      -> login
//  Al iniciar o cerrar sesión la pantalla cambia sola, sin navegar a mano.
// =====================================================================
@Composable
fun AppRaiz() {
    val estadoSesion by SupabaseClient.client.auth.sessionStatus.collectAsState()

    when (val estado = estadoSesion) {
        is SessionStatus.Initializing -> PantallaCarga()
        is SessionStatus.Authenticated -> AppAutenticada(userId = estado.session.user?.id ?: "")
        else -> LoginScreen()
    }
}

@Composable
fun AppAutenticada(userId: String) {
    // La clave evita reutilizar el perfil de otra cuenta si se cambia de usuario
    val sesionVm: SesionViewModel = viewModel(key = "sesion_$userId")
    val estado by sesionVm.estado.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    when (val e = estado) {
        EstadoPerfil.Cargando -> PantallaCarga()
        is EstadoPerfil.Error -> PantallaErrorPerfil(
            mensaje = e.mensaje,
            onReintentar = { sesionVm.cargar() },
            onSalir = { cerrarSesion(context, scope) }
        )
        is EstadoPerfil.Listo -> NavegacionPorRol(e.perfil)
    }
}

@Composable
fun NavegacionPorRol(perfil: PerfilModel) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val rol = Rol.desde(perfil.rol)
    val salir: () -> Unit = { cerrarSesion(context, scope) }

    // Pantalla de inicio según el rol
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
            PantallaPendiente("Reportar objeto") { navController.popBackStack() }
        }
        composable("solicitudes") {
            PantallaPendiente("Solicitudes") { navController.popBackStack() }
        }
        composable("vigilante") {
            if (rol.esStaff) {
                PantallaRol(
                    titulo = "Panel de vigilancia",
                    detalle = "Aquí irán \"Por recibir\", \"Por entregar\", el escáner QR y el mapa de casilleros (Fase 8).",
                    perfil = perfil,
                    onVerTablon = { navController.navigate("tablon") },
                    onSalir = salir
                )
            }
        }
        composable("admin") {
            if (rol == Rol.ADMIN) {
                PantallaRol(
                    titulo = "Panel de administración",
                    detalle = "Aquí irán la gestión de roles, las denuncias y los catálogos (Fase 9).",
                    perfil = perfil,
                    onVerTablon = { navController.navigate("tablon") },
                    onSalir = salir
                )
            }
        }
    }
}

/** Cierra la sesión de Google y de Supabase; la raíz muestra el login automáticamente. */
fun cerrarSesion(context: Context, scope: CoroutineScope) {
    scope.launch {
        // Primero Google; la sesión de Supabase va al final porque al cerrarla
        // esta pantalla sale de composición y el scope se cancela.
        try {
            CredentialManager.create(context).clearCredentialState(
                androidx.credentials.ClearCredentialStateRequest()
            )
        } catch (e: Exception) {
            Log.e("Login", "Error al limpiar credenciales", e)
        }
        try {
            SupabaseClient.client.auth.signOut()
        } catch (e: Exception) {
            Log.e("Login", "Error al cerrar sesión", e)
        }
    }
}

// =====================================================================
//  LOGIN CON GOOGLE (solo @unsa.edu.pe)
// =====================================================================
sealed class ResultadoLogin {
    data class Exitoso(val correo: String) : ResultadoLogin()
    object DominioInvalido : ResultadoLogin()
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
    return try {
        val (nonceCrudo, nonceHash) = generarNonce()

        // El filtro de dominio hace que Google solo muestre cuentas @unsa.edu.pe
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

        // Segunda barrera en el teléfono (la tercera es el trigger de la base de datos)
        if (!correo.endsWith(DOMINIO, ignoreCase = true)) {
            return ResultadoLogin.DominioInvalido
        }

        SupabaseClient.client.auth.signInWith(IDToken) {
            idToken = credencial.idToken
            provider = Google
            nonce = nonceCrudo
        }
        ResultadoLogin.Exitoso(correo)
    } catch (e: androidx.credentials.exceptions.GetCredentialCancellationException) {
        ResultadoLogin.Cancelado
    } catch (e: Exception) {
        Log.e("Login", "Error al iniciar sesión", e)
        val mensaje = e.message.orEmpty()
        if (mensaje.contains("unsa.edu.pe", ignoreCase = true) ||
            mensaje.contains("Database error", ignoreCase = true)
        ) {
            ResultadoLogin.DominioInvalido
        } else {
            ResultadoLogin.Fallo
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
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_unsa),
                contentDescription = "Logo UNSA",
                modifier = Modifier.size(140.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Objetos Perdidos",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )
            Text(
                text = "UNSA",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Primario
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Reporta objetos perdidos y encuentra los que otros han hallado dentro de la universidad.",
                style = MaterialTheme.typography.bodyMedium,
                color = Neutro,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    mensajeError = ""
                    cargando = true
                    scope.launch {
                        when (iniciarSesionConGoogle(context)) {
                            // Si el inicio es exitoso, la raíz detecta la sesión y cambia de pantalla
                            is ResultadoLogin.Exitoso -> Unit
                            ResultadoLogin.DominioInvalido -> {
                                mensajeError = "Usa tu correo institucional $DOMINIO"
                            }
                            ResultadoLogin.Cancelado -> {
                                mensajeError = ""
                            }
                            ResultadoLogin.Fallo -> {
                                mensajeError = "No se pudo iniciar sesión. Intenta de nuevo"
                            }
                        }
                        cargando = false
                    }
                },
                enabled = !cargando,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primario),
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
            ) {
                if (cargando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Ingresando...", color = Color.White, fontWeight = FontWeight.SemiBold)
                } else {
                    Text(
                        "Ingresar",
                        color = Color.White,
                        fontSize = 18.sp,
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
                            contentDescription = "Google",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

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

// =====================================================================
//  PANTALLAS AUXILIARES
// =====================================================================
@Composable
fun PantallaCarga() {
    FondoDecorado {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Primario)
        }
    }
}

@Composable
fun PantallaErrorPerfil(mensaje: String, onReintentar: () -> Unit, onSalir: () -> Unit) {
    FondoDecorado {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
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
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
            ) { Text("Cerrar sesión") }
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
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(titulo, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            Text("${Rol.desde(perfil.rol).texto} · ${perfil.email}", color = Primario, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
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
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
            ) { Text("Cerrar sesión", color = ErrorRojo) }
        }
    }
}

@Composable
fun PantallaPendiente(titulo: String, onVolver: () -> Unit) {
    FondoDecorado {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(titulo, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Pantalla en construcción", color = Neutro)
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedButton(onClick = onVolver) {
                Text("← Volver al tablón")
            }
        }
    }
}

@Composable
fun Banner(texto: String, color: Color, icono: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icono, color = color, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(text = texto, color = color, fontWeight = FontWeight.SemiBold)
    }
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    MaterialTheme { Banner("Vista previa", Exito, "✓") }
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
                color = Primario.copy(alpha = 0.06f),
                radius = size.width * 0.35f,
                center = Offset(x = size.width * 0.15f, y = -size.height * 0.02f)
            )
            drawCircle(
                color = Primario.copy(alpha = 0.08f),
                radius = size.width * 0.6f,
                center = Offset(x = size.width, y = size.height * 0.98f)
            )
            drawCircle(
                color = Primario.copy(alpha = 0.05f),
                radius = size.width * 0.35f,
                center = Offset(x = size.width * 0.85f, y = size.height * 1.02f)
            )
        }
        content()
    }
}
