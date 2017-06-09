package com.example

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class ImgurPostDeserializer: JsonDeserializer<ImgurPost> {
    override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext): ImgurPost {
        val post = json.asJsonObject.getAsJsonObject("data")
        return ImgurPost(post.get("mp4").asString, post.get("link").asString)
    }
}