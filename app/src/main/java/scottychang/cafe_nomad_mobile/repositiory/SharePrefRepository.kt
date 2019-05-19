package scottychang.cafe_nomad_mobile.repositiory

import android.content.Context
import scottychang.cafe_nomad_mobile.model.TwCity

class SharePrefRepository {
    private val PREFERENCE_NAME = "SharePrefRepository"
    private val KEY_CITY = "city"

    companion object {
        @Volatile private var INSTANCE: SharePrefRepository? = null

        fun getInstance(): SharePrefRepository = INSTANCE ?: synchronized(this) {
            INSTANCE ?: SharePrefRepository().also { INSTANCE = it }
        }
    }

    fun save(context: Context, twCity: TwCity) {
        val editor = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE).edit()
        editor.putInt(KEY_CITY, twCity.ordinal)
        editor.apply()
    }

    fun load(context: Context): TwCity {
        val sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        val index = sharedPreferences.getInt(KEY_CITY, -1)
        return if (index >= 0) TwCity.values()[index] else TwCity.UNKNOWN
    }
}