package alexsullivan.gifrecipes.database

import alexsullivan.gifrecipes.favoriting.FavoriteRecipe
import com.alexsullivan.GifRecipe

fun GifRecipe.toFavorite(): FavoriteRecipe {
    val recipe = FavoriteRecipe()
    recipe.url = url
    recipe.id = id
    recipe.thumbnail = thumbnail
    recipe.imageType = imageType
    recipe.title = title
    return recipe
}

fun FavoriteRecipe.toGifRecipe() =
    GifRecipe(url!!, id, thumbnail!!, imageType!!, title!!, recipeSourceThumbnail!!)