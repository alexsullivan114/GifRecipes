package com.alexsullivan

import org.junit.Assert
import org.junit.Test

class WebImageUtilsKtTest {

    @Test fun testNonGfycatLink() {
        val link = "https://imgur.com"
        Assert.assertEquals(link, link.massageGfycatLink())
    }

    @Test fun testGfycatLink() {
        val link = "https://gfycat.com/PitifulAggressiveGaur"
        val desiredLink = "https://giant.gfycat.com/PitifulAggressiveGaur.gif"
        Assert.assertEquals(desiredLink, link.massageGfycatLink())
    }

}