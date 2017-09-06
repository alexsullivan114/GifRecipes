package com.imgur

import io.reactivex.Observable

/**
 * Created by Alexs on 8/31/2017.
 */
interface ImgurRepository {
    fun getImageInfo(imageId: String): Observable<ImgurPost>
}