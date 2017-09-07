package com.gfycat

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class GfycatPostDeserializer: JsonDeserializer<GfycatPost> {

    override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?): GfycatPost {
        val data = json.asJsonObject.getAsJsonObject("gfyItem")
        if (!data.has("mp4Url")) {
            throw JsonParseException("Gfycat post does not have mp4 link")
        }
        val mp4 = data.get("mp4Url").asString
        return GfycatPost(mp4)
    }
}