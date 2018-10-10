package com.yeraygarcia.recipes.database.remote

data class ResourceData<T>(
    var status: Int,
    var code: String?,
    var message: String?,
    var result: T?
) {
    val isSuccessful: Boolean
        get() = status in 200..299
}
