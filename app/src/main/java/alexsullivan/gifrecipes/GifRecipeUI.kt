package alexsullivan.gifrecipes

import android.os.Parcel
import android.os.Parcelable
import com.alexsullivan.ImageType

// A slimmed down version of {@Link GifRecipe} that implements parcelable and can be passed around Android
// intents.
data class GifRecipeUI(val url: String, val thumbnail: String?, val imageType: ImageType, val title: String) : Parcelable {
    constructor(parcel: Parcel) : this(parcel.readString(), parcel.readString(), parcel.readSerializable() as ImageType, parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(url)
        parcel.writeString(thumbnail)
        parcel.writeSerializable(imageType)
        parcel.writeString(title)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<GifRecipeUI> {
        override fun createFromParcel(parcel: Parcel): GifRecipeUI {
            return GifRecipeUI(parcel)
        }

        override fun newArray(size: Int): Array<GifRecipeUI?> {
            return arrayOfNulls(size)
        }
    }
}