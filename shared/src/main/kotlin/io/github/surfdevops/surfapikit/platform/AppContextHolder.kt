package io.github.surfdevops.surfapikit.platform

import android.content.Context
import androidx.startup.Initializer

object AppContextHolder {
    @Volatile
    private var _context: Context? = null

    val context: Context
        get() = _context
            ?: error("AppContextHolder não inicializado. Verifique se androidx.startup está habilitado (deve ser automático ao adicionar a dependência).")

    internal fun set(ctx: Context) {
        if (_context == null) _context = ctx.applicationContext
    }
}

class SurfApiKitInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        AppContextHolder.set(context)
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> = mutableListOf()
}
