package com.alexsullivan.reddit.models

/**
 * This file has two extra model objects to deal with retrofit being rather opinionated about how
 * your model should match your API. At this point Retrofit may be overboard; but in the hopes of
 * potentially better handling expanded reddit API access I'm going to leave it.
 */
internal data class RedditListingItem(val type: String, val id: String, val url: String,
                                      val domain: String, val thumbnail: String? = null,
                                      val previewUrl: String? = null, val title: String,
                                      val pageKey: String?,
                                      val removed: Boolean = false,
                                      val permaLink: String,
                                      val created: Long)

internal class RedditListingResponse(val kind: String, val data: RedditListingResponseData)

internal class RedditListingResponseData(val modHash: String, val children: List<RedditListingItem>, val before: String?, val after: String)