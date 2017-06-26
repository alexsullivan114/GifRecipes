package com.alexsullivan.logging

interface Logger {
    companion object {
        /**
         * Priority constant for the println method; use Log.v.
         */
        val VERBOSE = 2

        /**
         * Priority constant for the println method; use Log.d.
         */
        val DEBUG = 3

        /**
         * Priority constant for the println method; use Log.i.
         */
        val INFO = 4

        /**
         * Priority constant for the println method; use Log.w.
         */
        val WARN = 5

        /**
         * Priority constant for the println method; use Log.e.
         */
        val ERROR = 6

        /**
         * Priority constant for the println method.
         */
        val ASSERT = 7
    }

    fun printLn(priority: Int, tag: String, msg: String): Int

    fun d(tag: String, msg: String) = printLn(DEBUG, tag, msg)
    fun v(tag: String, msg: String) = printLn(VERBOSE, tag, msg)
    fun i(tag: String, msg: String) = printLn(INFO, tag, msg)
    fun w(tag: String, msg: String) = printLn(WARN, tag, msg)
    fun e(tag: String, msg: String) = printLn(ERROR, tag, msg)
    fun a(tag: String, msg: String) = printLn(ASSERT, tag, msg)

}