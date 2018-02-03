package alexsullivan.gifrecipes.utils.datasourceutils

import alexsullivan.gifrecipes.GifRecipeUI
import alexsullivan.gifrecipes.favoriting.FavoriteCache
import android.arch.paging.DataSource

interface GifDataSourceFactory {
  fun create(searchTerm: String, favoriteCache: FavoriteCache): Pair<DataSource<*, GifRecipeUI>, DataSourceErrorProvider>
}