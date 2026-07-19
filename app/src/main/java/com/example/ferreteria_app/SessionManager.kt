package com.example.ferreteria_app

import android.content.Context
import android.content.SharedPreferences

object SessionManager {

    private const val PREFS_NAME = "ferremax_prefs"
    private const val KEY_REPARTIDOR_ID = "repartidor_id"
    private const val KEY_REPARTIDOR_NOMBRE = "repartidor_nombre"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getRepartidorId(context: Context): String {
        return getPrefs(context).getString(KEY_REPARTIDOR_ID, "") ?: ""
    }

    fun getRepartidorNombre(context: Context): String {
        return getPrefs(context).getString(KEY_REPARTIDOR_NOMBRE, "") ?: ""
    }

    fun guardarSesionRepartidor(context: Context, id: String, nombre: String) {
        getPrefs(context).edit()
            .putString(KEY_REPARTIDOR_ID, id)
            .putString(KEY_REPARTIDOR_NOMBRE, nombre)
            .apply()
    }

    fun sesionRepartidorActiva(context: Context): Boolean {
        return getRepartidorId(context).isNotEmpty()
    }

    fun cerrarSesionRepartidor(context: Context) {
        getPrefs(context).edit()
            .remove(KEY_REPARTIDOR_ID)
            .remove(KEY_REPARTIDOR_NOMBRE)
            .apply()
    }
}
