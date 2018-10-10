package com.yeraygarcia.recipes.database.remote

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Tasks
import com.yeraygarcia.recipes.R
import okhttp3.*
import okhttp3.Request
import timber.log.Timber
import java.io.IOException
import java.util.concurrent.ExecutionException

class ServiceInterceptor internal constructor(context: Context) : Interceptor, Authenticator {

    var idToken: String? = null
    val googleSignInClient: GoogleSignInClient = GoogleSignIn.getClient(
        context, GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.server_client_id))
            .requestEmail()
            .build()
    )

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        Timber.d("intercept(chain)")
        var request = chain.request()

        if (request.header("No-Authentication") == null && idToken != null) {
            Timber.d("Appending token to header")
            request = request.newBuilder()
                .addHeader("Authorization", "Bearer $idToken")
                .build()
        }

        return chain.proceed(request)
    }

    override fun authenticate(route: Route, response: Response): Request? {
        Timber.d("authenticate(route, response)")

        try {
            Timber.d("silentSignIn()")
            val task = googleSignInClient.silentSignIn()
            val account = Tasks.await(task)
            account?.apply {
                Timber.d("account != null")
                Timber.d("Token = $idToken")
                return response.request().newBuilder()
                    .addHeader("Authorization", "Bearer $idToken")
                    .build()
            }
            Timber.d("account == null")
        } catch (e: InterruptedException) {
            Timber.d(e)
        } catch (e: ExecutionException) {
            Timber.d(e)
        }

        return null
    }
}
