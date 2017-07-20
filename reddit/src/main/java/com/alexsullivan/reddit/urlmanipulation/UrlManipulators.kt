package com.alexsullivan.reddit.urlmanipulation

import com.alexsullivan.ImageType
import com.alexsullivan.logging.Logger
import com.alexsullivan.reddit.models.RedditGifRecipe
import com.gfycat.GfycatRepository
import com.gfycat.ImgurRepository
import io.reactivex.Observable

internal class GfycatUrlManipulator(val logger: Logger): UrlManipulator {

    override fun matchesDomain(domain: String): Boolean {
        return domain.contains("gfycat.com")
    }

    override fun modifyRedditItem(item: RedditGifRecipe): Observable<RedditGifRecipe> {
        val gfycatId = item.url.substringAfter(".com/").substringBefore(".")
        return GfycatRepository.create(logger).getImageInfo(gfycatId)
                .map { item.copy(url = it.mp4, imageType = ImageType.VIDEO) }
    }
}

internal class ImgurUrlManipulator(val logger: Logger): UrlManipulator {
    override fun matchesDomain(domain: String): Boolean {
        return domain.contains("imgur.com")
    }

    override fun modifyRedditItem(item: RedditGifRecipe): Observable<RedditGifRecipe> {
        val imgurId = item.url.substringAfter(".com/").substringBefore(".")
        return ImgurRepository.create(logger).getImageInfo(imgurId)
                .map { item.copy(url = it.mp4, imageType = ImageType.VIDEO) }
    }
}