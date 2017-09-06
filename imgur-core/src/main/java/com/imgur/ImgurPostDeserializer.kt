package com.imgur

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class ImgurPostDeserializer: JsonDeserializer<ImgurPost> {
    override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?): ImgurPost {
        val post = json.asJsonObject.getAsJsonObject("data")
        if (!post.has("mp4")) {
            throw JsonParseException("Imgur post does not have mp4 link")
        }
        return ImgurPost(post.get("mp4").asString)
    }
}