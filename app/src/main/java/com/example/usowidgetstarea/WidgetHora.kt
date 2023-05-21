package com.example.usowidgetstarea

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.widget.RemoteViews
import java.text.SimpleDateFormat
import java.util.*

/**
 * Implementation of App Widget functionality.
 */
class WidgetHora : AppWidgetProvider() {
    private var timer: Timer? = null
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            actualizarTiempo(context, appWidgetManager, appWidgetId)
        }

        // Iniciar temporizador para actualizar periódicamente
        if (timer == null) {
            timer = Timer()
            timer?.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    for (appWidgetId in appWidgetIds) {
                        actualizarTiempo(context, appWidgetManager, appWidgetId)
                    }
                }
            }, 0, 60000) // Actualizar cada minuto (60000 milisegundos)
        }
    }

    override fun onDisabled(context: Context) {
        // Detener el temporizador cuando se eliminen todos los widgets
        timer?.cancel()
        timer = null
    }
    private fun obtenerLocalidadActual(context: Context): String {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses: List<Address> = geocoder.getFromLocation(-16.409047, -71.537450, 1)
            ?.toList()
            ?: emptyList()
        if (addresses.isNotEmpty()) {
            val address: Address = addresses[0]
            return address.locality ?: "Desconocida"
        }

        return "Desconocida"
    }
    private fun obtenerTemperaturaActual(context: Context): String {
        val contentResolver: ContentResolver = context.contentResolver
        val uri: Uri = Uri.parse("content://com.android.weather.provider/weather")
        val projection: Array<String> = arrayOf("temperature")
        var temperatura: String = "Desconocida"

        val cursor: Cursor? = contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex: Int = it.getColumnIndex("temperature")
                if (columnIndex != -1) {
                    temperatura = it.getString(columnIndex)
                }
            }
        }

        return temperatura
    }



    private fun actualizarTiempo(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_hora)
        val formatoTiempo = SimpleDateFormat("HH:mm a", Locale.getDefault())
        val formatoFecha = SimpleDateFormat("EEEE, dd/MM/yyyy", Locale.getDefault())
        val tiempoActual = formatoTiempo.format(Date())
        val fechaActual = formatoFecha.format(Date())
        val localidadActual = obtenerLocalidadActual(context)




        remoteViews.setTextViewText(R.id.lblhora, tiempoActual)
        remoteViews.setTextViewText(R.id.lbldiafecha, fechaActual)
        remoteViews.setTextViewText(R.id.lblciudad, localidadActual)

        appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
    }







}