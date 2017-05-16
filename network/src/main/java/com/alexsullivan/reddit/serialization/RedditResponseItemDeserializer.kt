package com.alexsullivan.reddit.serialization

import com.alexsullivan.massageGfycatLink
import com.alexsullivan.reddit.models.RedditListingItem
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class RedditResponseItemDeserializer : JsonDeserializer<RedditListingItem> {

    override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?): RedditListingItem {
        val jObject = json.asJsonObject
        val kind = jObject.get("kind").asString
        val data = jObject.getAsJsonObject("data")
        val id = data.get("id").asString
        var url = data.get("url").asString
        val thumbnail = data.get("thumbnail").asString
        val secureMedia = data.get("secure_media")
        val preview = data.getAsJsonObject("preview")
        var previewUrl: String? = null
        if (preview != null) {
            val images = preview.getAsJsonArray("images")
            val firstObject = images[0].asJsonObject
            val variants = firstObject.getAsJsonObject("variants")
            val gifs = variants.getAsJsonObject("gif")
            val source = gifs.getAsJsonObject("source")
            previewUrl = source.get("url").asString
        }
        var mediaType: String? = null
        if (!secureMedia.isJsonNull) {
            val secureMediaObject = secureMedia.asJsonObject
            mediaType = secureMediaObject.get("type").asString
            // Massage the link if its a gyfcat link since we want the actual gif, not just the
            // gyfcat link.
            if (mediaType == "gfycat.com") {
                url = url.massageGfycatLink()
            }
        }

        return RedditListingItem(kind, id, url, mediaType, thumbnail, previewUrl)
    }
}