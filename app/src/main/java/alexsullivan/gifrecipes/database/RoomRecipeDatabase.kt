package alexsullivan.gifrecipes.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters

@Database(entities = arrayOf(FavoriteRecipe::class), version = 1)
@TypeConverters(Converters::class)
abstract class RoomRecipeDatabase: RoomDatabase(), RecipeDatabase {

    abstract fun roomGifRecipeDao(): RoomGifRecipeDao

    override fun gifRecipeDao() = roomGifRecipeDao()
}