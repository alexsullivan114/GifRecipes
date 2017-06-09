package com.alexsullivan.reddit.urlmanipulation

import com.alexsullivan.ImageType
import com.alexsullivan.reddit.models.RedditGifRecipe
import com.example.ImgurRepository
import io.reactivex.Observable

internal class GfycatUrlManipulator: UrlManipulator {

    override fun matchesDomain(domain: String): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun modifyRedditItem(item: RedditGifRecipe): Observable<RedditGifRecipe> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

internal class ImgurUrlManipulator: UrlManipulator {
    override fun matchesDomain(domain: String): Boolean {
        return domain.contains("imgur.com")
    }

    override fun modifyRedditItem(item: RedditGifRecipe): Observable<RedditGifRecipe> {
        val imgurId = item.url.substringAfter(".com/").substringBefore(".")
        return ImgurRepository.create().getImageInfo(imgurId)
                .map { item.copy(url = it.mp4, imageType = ImageType.VIDEO) }
    }
}