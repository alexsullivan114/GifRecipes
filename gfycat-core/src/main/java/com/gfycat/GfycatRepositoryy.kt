package com.gfycat

import io.reactivex.Observable

/**
 * Created by Alexs on 8/31/2017.
 */
interface GfycatRepository {
    fun getImageInfo(imageId: String): Observable<GfycatPost>
}