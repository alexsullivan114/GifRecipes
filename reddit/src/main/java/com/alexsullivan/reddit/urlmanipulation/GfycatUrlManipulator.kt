package com.alexsullivan.reddit.urlmanipulation

class GfycatUrlManipulator: UrlManipulator {

    override fun ownsUrl(url: String): Boolean {
        return url.contains("gfycat")
    }

    override fun modifyUrl(url: String): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}