package com.alexsullivan

data class GifRecipe (val url: String, val id: String, val thumbnail: String?,
                      val previewUrl: String?, val imageType: ImageType)

enum class ImageType {GIF, VIDEO}