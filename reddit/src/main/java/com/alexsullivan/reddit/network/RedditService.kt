package com.alexsullivan.reddit.network

import com.alexsullivan.reddit.models.RedditListingResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by Alexs on 5/10/2017.
 */
internal interface RedditService {
    object statics {
        val baseUrl = "https://oauth.reddit.com/"
    }

    @GET("r/gifrecipes/hot")
    fun hotRecipes(@Query("limit") limit: Int = 100, @Query("after") after: String? = ""): Observable<RedditListingResponse>

    @GET("r/gifrecipes/search")
    fun searchRecipes(@Query("q") searchParam: String, @Query("after") after: String? = "",
                      @Query("limit") limit: Int = 100, @Query("restrict_sr") restrict: Boolean? = true): Observable<RedditListingResponse>
}