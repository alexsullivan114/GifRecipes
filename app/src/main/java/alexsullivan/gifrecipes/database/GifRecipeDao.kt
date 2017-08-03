package alexsullivan.gifrecipes.database

import io.reactivex.Flowable

interface GifRecipeDao {
    fun insertFavoriteRecipe(gifRecipe: FavoriteRecipe)
    fun deleteFavoriteRecipe(gifRecipe: FavoriteRecipe)
    fun findFavorites(ids: List<String>): Flowable<String>
    fun recipeIsFavoritedStream(id: String): Flowable<Boolean>
    fun isRecipeFavorited(id: String): Boolean
}