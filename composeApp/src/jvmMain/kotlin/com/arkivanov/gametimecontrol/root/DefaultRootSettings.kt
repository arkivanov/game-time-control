package com.arkivanov.gametimecontrol.root

import java.util.prefs.Preferences

object DefaultRootSettings : RootSettings {

    private const val KEY_PIN_CODE = "pin_code"
    private const val KEY_IS_VOICE_ENABLED = "is_voice_enabled"
    private val preferences = Preferences.userNodeForPackage(DefaultRootSettings::class.java)

    override var pinCode: String
        get() = preferences.get(KEY_PIN_CODE, "0000")
        set(value) {
            preferences.put(KEY_PIN_CODE, value)
        }

    override var isVoiceEnabled: Boolean
        get() = preferences.getBoolean(KEY_IS_VOICE_ENABLED, false)
        set(value) {
            preferences.putBoolean(KEY_IS_VOICE_ENABLED, value)
        }
}
