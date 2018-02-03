package alexsullivan.gifrecipes.favoriting

import alexsullivan.gifrecipes.database.RoomRecipeDatabaseHolder
import android.content.Context

fun buildRoomFavoriteCache(context: Context): RoomFavoriteCache {
  val dao = RoomRecipeDatabaseHolder.get(context.applicationContext).gifRecipeDao()
  return RoomFavoriteCache.getInstance(dao)
}