package com.example.ihc_proyecto1theme

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

private val UTC: TimeZone = TimeZone.getTimeZone("UTC")
private val LIMA: TimeZone = TimeZone.getTimeZone("America/Lima")
private val LOCALE_PE: Locale = Locale.forLanguageTag("es-PE")
private const val MINUTO_MS = 60_000L
private const val DIA_MS = 24 * 60 * MINUTO_MS

/** Convierte "2026-11-15T10:20:30.123456+00:00" en milisegundos. */
fun parsearFecha(iso: String): Long? {
    if (iso.length < 19) return null
    return try {
        val formato = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
            timeZone = UTC
            isLenient = false
        }
        val base = formato.parse(iso.substring(0, 19).replace(' ', 'T'))?.time ?: return null
        val offset = Regex("([+-])(\\d{2}):?(\\d{2})$").find(iso.substring(19))?.let {
            val signo = if (it.groupValues[1] == "-") -1 else 1
            signo * (it.groupValues[2].toLong() * 60 * MINUTO_MS + it.groupValues[3].toLong() * MINUTO_MS)
        } ?: 0L
        base - offset
    } catch (e: Exception) {
        null
    }
}

fun fechaRelativa(iso: String, ahora: Long = System.currentTimeMillis()): String {
    val t = parsearFecha(iso) ?: return ""
    val minutos = (ahora - t) / MINUTO_MS
    return when {
        minutos < 1 -> "Hace un momento"
        minutos < 60 -> "Hace $minutos min"
        minutos < 60 * 24 -> "Hace ${minutos / 60} h"
        minutos < 60 * 24 * 2 -> "Ayer"
        minutos < 60 * 24 * 7 -> "Hace ${minutos / (60 * 24)} días"
        else -> SimpleDateFormat("dd/MM/yyyy", LOCALE_PE).apply { timeZone = LIMA }.format(t)
    }
}

/** Formato ISO en UTC para filtrar en Supabase. */
fun isoUtc(ms: Long): String =
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply { timeZone = UTC }.format(ms)

/** Las fechas del selector llegan como medianoche UTC. */
fun formatoFechaSelector(ms: Long): String =
    SimpleDateFormat("dd/MM/yyyy", LOCALE_PE).apply { timeZone = UTC }.format(ms)

private fun inicioDiaLima(ms: Long): Long =
    Calendar.getInstance(LIMA).apply {
        timeInMillis = ms
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

private fun inicioDiaLimaDeFechaSelector(msUtc: Long): Long {
    val u = Calendar.getInstance(UTC).apply { timeInMillis = msUtc }
    return Calendar.getInstance(LIMA).apply {
        clear()
        set(u.get(Calendar.YEAR), u.get(Calendar.MONTH), u.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
    }.timeInMillis
}

/** Devuelve (desde, hasta) en milisegundos según el filtro de fecha elegido. */
fun rangoFechas(f: FiltrosTablon, ahora: Long): Pair<Long?, Long?> = when (f.fecha) {
    FiltroFecha.TODAS -> null to null
    FiltroFecha.HOY -> inicioDiaLima(ahora) to null
    FiltroFecha.SIETE_DIAS -> (ahora - 7 * DIA_MS) to null
    FiltroFecha.TREINTA_DIAS -> (ahora - 30 * DIA_MS) to null
    FiltroFecha.RANGO -> {
        val desde = f.rangoDesdeMs
        val hasta = f.rangoHastaMs ?: desde
        if (desde == null || hasta == null) {
            null to null
        } else {
            inicioDiaLimaDeFechaSelector(desde) to (inicioDiaLimaDeFechaSelector(hasta) + DIA_MS - 1)
        }
    }
}

/** Las fotos pueden guardarse como URL completa o como ruta dentro del bucket. */
fun urlFoto(referencia: String): String =
    if (referencia.startsWith("http")) {
        referencia
    } else {
        BuildConfig.SUPABASE_URL.trimEnd('/') +
            "/storage/v1/object/public/fotos-objetos/" + referencia.trimStart('/')
    }
