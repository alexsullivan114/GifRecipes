package com.alexsullivan.reddit.models

/**
 * Created by Alexs on 5/9/2017.
 *
 * Used exclusively as a response object from the reddit auth api, hence the name styles.
 */

data class RedditAuth(val access_token: String, val token_type: String, val device_id: String,
                      val expires_in: Int, val scope: String)