package space.celestia.MobileCelestia.Utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import java.lang.RuntimeException

class PreferenceManager(private val context: Context, private val name: String) {
    private val sp: SharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE)
    private var et: SharedPreferences.Editor? = null

    interface Key {
        val valueString: String
    }

    enum class PredefinedKey : Key {
        DataVersion;

        override val valueString: String
            get() = toString()
    }

    class CustomKey(val key: String) : Key {
        override val valueString: String
            get() = key
    }

    operator fun get(key: Key): String? {
        return sp.getString(key.valueString, null)
    }

    @SuppressLint("CommitPrefEdits")
    public fun startEditing() {
        if (et != null)
            throw RuntimeException("Editing context already exists when calling start.")
        et = sp.edit()
    }

    public fun stopEditing() {
        if (et == null)
            throw RuntimeException("No current editing context when calling stop.")
        et!!.apply()
    }

    @SuppressLint("CommitPrefEdits")
    operator fun set(key: Key, value: String?) {
        val needEditor = et == null

        val editor = if (needEditor) sp.edit() else et
        editor?.putString(key.valueString, value)

        if (needEditor) { editor?.apply() }
    }
}