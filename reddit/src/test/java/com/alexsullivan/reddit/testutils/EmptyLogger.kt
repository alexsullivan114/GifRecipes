package com.alexsullivan.reddit.testutils

import com.alexsullivan.logging.Logger

/**
 * Created by Alexs on 9/5/2017.
 */
object EmptyLogger: Logger {
    override fun printLn(priority: Int, tag: String, msg: String) = -1

    override fun e(tag: String, msg: String, error: Throwable) {
        // no-op
    }
}