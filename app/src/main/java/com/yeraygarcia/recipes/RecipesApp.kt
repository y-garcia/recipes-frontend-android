package com.yeraygarcia.recipes

import android.app.Application
import timber.log.Timber

class RecipesApp : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(RecipesDebugTree())
        }
    }
}

class RecipesDebugTree : Timber.DebugTree() {

    override fun createStackElementTag(element: StackTraceElement): String? {
        return TAG + super.createStackElementTag(element)
    }

    companion object {
        const val TAG = "YGQ: "
    }
}