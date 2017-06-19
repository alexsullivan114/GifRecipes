package com.alexsullivan

data class GifRecipe (val url: String, val id: String, val thumbnail: String?,
                      val previewUrl: String?, val imageType: ImageType, val title: String)

enum class ImageType {GIF, VIDEO}