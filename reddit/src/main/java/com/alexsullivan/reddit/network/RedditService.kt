package com.alexsullivan.reddit.network

import com.alexsullivan.reddit.models.RedditListingResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

internal interface RedditService {
    companion object {
        const val baseUrl = "https://oauth.reddit.com/"
    }

    @GET("r/gifrecipes/hot")
    fun hotRecipes(@Query("limit") limit: Int = 100, @Query("after") after: String? = ""): Observable<RedditListingResponse>

    @GET("r/gifrecipes/search")
    fun searchRecipes(@Query("q") searchParam: String, @Query("after") after: String? = "",
                      @Query("limit") limit: Int = 100, @Query("restrict_sr") restrict: Boolean? = true,
                      @Query("sort") sort: String = "hot", @Query("raw_json") useRawJson: Int = 1): Observable<RedditListingResponse>
}