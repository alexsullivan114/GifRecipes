package com.alexsullivan

fun createRecipe(url: String = "",
                 id: String = "",
                 thumbnail: String = "",
                 imageType: ImageType = ImageType.VIDEO,
                 title: String = "",
                 sourceThumbnail: Int = 0,
                 recipeSourceLink: String = "",
                 creationDate: Long = 0): GifRecipe = GifRecipe(url, id, thumbnail, imageType, title, sourceThumbnail, recipeSourceLink, creationDate)