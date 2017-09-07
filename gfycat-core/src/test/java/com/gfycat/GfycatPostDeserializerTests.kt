package com.gfycat

import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import org.junit.Assert
import org.junit.Test

class GfycatPostDeserializerTests {
    val normalPost = """{
	"gfyItem": {
		"gfyId": "fragrantbeneficialcornsnake",
		"gfyName": "FragrantBeneficialCornsnake",
		"gfyNumber": "961237444",
		"webmUrl": "https://giant.gfycat.com/FragrantBeneficialCornsnake.webm",
		"gifUrl": "https://giant.gfycat.com/FragrantBeneficialCornsnake.gif",
		"mobileUrl": "https://thumbs.gfycat.com/FragrantBeneficialCornsnake-mobile.mp4",
		"mobilePosterUrl": "https://thumbs.gfycat.com/FragrantBeneficialCornsnake-mobile.jpg",
		"miniUrl": "https://thumbs.gfycat.com/FragrantBeneficialCornsnake-mini.mp4",
		"miniPosterUrl": "https://thumbs.gfycat.com/FragrantBeneficialCornsnake-mini.jpg",
		"posterUrl": "https://thumbs.gfycat.com/FragrantBeneficialCornsnake-poster.jpg",
		"thumb360Url": "https://thumbs.gfycat.com/FragrantBeneficialCornsnake-360.mp4",
		"thumb360PosterUrl": "https://thumbs.gfycat.com/FragrantBeneficialCornsnake-thumb360.jpg",
		"thumb100PosterUrl": "https://thumbs.gfycat.com/FragrantBeneficialCornsnake-thumb100.jpg",
		"max5mbGif": "https://thumbs.gfycat.com/FragrantBeneficialCornsnake-size_restricted.gif",
		"max2mbGif": "https://thumbs.gfycat.com/FragrantBeneficialCornsnake-small.gif",
		"max1mbGif": "https://thumbs.gfycat.com/FragrantBeneficialCornsnake-max-1mb.gif",
		"gif100px": "https://thumbs.gfycat.com/FragrantBeneficialCornsnake-100px.gif",
		"mjpgUrl": "https://thumbs.gfycat.com/FragrantBeneficialCornsnake.mjpg",
		"webpUrl": "https://thumbs.gfycat.com/FragrantBeneficialCornsnake.webp",
		"width": 1920,
		"height": 1080,
		"avgColor": "#000000",
		"frameRate": 29,
		"numFrames": 1540,
		"mp4Size": 29396821,
		"webmSize": 8280835,
		"gifSize": 10788933,
		"source": 8,
		"createDate": 1504776368,
		"nsfw": "1",
		"mp4Url": "https://giant.gfycat.com/FragrantBeneficialCornsnake.mp4",
		"likes": 0,
		"published": 1,
		"dislikes": 0,
		"extraLemmas": "",
		"md5": "cc234974183afd68341220f344663c92",
		"views": 244,
		"tags": null,
		"userName": "uncle_retardo",
		"title": "Mini Beef Wellingtons",
		"description": "",
		"languageCategories": null,
		"domainWhitelist": []
	}
}"""

    val errorPost = """{
	"gfyItem": {
		"gfyId": "fragrantbeneficialcornsnake",
		"gfyName": "FragrantBeneficialCornsnake",
		"gfyNumber": "961237444",
		"webmUrl": "https://giant.gfycat.com/FragrantBeneficialCornsnake.webm",
		"gifUrl": "https://giant.gfycat.com/FragrantBeneficialCornsnake.gif",
		"mobileUrl": "https://thumbs.gfycat.com/FragrantBeneficialCornsnake-mobile.mp4",
		"mobilePosterUrl": "https://thumbs.gfycat.com/FragrantBeneficialCornsnake-mobile.jpg",
		"miniUrl": "https://thumbs.gfycat.com/FragrantBeneficialCornsnake-mini.mp4",
		"miniPosterUrl": "https://thumbs.gfycat.com/FragrantBeneficialCornsnake-mini.jpg",
		"posterUrl": "https://thumbs.gfycat.com/FragrantBeneficialCornsnake-poster.jpg",
		"thumb360Url": "https://thumbs.gfycat.com/FragrantBeneficialCornsnake-360.mp4",
		"thumb360PosterUrl": "https://thumbs.gfycat.com/FragrantBeneficialCornsnake-thumb360.jpg",
		"thumb100PosterUrl": "https://thumbs.gfycat.com/FragrantBeneficialCornsnake-thumb100.jpg",
		"max5mbGif": "https://thumbs.gfycat.com/FragrantBeneficialCornsnake-size_restricted.gif",
		"max2mbGif": "https://thumbs.gfycat.com/FragrantBeneficialCornsnake-small.gif",
		"max1mbGif": "https://thumbs.gfycat.com/FragrantBeneficialCornsnake-max-1mb.gif",
		"gif100px": "https://thumbs.gfycat.com/FragrantBeneficialCornsnake-100px.gif",
		"mjpgUrl": "https://thumbs.gfycat.com/FragrantBeneficialCornsnake.mjpg",
		"webpUrl": "https://thumbs.gfycat.com/FragrantBeneficialCornsnake.webp",
		"width": 1920,
		"height": 1080,
		"avgColor": "#000000",
		"frameRate": 29,
		"numFrames": 1540,
		"mp4Size": 29396821,
		"webmSize": 8280835,
		"gifSize": 10788933,
		"source": 8,
		"createDate": 1504776368,
		"nsfw": "1",
		"likes": 0,
		"published": 1,
		"dislikes": 0,
		"extraLemmas": "",
		"md5": "cc234974183afd68341220f344663c92",
		"views": 244,
		"tags": null,
		"userName": "uncle_retardo",
		"title": "Mini Beef Wellingtons",
		"description": "",
		"languageCategories": null,
		"domainWhitelist": []
	}
}"""

    @Test fun testNormalDeserialization() {
        val deserializer = GfycatPostDeserializer()
        val post = deserializer.deserialize(JsonParser().parse(normalPost), null, null)
        Assert.assertEquals("https://giant.gfycat.com/FragrantBeneficialCornsnake.mp4", post.mp4)
    }

    @Test(expected = JsonParseException::class) fun testFailedDeserialization() {
        val deserializer = GfycatPostDeserializer()
        val post = deserializer.deserialize(JsonParser().parse(errorPost), null, null)
        Assert.fail("Expecting json parse exception!")
    }
}