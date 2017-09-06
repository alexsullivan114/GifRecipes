package com.imgur

import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import org.junit.Assert
import org.junit.Test

class ImgurPostDeserializerTests {

    val normalImgurPost = """
{
	"data": {
		"id": "MIix6CP",
		"title": null,
		"description": "#Recipes,#food, #mealthy, #easy, #cauliflower, #steak",
		"datetime": 1504644143,
		"type": "image/gif",
		"animated": true,
		"width": 450,
		"height": 450,
		"size": 145916745,
		"views": 25933,
		"bandwidth": 3784058948085,
		"vote": null,
		"favorite": false,
		"nsfw": false,
		"section": "GifRecipes",
		"account_url": null,
		"account_id": null,
		"is_ad": false,
		"in_most_viral": false,
		"has_sound": false,
		"tags": [],
		"ad_type": 0,
		"ad_url": "",
		"in_gallery": false,
		"link": "http://i.imgur.com/MIix6CPh.gif",
		"mp4": "https://i.imgur.com/MIix6CP.mp4",
		"gifv": "https://i.imgur.com/MIix6CP.gifv",
		"mp4_size": 8953342,
		"looping": true
	},
	"success": true,
	"status": 200
}"""

    val malformedPost = """
{
	"data": {
		"id": "MIix6CP",
		"title": null,
		"description": "#Recipes,#food, #mealthy, #easy, #cauliflower, #steak",
		"datetime": 1504644143,
		"type": "image/gif",
		"animated": true,
		"width": 450,
		"height": 450,
		"size": 145916745,
		"views": 25933,
		"bandwidth": 3784058948085,
		"vote": null,
		"favorite": false,
		"nsfw": false,
		"section": "GifRecipes",
		"account_url": null,
		"account_id": null,
		"is_ad": false,
		"in_most_viral": false,
		"has_sound": false,
		"tags": [],
		"ad_type": 0,
		"ad_url": "",
		"in_gallery": false,
		"link": "http://i.imgur.com/MIix6CPh.gif",
		"gifv": "https://i.imgur.com/MIix6CP.gifv",
		"mp4_size": 8953342,
		"looping": true
	},
	"success": true,
	"status": 200
}"""

    @Test fun normalDeserialization() {
        val deserializer = ImgurPostDeserializer()
        val post = deserializer.deserialize(JsonParser().parse(normalImgurPost), null, null)
        Assert.assertEquals("https://i.imgur.com/MIix6CP.mp4", post.mp4)
    }

    @Test(expected = JsonParseException::class) fun failedDeserialization() {
        val deserializer = ImgurPostDeserializer()
        val post = deserializer.deserialize(JsonParser().parse(malformedPost), null, null)
        Assert.fail("Expected a json exception form malformed post!")
    }
}