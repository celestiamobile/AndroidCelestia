package space.celestia.MobileCelestia.Utils

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(private val context: Context, private val name: String) {
    private val sp: SharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE)

    enum class Preference {
        DataVersion
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