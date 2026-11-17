package com.example.ihc_proyecto1theme

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ihc_proyecto1theme.model.ObjetoModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface EstadoLista {
    data object Cargando : EstadoLista
    data class Error(val mensaje: String) : EstadoLista
    data class Datos(val objetos: List<ObjetoModel>) : EstadoLista
}

@OptIn(FlowPreview::class)
class TablonViewModel : ViewModel() {

    private val repo = ObjetosRepository()

    private val _aplicados = MutableStateFlow(FiltrosTablon())
    val aplicados: StateFlow<FiltrosTablon> = _aplicados.asStateFlow()

    private val _borrador = MutableStateFlow(FiltrosTablon())
    val borrador: StateFlow<FiltrosTablon> = _borrador.asStateFlow()

    private val _lista = MutableStateFlow<EstadoLista>(EstadoLista.Cargando)
    val lista: StateFlow<EstadoLista> = _lista.asStateFlow()

    private val _conteo = MutableStateFlow<Long?>(null)
    val conteo: StateFlow<Long?> = _conteo.asStateFlow()

    private val _catalogos = MutableStateFlow(Catalogos())
    val catalogos: StateFlow<Catalogos> = _catalogos.asStateFlow()

    private var trabajoLista: Job? = null

    init {
        cargarCatalogos()
        recargar()
        viewModelScope.launch {
            _borrador.debounce(250).collectLatest { filtros ->
                _conteo.value = null
                _conteo.value = try {
                    repo.contarTablon(filtros)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e("Tablon", "Error al contar resultados", e)
                    null
                }
            }
        }
    }

    private fun cargarCatalogos() {
        viewModelScope.launch {
            try {
                _catalogos.value = Catalogos(
                    sedes = repo.sedes(),
                    facultades = repo.facultades(),
                    pabellones = repo.pabellones()
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("Tablon", "Error al cargar catálogos", e)
            }
        }
    }

    fun recargar() {
        trabajoLista?.cancel()
        _lista.value = EstadoLista.Cargando
        trabajoLista = viewModelScope.launch {
            _lista.value = try {
                EstadoLista.Datos(repo.listarTablon(_aplicados.value))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("Tablon", "Error al cargar el tablón", e)
                EstadoLista.Error("No se pudo cargar el tablón. Revisa tu conexión e inténtalo de nuevo.")
            }
        }
    }

    fun abrirFiltros() {
        _borrador.value = _aplicados.value
    }

    fun editarBorrador(cambio: (FiltrosTablon) -> FiltrosTablon) {
        _borrador.update(cambio)
    }

    fun limpiarBorrador() {
        _borrador.update { FiltrosTablon(orden = it.orden) }
    }

    fun aplicarBorrador() {
        _aplicados.value = _borrador.value
        recargar()
    }

    fun cambiarOrden(orden: OrdenTablon) {
        if (_aplicados.value.orden == orden) return
        _aplicados.update { it.copy(orden = orden) }
        recargar()
    }

    fun quitarFacultad() = actualizar { it.copy(facultadId = null) }
    fun quitarSede() = actualizar { it.copy(sedeId = null) }
    fun quitarCategoria(c: CategoriaObjeto) = actualizar { it.copy(categorias = it.categorias - c) }
    fun quitarFecha() = actualizar {
        it.copy(fecha = FiltroFecha.TODAS, rangoDesdeMs = null, rangoHastaMs = null)
    }

    fun limpiarTodo() = actualizar { FiltrosTablon(orden = it.orden) }

    private fun actualizar(cambio: (FiltrosTablon) -> FiltrosTablon) {
        _aplicados.update(cambio)
        recargar()
    }
}