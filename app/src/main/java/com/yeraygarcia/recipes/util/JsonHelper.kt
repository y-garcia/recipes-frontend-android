package com.yeraygarcia.recipes.util

import com.google.gson.Gson

fun Any.toJson() = Gson().toJson(this)
fun <T> String.fromJson(classOfT: Class<T>): T? = try {
    Gson().fromJson(this, classOfT)
} catch (e: Exception) {
    null
}
