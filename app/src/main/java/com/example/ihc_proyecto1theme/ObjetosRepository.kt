package com.example.ihc_proyecto1theme

import com.example.ihc_proyecto1theme.model.FacultadModel
import com.example.ihc_proyecto1theme.model.ObjetoModel
import com.example.ihc_proyecto1theme.model.PabellonModel
import com.example.ihc_proyecto1theme.model.PerfilModel
import com.example.ihc_proyecto1theme.model.SedeModel
import com.example.ihc_proyecto1theme.model.SolicitudModel
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.filter.PostgrestFilterBuilder
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class ObjetosRepository {

    private val db get() = SupabaseClient.client

    companion object {
        private const val BUCKET_FOTOS = "fotos-objetos"
    }

    private fun PostgrestFilterBuilder.aplicar(f: FiltrosTablon, ahora: Long) {
        isIn("estado", listOf("sin_reclamar", "reclamado"))
        f.facultadId?.let { eq("facultad_id", it) }
        f.sedeId?.let { eq("sede_id", it) }
        if (f.categorias.isNotEmpty()) {
            isIn("categoria", f.categorias.map { it.codigo })
        }
        val (desde, hasta) = rangoFechas(f, ahora)
        desde?.let { gte("fecha_hallazgo", isoUtc(it)) }
        hasta?.let { lte("fecha_hallazgo", isoUtc(it)) }
    }

    suspend fun listarTablon(f: FiltrosTablon): List<ObjetoModel> = withContext(Dispatchers.IO) {
        val ahora = System.currentTimeMillis()
        db.from("objetos").select {
            filter { aplicar(f, ahora) }
            if (f.orden == OrdenTablon.GENERAL) order("categoria", Order.ASCENDING)
            order("fecha_hallazgo", Order.DESCENDING)
            limit(100)
        }.decodeList<ObjetoModel>()
    }

    suspend fun contarTablon(f: FiltrosTablon): Long = withContext(Dispatchers.IO) {
        val ahora = System.currentTimeMillis()
        db.from("objetos").select {
            count(Count.EXACT)
            head = true
            filter { aplicar(f, ahora) }
        }.countOrNull() ?: 0L
    }

    suspend fun sedes(): List<SedeModel> = withContext(Dispatchers.IO) {
        db.from("sedes").select { order("nombre", Order.ASCENDING) }.decodeList<SedeModel>()
    }

    suspend fun facultades(): List<FacultadModel> = withContext(Dispatchers.IO) {
        db.from("facultades").select { order("nombre", Order.ASCENDING) }.decodeList<FacultadModel>()
    }

    suspend fun pabellones(): List<PabellonModel> = withContext(Dispatchers.IO) {
        db.from("pabellones").select { order("nombre", Order.ASCENDING) }.decodeList<PabellonModel>()
    }

    suspend fun subirFotoObjeto(
        bytes: ByteArray,
        mimeType: String = "image/jpeg"
    ): String = withContext(Dispatchers.IO) {
        val extension = when (mimeType) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> "jpg"
        }
        val userId = db.auth.currentUserOrNull()?.id ?: "anonimo"
        val ruta = "$userId/${UUID.randomUUID()}.$extension"
        val bucket = db.storage.from(BUCKET_FOTOS)

        bucket.upload(ruta, bytes) {
            contentType = ContentType.parse(mimeType)
        }
        bucket.publicUrl(ruta)
    }

    suspend fun registrarObjeto(objeto: ObjetoModel): Unit = withContext(Dispatchers.IO) {
        val payload = if (objeto.id.isBlank()) {
            objeto.copy(id = UUID.randomUUID().toString())
        } else {
            objeto
        }
        db.from("objetos").insert(payload)
    }

    suspend fun solicitarObjeto(objetoId: String): Unit = withContext(Dispatchers.IO) {
        val userId = db.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("Debes iniciar sesión para reclamar un objeto.")

        // Evita que el mismo estudiante envíe solicitudes duplicadas por el mismo objeto
        val previas = db.from("solicitudes").select {
            filter {
                eq("objeto_id", objetoId)
                eq("usuario_id", userId)
            }
        }.decodeList<SolicitudModel>()

        if (previas.isNotEmpty()) {
            throw IllegalStateException("Ya enviaste una solicitud para este objeto. Revisa la pestaña Solicitudes.")
        }

        val nuevaSolicitud = SolicitudModel(
            id = UUID.randomUUID().toString(),
            objetoId = objetoId,
            usuarioId = userId,
            estado = "en_verificacion",
            intentos = 0
        )
        db.from("solicitudes").insert(nuevaSolicitud)
    }

    suspend fun misSolicitudes(): List<SolicitudConObjeto> = withContext(Dispatchers.IO) {
        val userId = db.auth.currentUserOrNull()?.id ?: return@withContext emptyList()
        val solicitudes = db.from("solicitudes").select {
            filter { eq("usuario_id", userId) }
        }.decodeList<SolicitudModel>()

        if (solicitudes.isEmpty()) return@withContext emptyList()

        val objetoIds = solicitudes.map { it.objetoId }.distinct()
        val objetos = db.from("objetos").select {
            filter { isIn("id", objetoIds) }
        }.decodeList<ObjetoModel>().associateBy { it.id }

        solicitudes.map { sol ->
            SolicitudConObjeto(
                solicitud = sol,
                objeto = objetos[sol.objetoId]
            )
        }
    }
}

class PerfilRepository {

    suspend fun miPerfil(): PerfilModel? = withContext(Dispatchers.IO) {
        val id = SupabaseClient.client.auth.currentUserOrNull()?.id ?: return@withContext null
        SupabaseClient.client.from("perfiles").select {
            filter { eq("id", id) }
        }.decodeSingleOrNull<PerfilModel>()
    }
}

data class SolicitudConObjeto(
    val solicitud: SolicitudModel,
    val objeto: ObjetoModel?
)