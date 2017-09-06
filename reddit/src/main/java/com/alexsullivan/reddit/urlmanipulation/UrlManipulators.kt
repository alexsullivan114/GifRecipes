package com.alexsullivan.reddit.urlmanipulation

import com.alexsullivan.ImageType
import com.alexsullivan.reddit.models.RedditGifRecipe
import com.gfycat.GfycatRepository
import com.imgur.ImgurRepository
import io.reactivex.Observable
import java.util.regex.Pattern

internal class GfycatUrlManipulator(private val repository: GfycatRepository): UrlManipulator {

    override fun matchesDomain(domain: String):Boolean {
        val regexString = "^(https:\\/\\/)?(www\\.)?[A-Za-z]*\\.?gfycat\\.com\\/.*"
        val pattern = Pattern.compile(regexString)
        val matcher = pattern.matcher(domain)
        return matcher.matches()
    }

    override fun modifyRedditItem(item: RedditGifRecipe): Observable<RedditGifRecipe> {
        val gfycatId = item.url.substringAfter(".com/").substringBefore(".")
        val thumbnailId = "http://thumbs.gfycat.com/$gfycatId-poster.jpg"
        return repository.getImageInfo(gfycatId)
            .map { item.copy(url = it.mp4, imageType = ImageType.VIDEO, thumbnail = thumbnailId) }
            .onErrorReturnItem(item)
    }
}

 internal class ImgurUrlManipulator(private val repository: ImgurRepository) : UrlManipulator {
    override fun matchesDomain(domain: String):Boolean {
        val regexString = "(https:\\/\\/)?(www\\.)?(i\\.)?imgur.com\\/.*"
        val pattern = Pattern.compile(regexString)
        val matcher = pattern.matcher(domain)
        return matcher.matches()
    }

    override fun modifyRedditItem(item: RedditGifRecipe): Observable<RedditGifRecipe> {
        val imgurId = item.url.substringAfter(".com/").substringBefore(".")
        val thumbnail = "http://i.imgur.com/${imgurId}l.jpg"
        return repository.getImageInfo(imgurId)
                .map { item.copy(url = it.mp4, imageType = ImageType.VIDEO, thumbnail = thumbnail) }
                .onErrorReturnItem(item)
    }
}