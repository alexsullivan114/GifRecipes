package alexsullivan.com.gfycat

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

internal interface GfycatService {
    object Statics {
        val baseUrl = "https://api.gfycat.com/v1/gfycats/"
        const val clientId = "2_ntJYHc"
    }

    @GET("trending")
    fun fetchRecipes(@Query("tagName") tagName: String = "gifrecipes",
                     @Query("gfyCount") limit: Int = 100,
                     @Query("cursor") pageKey: String? = ""): Observable<GfycatApiResponse>
}