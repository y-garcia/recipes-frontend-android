package com.yeraygarcia.recipes.database.remote

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Tasks
import com.yeraygarcia.recipes.R
import com.yeraygarcia.recipes.util.Debug
import okhttp3.*
import okhttp3.Request
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
        Debug.d(this, "intercept(chain)")
        var request = chain.request()

        if (request.header("No-Authentication") == null && idToken != null) {
            Debug.d(this, "Appending token to header")
            request = request.newBuilder()
                .addHeader("Authorization", "Bearer $idToken")
                .build()
        }

        return chain.proceed(request)
    }

    override fun authenticate(route: Route, response: Response): Request? {
        Debug.d(this, "authenticate(route, response)")

        try {
            Debug.d(this, "silentSignIn()")
            val task = googleSignInClient.silentSignIn()
            val account = Tasks.await(task)
            account?.apply {
                Debug.d("ServiceInterceptor", "account != null")
                Debug.d("Token", idToken)
                return response.request().newBuilder()
                    .addHeader("Authorization", "Bearer $idToken")
                    .build()
            }
            Debug.d(this, "account == null")
        } catch (e: InterruptedException) {
            Debug.d(this, e.message)
        } catch (e: ExecutionException) {
            Debug.d(this, e.message)
        }

        return null
    }
}
