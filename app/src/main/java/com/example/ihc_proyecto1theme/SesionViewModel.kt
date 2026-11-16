package com.example.ihc_proyecto1theme

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ihc_proyecto1theme.model.PerfilModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface EstadoPerfil {
    data object Cargando : EstadoPerfil
    data class Listo(val perfil: PerfilModel) : EstadoPerfil
    data class Error(val mensaje: String) : EstadoPerfil
}

/** Lee el perfil (y por tanto el rol) del usuario que inició sesión. */
class SesionViewModel : ViewModel() {

    private val repo = PerfilRepository()
    private val _estado = MutableStateFlow<EstadoPerfil>(EstadoPerfil.Cargando)
    val estado: StateFlow<EstadoPerfil> = _estado.asStateFlow()

    init {
        cargar()
    }

    fun cargar() {
        _estado.value = EstadoPerfil.Cargando
        viewModelScope.launch {
            _estado.value = try {
                val perfil = repo.miPerfil()
                if (perfil == null) {
                    EstadoPerfil.Error("No se encontró tu perfil. Cierra sesión e inténtalo de nuevo.")
                } else {
                    EstadoPerfil.Listo(perfil)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("Sesion", "Error al cargar el perfil", e)
                EstadoPerfil.Error("No se pudo cargar tu perfil. Revisa tu conexión.")
            }
        }
    }
}
