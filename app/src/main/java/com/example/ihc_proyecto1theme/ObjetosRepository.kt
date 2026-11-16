package com.example.ihc_proyecto1theme

import com.example.ihc_proyecto1theme.model.FacultadModel
import com.example.ihc_proyecto1theme.model.ObjetoModel
import com.example.ihc_proyecto1theme.model.PabellonModel
import com.example.ihc_proyecto1theme.model.PerfilModel
import com.example.ihc_proyecto1theme.model.SedeModel
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.filter.PostgrestFilterBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ObjetosRepository {

    private val db get() = SupabaseClient.client

    /** El tablón solo muestra objetos publicados (sin reclamar o reclamados). */
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
            // "General" agrupa por categoría; "Recientes" solo por fecha
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
}

class PerfilRepository {

    suspend fun miPerfil(): PerfilModel? = withContext(Dispatchers.IO) {
        val id = SupabaseClient.client.auth.currentUserOrNull()?.id ?: return@withContext null
        SupabaseClient.client.from("perfiles").select {
            filter { eq("id", id) }
        }.decodeSingleOrNull<PerfilModel>()
    }
}
