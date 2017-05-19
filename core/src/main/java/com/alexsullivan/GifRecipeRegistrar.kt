package com.alexsullivan

internal object GifRecipeRegistrar {

    internal val providers = mutableListOf<GifRecipeProvider>()

    fun registerProvider(provider: GifRecipeProvider) {
        providers.add(provider)
    }
}