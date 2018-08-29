package com.yeraygarcia.recipes.util

import android.util.Log
import timber.log.Timber

object Debug {

    private const val PREFIX = "YGQ: "
    private const val DEBUG = true

    @JvmStatic
    fun d(obj: Any, message: String) {
        if (DEBUG) {
            Log.d(PREFIX + obj.javaClass.simpleName, message)
        }
    }

    @JvmStatic
    fun d(tag: String, message: String) {
        if (DEBUG) {
            Log.d(PREFIX + tag, message)
        }
    }
}
