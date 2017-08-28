package alexsullivan.gifrecipes.utils

import android.graphics.Point
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity

val AppCompatActivity.width: Int
    get() {
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        return size.x
    }

val AppCompatActivity.height: Int
    get() {
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        return size.y
    }

fun AppCompatActivity.addFragment(fragment: Fragment, containerId: Int, tag: String?) {
    val manager = supportFragmentManager
    val transaction = manager.beginTransaction()
    transaction.add(containerId, fragment, tag)
    transaction.commit()
}