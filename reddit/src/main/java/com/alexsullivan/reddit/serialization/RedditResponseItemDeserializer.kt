package com.alexsullivan.reddit.serialization

import com.alexsullivan.reddit.models.RedditListingItem
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

internal class RedditResponseItemDeserializer : JsonDeserializer<RedditListingItem> {

    override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?): RedditListingItem {
        try {
            val jObject = json.asJsonObject
            val kind = jObject.get("kind").asString
            val data = jObject.getAsJsonObject("data")
            val id = data.get("id").asString
            val url = data.get("url").asString
            val thumbnail = data.get("thumbnail").asString
            val preview = data.getAsJsonObject("preview")
            val title = data.get("title").asString
            var previewUrl: String? = null
            if (preview != null) {
                val images = preview.getAsJsonArray("images")
                val firstObject = images[0].asJsonObject
                val variants = firstObject.getAsJsonObject("variants")
                val gifs = variants.getAsJsonObject("gif")
                if (gifs != null) {
                    val source = gifs.getAsJsonObject("source")
                    previewUrl = source.get("url").asString
                }
            }
            val domain: String = data.get("domain").asString
            return RedditListingItem(kind, id, url, domain, thumbnail, previewUrl, title)
        }
        catch (exception: JsonParseException) {
            throw JsonParseException("Json doesn't match expected reddit listing format! " + exception)
        }
        catch (exception: IllegalStateException) {
            throw IllegalStateException("Json doesn't match expected reddit listing format! " + exception)
        }
    }
}