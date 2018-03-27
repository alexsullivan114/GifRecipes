package alexsullivan.com.gfycat

import com.alexsullivan.GifRecipeProvider
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

fun createGfycatRecipeProvider(): GifRecipeProvider {
  val retrofit = Retrofit.Builder()
      .baseUrl(GfycatService.Statics.baseUrl)
      .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
      .addConverterFactory(GsonConverterFactory.create())
      .build()
  val service = retrofit.create(GfycatService::class.java)
  return GfycatRecipeProvider(service)
}