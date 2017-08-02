package alexsullivan.gifrecipes.application

import android.util.Log
import com.alexsullivan.logging.Logger

object AndroidLogger: Logger {
    override fun printLn(priority: Int, tag: String, msg: String) = Log.println(priority, tag, msg)
    override fun e(tag: String, msg: String, error: Throwable) {
        Log.e(tag, msg, error)
    }
}