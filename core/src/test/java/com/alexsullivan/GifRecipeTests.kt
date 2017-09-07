package com.alexsullivan

import org.junit.Assert
import org.junit.Test

class GifRecipeTests {
    @Test fun testValidValueConversion() {
        Assert.assertEquals(ImageType.GIF, ImageType.fromInt(0))
        Assert.assertEquals(ImageType.VIDEO, ImageType.fromInt(1))
    }

    @Test(expected = IllegalArgumentException::class) fun testInvalidValueConversion() {
        val imageType = ImageType.fromInt(2)
        Assert.fail("Should have thrown exception from invalid image type")
    }
}