package alexsullivan.gifrecipes.preferences

import android.content.Context
import android.content.SharedPreferences

// TODO: If we use this class more, use all the kotlin delegate goodness to make it not bad.
object RecipePreferences: Preferences {

    private var sharedPrefs: SharedPreferences? = null

    fun init(context: Context) {
        sharedPrefs = context.applicationContext.getSharedPreferences("GifRecipesPreferences", Context.MODE_PRIVATE)
    }

    override var deviceId: String
        get() = sharedPrefs!!.getString("deviceId", "")
        set(id) = sharedPrefs!!.edit().putString("deviceId", id).apply()
}