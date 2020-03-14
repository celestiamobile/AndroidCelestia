package space.celestia.MobileCelestia.Utils

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager {
    private val context: Context
    private val name: String
    private val sp: SharedPreferences

    constructor(context: Context, name: String) {
        this.context = context
        this.name = name
        this.sp = context.getSharedPreferences(name, Context.MODE_PRIVATE)
    }

    enum class Preference {
        AssetCopied
    }

    operator fun get(key: Preference): String? {
        return sp.getString(key.toString(), null)
    }

    operator fun set(key: Preference, value: String?) {
        val editor = sp.edit()
        editor.putString(key.toString(), value)
        editor.apply()
    }
}