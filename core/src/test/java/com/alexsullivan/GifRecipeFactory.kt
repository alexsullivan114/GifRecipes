package com.alexsullivan

fun createRecipe(url: String = "",
                 id: String = "",
                 thumbnail: String = "",
                 imageType: ImageType = ImageType.VIDEO,
                 title: String = ""): GifRecipe = GifRecipe(url, id, thumbnail, imageType, title)