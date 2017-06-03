package com.alexsullivan.reddit.urlmanipulation

interface UrlManipulator {
    fun ownsUrl(url: String): Boolean
    fun modifyUrl(url: String): String
}