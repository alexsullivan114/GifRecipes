package alexsullivan.gifrecipes.database

import alexsullivan.gifrecipes.favoriting.FavoriteRecipe
import android.arch.paging.DataSource
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

  @Query("SELECT count(1) FROM favorites WHERE id = :id")
  abstract fun isRecipeFavoritedInternal(id: String): Int

  @Query("SELECT * FROM favorites")
  abstract fun findFavoritesInternal(): List<FavoriteRecipe>

  @Query("SELECT * FROM favorites")
  abstract fun test(): DataSource.Factory<Int, FavoriteRecipe>

  @Query("SELECT * FROM favorites LIMIT :limit OFFSET :offset")
  abstract fun findFavoritesInternal(offset: Int, limit: Int): Flowable<List<FavoriteRecipe>>

  override fun findFavorites(offset: Int, limit: Int) = findFavoritesInternal(offset, limit)

  override fun findFavorites(): List<FavoriteRecipe> = findFavoritesInternal()

  override fun insertFavoriteRecipe(gifRecipe: FavoriteRecipe) = insertRoomFavorite(gifRecipe)

  override fun deleteFavoriteRecipe(gifRecipe: FavoriteRecipe) = deleteRoomFavorite(gifRecipe)

  override fun findFavorites(ids: List<String>): Flowable<String> = findRoomFavorites(ids)

  override fun recipeIsFavoritedStream(id: String): Flowable<Boolean> {
    return gifRecipeIdInFavoritesInternal(id)
        .map { it == 1 }
  }

  override fun isRecipeFavorited(id: String): Boolean {
    return isRecipeFavoritedInternal(id) == 1
  }
}