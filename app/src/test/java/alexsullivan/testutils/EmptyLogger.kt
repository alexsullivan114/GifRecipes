package alexsullivan.testutils

import com.alexsullivan.logging.Logger

object EmptyLogger: Logger {
    override fun printLn(priority: Int, tag: String, msg: String) = -1

    override fun e(tag: String, msg: String, error: Throwable) {
        // no-op
    }
}