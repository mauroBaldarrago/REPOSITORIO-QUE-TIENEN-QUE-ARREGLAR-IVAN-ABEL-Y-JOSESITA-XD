package com.example.ihc_proyecto1theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ihc_proyecto1theme.model.FacultadModel
import com.example.ihc_proyecto1theme.model.PabellonModel
import com.example.ihc_proyecto1theme.model.SedeModel

enum class Rol(val codigo: String, val texto: String) {
    ESTUDIANTE("estudiante", "Estudiante"),
    VIGILANTE("vigilante", "Vigilante"),
    ADMIN("admin", "Administrador");

    val esStaff: Boolean get() = this != ESTUDIANTE

    companion object {
        fun desde(codigo: String?): Rol = entries.firstOrNull { it.codigo == codigo } ?: ESTUDIANTE
    }
}

/** Categorías cerradas (coinciden con el enum categoria_objeto de la base de datos). */
enum class CategoriaObjeto(val codigo: String, val texto: String, val icono: ImageVector) {
    MOCHILAS_BOLSOS("mochilas_bolsos", "Mochilas y bolsos", Icons.Default.Work),
    ELECTRONICOS("electronicos", "Electrónicos", Icons.Default.Laptop),
    DOCUMENTOS("documentos", "Documentos y carnés", Icons.Default.Badge),
    LLAVES("llaves", "Llaves", Icons.Default.VpnKey),
    ROPA_ACCESORIOS("ropa_accesorios", "Ropa y accesorios", Icons.Default.Checkroom),
    OTROS("otros", "Otros", Icons.Default.MoreHoriz);

    companion object {
        fun desde(codigo: String?): CategoriaObjeto = entries.firstOrNull { it.codigo == codigo } ?: OTROS
    }
}

/** Estados del objeto (coinciden con el enum estado_objeto de la base de datos). */
enum class EstadoObjeto(val codigo: String, val texto: String, val color: Color, val icono: ImageVector) {
    PENDIENTE_ENTREGA("pendiente_entrega", "Pendiente de entrega", Alerta, Icons.Default.Schedule),
    SIN_RECLAMAR("sin_reclamar", "Sin reclamar", Exito, Icons.Default.CheckCircle),
    RECLAMADO("reclamado", "Reclamado", Alerta, Icons.Default.Schedule),
    ENTREGADO("entregado", "Entregado", Neutro, Icons.Default.Inventory2);

    companion object {
        fun desde(codigo: String?): EstadoObjeto =
            entries.firstOrNull { it.codigo == codigo } ?: SIN_RECLAMAR
    }
}

enum class FiltroFecha { TODAS, HOY, SIETE_DIAS, TREINTA_DIAS, RANGO }

enum class OrdenTablon(val texto: String) {
    RECIENTES("Recientes"),
    GENERAL("General")
}

data class FiltrosTablon(
    val facultadId: Int? = null,
    val sedeId: Int? = null,
    val categorias: Set<CategoriaObjeto> = emptySet(),
    val fecha: FiltroFecha = FiltroFecha.TODAS,
    val rangoDesdeMs: Long? = null,
    val rangoHastaMs: Long? = null,
    val orden: OrdenTablon = OrdenTablon.RECIENTES
) {
    /** Filtros activos (el orden no cuenta). */
    val cantidadActivos: Int
        get() = listOf(
            facultadId != null,
            sedeId != null,
            categorias.isNotEmpty(),
            fecha != FiltroFecha.TODAS
        ).count { it }
}

data class Catalogos(
    val sedes: List<SedeModel> = emptyList(),
    val facultades: List<FacultadModel> = emptyList(),
    val pabellones: List<PabellonModel> = emptyList()
)
