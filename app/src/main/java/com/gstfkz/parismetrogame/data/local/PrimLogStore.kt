package com.gstfkz.parismetrogame.data.local

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Persistent append-only PRIM diagnostic log, retained until app data is cleared. */
class PrimLogStore(context: Context) {
    private val file = File(context.applicationContext.filesDir, "prim_api.log")
    private val lock = Any()
    fun append(message: String) {
        val stamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(Date())
        synchronized(lock) {
            file.appendText("[$stamp] $message\n")
        }
    }
    fun readAll(): String = synchronized(lock) {
        if (file.exists()) file.readText() else ""
    }
}
