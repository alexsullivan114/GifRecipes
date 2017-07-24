package com.alexsullivan

data class GifRecipe (val url: String, val id: String, val thumbnail: String?,
                      val imageType: ImageType, val title: String, val pageKey: String?)

enum class ImageType {GIF, VIDEO}