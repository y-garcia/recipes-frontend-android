package com.yeraygarcia.recipes.database.remote

import android.content.Context
import android.support.annotation.MainThread
import android.support.annotation.WorkerThread
import com.yeraygarcia.recipes.AppExecutors
import com.yeraygarcia.recipes.util.NetworkUtil
import com.yeraygarcia.recipes.util.fromJson
import com.yeraygarcia.recipes.util.toJson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

abstract class Request<Entity>(val context: Context) {

    private val appExecutors = AppExecutors()

    private fun canFetch(): Boolean {
        return NetworkUtil.isOnline(context)
    }

    fun send(entity: Entity) {
        if (!canFetch()) {
            return
        }

        appExecutors.diskIO {
            val oldEntity = getEntityBeforeUpdate(entity)

            updateLocalDatabase(entity)

            Timber.d("Sending request to server.")
            Timber.d(entity?.toJson())

            val call = sendRequestToServer(entity)

            call.enqueue(object : Callback<ResourceData<Entity>> {

                override fun onResponse(
                    call: Call<ResourceData<Entity>>,
                    response: Response<ResourceData<Entity>>
                ) {
                    Timber.d("We got a response from the server: ${response.toJson()}")
                    processResponse(response, entity, oldEntity)
                }

                override fun onFailure(call: Call<ResourceData<Entity>>, t: Throwable) {
                    Timber.d("Call failed: ${t.message}")
                    onError(errorMessage = t.message.toString())
                    appExecutors.diskIO { updateLocalDatabase(oldEntity) }
                }
            })
        }
    }

    private fun processResponse(
        response: Response<ResourceData<Entity>>,
        entity: Entity,
        oldEntity: Entity
    ) {
        if (response.isSuccessful) {
            Timber.d("Response was successful: ${response.code()}")
            onSuccess(response.body()?.result ?: entity)
        } else {
            val body = response.errorBody()?.string()
            val code = response.code().toString()
            val message = response.message()

            Timber.d("Response was not successful: $code - $body")

            val error = body?.fromJson(ResourceData::class.java)
            onError(error?.code ?: code, error?.message ?: message)

            appExecutors.diskIO { updateLocalDatabase(oldEntity) }
        }
    }

    @WorkerThread
    abstract fun getEntityBeforeUpdate(newEntity: Entity): Entity

    @WorkerThread
    abstract fun updateLocalDatabase(entity: Entity)

    @MainThread
    abstract fun sendRequestToServer(newEntity: Entity): Call<ResourceData<Entity>>

    @MainThread
    abstract fun onSuccess(responseEntity: Entity)

    @MainThread
    abstract fun onError(errorCode: String = "0", errorMessage: String = "Unknown error")
}