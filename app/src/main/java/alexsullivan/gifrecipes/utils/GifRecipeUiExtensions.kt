package alexsullivan.gifrecipes.utils

import alexsullivan.gifrecipes.GifRecipeUI
import com.alexsullivan.GifRecipe
import com.alexsullivan.ImageType

fun GifRecipeUI.toGifRecipe() = GifRecipe(url, id, thumbnail, imageType, title, recipeSourceThumbnail, recipeSourceLink, creationDate)

fun GifRecipe.toGifRecipeUI(favorited: Boolean) = GifRecipeUI(url, id, thumbnail, imageType, title, favorited, recipeSourceThumbnail, recipeSourceLink, creationDate)

fun GifRecipeUI.previewImageUrl(): String {
  if (!thumbnail.isNullOrBlank()) {
    return thumbnail!!
  } else if (imageType == ImageType.GIF) {
    return url
  } else {
    return ""
  }
}