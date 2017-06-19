package com.alexsullivan.reddit.models

import com.alexsullivan.ImageType

/**
 * Created by Alexs on 5/10/2017.
 */
internal data class RedditGifRecipe(val url: String, val id: String, val imageType: ImageType,
                                    val thumbnail: String?, val previewUrl: String?, val domain: String, val title: String)