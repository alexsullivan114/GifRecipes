package alexsullivan.gifrecipes.database

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import io.reactivex.Flowable

@Dao
abstract class RoomGifRecipeDao : GifRecipeDao {

    @Insert
    abstract fun insertRoomFavorite(gifRecipe: FavoriteRecipe)

    @Delete
    abstract fun deleteRoomFavorite(gifRecipe: FavoriteRecipe)

    @Query("SELECT id FROM favorites WHERE id IN (:ids)")
    abstract fun findRoomFavorites(ids: List<String>): Flowable<String>

    @Query("SELECT count(1) FROM favorites WHERE id = :id")
    abstract fun gifRecipeIdInFavoritesInternal(id: String): Flowable<Int>

    override fun insertFavoriteRecipe(gifRecipe: FavoriteRecipe) = insertRoomFavorite(gifRecipe)

    override fun deleteFavoriteRecipe(gifRecipe: FavoriteRecipe) = deleteRoomFavorite(gifRecipe)

    override fun findFavorites(ids: List<String>): Flowable<String> = findRoomFavorites(ids)

    override fun recipeIsFavorited(id: String): Flowable<Boolean> {
        return gifRecipeIdInFavoritesInternal(id)
                .map { it == 1 }
    }
}