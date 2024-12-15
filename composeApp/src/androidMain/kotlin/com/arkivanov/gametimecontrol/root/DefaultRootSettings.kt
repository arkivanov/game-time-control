package com.arkivanov.gametimecontrol.root

import android.content.Context
import androidx.core.content.edit

class DefaultRootSettings(
    context: Context,
) : RootSettings {

    private val prefs = context.getSharedPreferences("RootSettings", Context.MODE_PRIVATE)

    override var host: String?
        get() = prefs.getString(KEY_HOST, null)
        set(value) {
            prefs.edit { putString(KEY_HOST, value) }
        }

    private companion object {
        private const val KEY_HOST = "host"
    }
}
