package com.example.ihc_proyecto1theme.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ObjetoModel(
    val id: String = "",
    val titulo: String,
    val descripcion: String? = null,
    val categoria: String,
    @SerialName("facultad_id") val facultadId: Int? = null,
    @SerialName("sede_id") val sedeId: Int? = null,
    @SerialName("pabellon_id") val pabellonId: Int? = null,
    @SerialName("lugar_detalle") val lugarDetalle: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    @SerialName("fecha_hallazgo") val fechaHallazgo: String = "",
    @SerialName("fecha_registro") val fechaRegistro: String = "",
    val estado: String = "pendiente_entrega",
    val fotos: List<String> = emptyList()
)

@Serializable
data class PerfilModel(
    val id: String,
    val email: String,
    val rol: String = "estudiante",
    @SerialName("sede_id") val sedeId: Int? = null,
    val creado: String = ""
)

@Serializable
data class SolicitudModel(
    val id: String = "",
    @SerialName("objeto_id") val objetoId: String,
    @SerialName("usuario_id") val usuarioId: String,
    val estado: String = "en_verificacion",
    val intentos: Int = 0,
    @SerialName("codigo_recojo") val codigoRecojo: String? = null,
    @SerialName("lugar_recojo") val lugarRecojo: String? = null,
    @SerialName("expira_en") val expiraEn: String? = null
)

@Serializable
data class SedeModel(
    val id: Int,
    val nombre: String
)

@Serializable
data class FacultadModel(
    val id: Int,
    val nombre: String
)

@Serializable
data class PabellonModel(
    val id: Int,
    @SerialName("sede_id") val sedeId: Int,
    val nombre: String,
    val lat: Double? = null,
    val lng: Double? = null
)
