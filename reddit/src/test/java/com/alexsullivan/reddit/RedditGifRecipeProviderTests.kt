package com.alexsullivan.reddit

import com.alexsullivan.reddit.models.RedditListingItem
import com.alexsullivan.reddit.models.RedditListingResponse
import com.alexsullivan.reddit.models.RedditListingResponseData
import com.alexsullivan.reddit.network.RedditService
import com.alexsullivan.reddit.testutils.EmptyLogger
import com.alexsullivan.reddit.urlmanipulation.UrlManipulator
import io.reactivex.Observable
import org.junit.Test

/**
 * Created by Alexs on 8/31/2017.
 */
class RedditGifRecipeProviderTests {
    @Test fun simpleResponse() {
        val service = object: RedditService {
            override fun hotRecipes(limit: Int, after: String?): Observable<RedditListingResponse> {
                val item1 = RedditListingItem("t3", "testId1", "url1", "Gfycat", "", "", "Test title 1", "pageKey1", false)
                val item2 = RedditListingItem("t3", "testId2", "url2", "Gfycat", "", "", "Test title 2", "pageKey2", false)
                val item3 = RedditListingItem("t3", "testId3", "url3", "Gfycat", "", "", "Test title 3", "pageKey3", false)
                val item4 = RedditListingItem("t3", "testId4", "url4", "Gfycat", "", "", "Test title 4", "pageKey4", false)
                val item5 = RedditListingItem("t3", "testId5", "url5", "Gfycat", "", "", "Test title 5", "pageKey5", false)
                val responeData = RedditListingResponseData("", listOf(item1, item2, item3, item4, item5), "", "")
                val response = RedditListingResponse("post", responeData)
                return Observable.just(response)
            }

            override fun searchRecipes(searchParam: String, after: String?, limit: Int, restrict: Boolean?, sort: String, useRawJson: Int): Observable<RedditListingResponse> {
                val item1 = RedditListingItem("t3", "testId1", "url1", "Gfycat", "", "", "Test title 1", "pageKey1", false)
                val item2 = RedditListingItem("t3", "testId2", "url2", "Gfycat", "", "", "Test title 2", "pageKey2", false)
                val item3 = RedditListingItem("t3", "testId3", "url3", "Gfycat", "", "", "Test title 3", "pageKey3", false)
                val item4 = RedditListingItem("t3", "testId4", "url4", "Gfycat", "", "", "Test title 4", "pageKey4", false)
                val item5 = RedditListingItem("t3", "testId5", "url5", "Gfycat", "", "", "Test title 5", "pageKey5", false)
                val responeData = RedditListingResponseData("", listOf(item1, item2, item3, item4, item5), "", "")
                val response = RedditListingResponse("post", responeData)
                return Observable.just(response)
            }
        }

        val urlManipulators = listOf<UrlManipulator>()
        val mediaChecker = fun(_: String) = true
        val logger = EmptyLogger

        val gifRecipeProvider = RedditGifRecipeProviderImpl(service, urlManipulators, mediaChecker, logger)
    }
}