package alexsullivan.gifrecipes.database

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.alexsullivan.ImageType


@Entity(tableName = "favorites") class FavoriteRecipe {
    var url: String? = null
    @PrimaryKey var id: String? = null
    var thumbnail: String? = null
    var imageType: ImageType? = null
    var title: String? = null
}