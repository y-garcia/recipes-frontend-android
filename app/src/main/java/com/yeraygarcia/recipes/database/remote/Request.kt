package com.yeraygarcia.recipes.database.remote

import android.content.Context
import android.support.annotation.MainThread
import android.support.annotation.WorkerThread
import com.google.gson.Gson
import com.yeraygarcia.recipes.AppExecutors
import com.yeraygarcia.recipes.util.NetworkUtil
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
        if (canFetch()) {
            appExecutors.diskIO().execute {
                val oldEntity = getEntityBeforeUpdate(entity)

                updateLocalDatabase(entity)

                Timber.d("Sending request to server.")
                Timber.d(Gson().toJson(entity))

                val call = sendRequestToServer(entity)

                call.enqueue(object : Callback<ResourceData<Entity>> {

                    override fun onResponse(
                        call: Call<ResourceData<Entity>>,
                        response: Response<ResourceData<Entity>>
                    ) {

                        Timber.d("We got a response from the server: ${Gson().toJson(response)}")

                        if (response.isSuccessful) {

                            Timber.d("Response was successful: ${response.code()}")
                            val body = response.body()

                            if (body?.result != null) {
                                Timber.d("Body contains payload. Execute onSuccess with payload.")
                                onSuccess(body.result!!)

                            } else {
                                Timber.d("Body is empty. Execute onSuccess with entity.")
                                onSuccess(entity)
                            }

                        } else {
                            val errorBody = response.errorBody()?.string()

                            Timber.d("Response was not successful: ${response.code()} - $errorBody")

                            try {
                                val error = Gson().fromJson(errorBody, ResourceData::class.java)
                                onError(error.code ?: "0", error.message ?: "Unknown error")
                            } catch (e: Exception) {
                                onError(response.code().toString(), response.message())
                            }

                            appExecutors.diskIO().execute { updateLocalDatabase(oldEntity) }
                        }
                    }

                    override fun onFailure(call: Call<ResourceData<Entity>>, t: Throwable) {

                        Timber.d("Call failed: ${t.message}")

                        onError(errorMessage = t.message.toString())

                        appExecutors.diskIO().execute { updateLocalDatabase(oldEntity) }
                    }
                })
            }
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