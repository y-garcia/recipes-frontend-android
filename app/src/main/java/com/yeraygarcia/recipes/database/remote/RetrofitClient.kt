package com.yeraygarcia.recipes.database.remote

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber

class RetrofitClient private constructor(context: Context) {

    //    private val baseUrl = "http://10.0.2.2/recipes-api/src/api/"
    private val baseUrl = "https://yeraygarcia.com/recipes-dev/src/api/"

    private val gsonConverter = GsonConverterFactory.create()
    private val liveDataCallAdapter = LiveDataCallAdapterFactory()
    private val interceptor = ServiceInterceptor(context)
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(interceptor)
        .authenticator(interceptor)
        .build()
    private var retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(gsonConverter)
        .addCallAdapterFactory(liveDataCallAdapter)
        .client(okHttpClient)
        .build()

    val googleSignInClient: GoogleSignInClient
        get() = interceptor.googleSignInClient

    /**
     * Gets an instance of Retrofit object and creates a service from it
     */
    fun <T> create(service: Class<T>): T {
        Timber.d("create(${service.simpleName})")
        return retrofit.create(service)
    }

    fun setIdToken(idToken: String?) {
        Timber.d("Setting token: $idToken")
        interceptor.idToken = idToken
    }

    companion object {

        private var retrofitClient: RetrofitClient? = null

        fun get(context: Context): RetrofitClient {
            Timber.d("get(context)")
            if (retrofitClient == null) {
                Timber.d("retrofitClient == null -> Initializing...")
                retrofitClient = RetrofitClient(context)
            }
            return retrofitClient as RetrofitClient
        }

        fun clear() {
            Timber.d("clear()")
            retrofitClient = null
        }
    }
}
