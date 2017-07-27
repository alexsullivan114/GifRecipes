package com.alexsullivan.reddit.serialization

import com.alexsullivan.reddit.models.RedditListingItem
import com.google.gson.*
import java.lang.reflect.Type

internal class RedditResponseItemDeserializer : JsonDeserializer<RedditListingItem> {

    override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?): RedditListingItem {
        try {
            val jObject = json.asJsonObject
            val kind = jObject.get("kind").asString
            val data = jObject.getAsJsonObject("data")
            val removed = data.get("removal_reason")
            // If this post was removed for legal reasons, we're not going to try and gather its
            // details.
            if (removed != JsonNull.INSTANCE) {
                return RedditListingItem("", "", "", "", "", "", "", null, true)
            }
            val id = data.get("id").asString
            val url = data.get("url").asString
            val thumbnail = data.get("thumbnail")
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
            return RedditListingItem(kind, id, url, domain, "", previewUrl, title, null)
        }
        catch (exception: JsonParseException) {
            throw JsonParseException("Json doesn't match expected reddit listing format! " + exception)
        }
        catch (exception: IllegalStateException) {
            throw IllegalStateException("Json doesn't match expected reddit listing format! " + exception)
        }
    }
}