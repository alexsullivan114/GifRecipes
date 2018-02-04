package com.alexsullivan

data class GifRecipe (val url: String, val id: String, val thumbnail: String?,
                      val imageType: ImageType, val title: String, val pageKey: String?)

enum class ImageType(val value: Int) {
    GIF(0),
    VIDEO(1);

    companion object {
        private val map = ImageType.values().associateBy(ImageType::value);
        fun fromInt(type: Int): ImageType {
            if (map[type] == null) {
                throw IllegalArgumentException("Can't create ImageType from value $type")
            }

            return map[type]!!
        }
    }

    fun isGif() = this == ImageType.GIF
}