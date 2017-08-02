package alexsullivan.gifrecipes.database

import android.arch.persistence.room.Room
import android.content.Context

class RoomRecipeDatabaseHolder(val context: Context) {

    companion object {
        var database: RoomRecipeDatabase? = null

        fun get(context: Context): RecipeDatabase {
            if (database == null) {
                database = Room.databaseBuilder(context.applicationContext, RoomRecipeDatabase::class.java, "database").build()
            }
            return database!!
        }
    }
}