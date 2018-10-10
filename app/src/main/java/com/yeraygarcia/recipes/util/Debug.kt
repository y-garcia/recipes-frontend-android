package com.yeraygarcia.recipes.util

import com.yeraygarcia.recipes.BuildConfig
import timber.log.Timber

object Debug {

    private const val PREFIX = "YGQ: "

    fun d(obj: Any, message: String?) {
        if (BuildConfig.DEBUG) {
            val tag = PREFIX + (obj as? String ?: obj.javaClass.simpleName)
            Timber.tag(tag).d(message)
        }
    }
}
