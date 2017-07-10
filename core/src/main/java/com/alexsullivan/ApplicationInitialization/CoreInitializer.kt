package com.alexsullivan.ApplicationInitialization

import com.alexsullivan.GifRecipeProvider
import com.alexsullivan.GifRecipeRegistrar

object CoreInitializer {

    fun initialize(vararg providers: GifRecipeProvider) {
        for (provider in providers) {
            GifRecipeRegistrar.registerProvider(provider)
        }
    }
}