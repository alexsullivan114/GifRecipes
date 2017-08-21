package alexsullivan.gifrecipes.database

import alexsullivan.gifrecipes.favoriting.FavoriteRecipe
import io.reactivex.Flowable

interface GifRecipeDao {
    fun insertFavoriteRecipe(gifRecipe: FavoriteRecipe)
    fun deleteFavoriteRecipe(gifRecipe: FavoriteRecipe)
    fun findFavorites(ids: List<String>): Flowable<String>
    fun findFavorites(): List<FavoriteRecipe>
    fun recipeIsFavoritedStream(id: String): Flowable<Boolean>
    fun isRecipeFavorited(id: String): Boolean
}