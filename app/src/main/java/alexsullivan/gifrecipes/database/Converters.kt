package alexsullivan.gifrecipes.database

import android.arch.persistence.room.TypeConverter
import com.alexsullivan.ImageType

object Converters {

    @TypeConverter
    @JvmStatic
    fun fromValue(value: Int) = ImageType.fromInt(value)

    @TypeConverter
    @JvmStatic
    fun toValue(imageType: ImageType) = imageType.value

}