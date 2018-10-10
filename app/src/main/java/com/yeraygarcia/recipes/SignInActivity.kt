package com.yeraygarcia.recipes

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.gson.Gson
import com.yeraygarcia.recipes.database.remote.ResourceData
import com.yeraygarcia.recipes.database.remote.RetrofitClient
import com.yeraygarcia.recipes.database.remote.User
import com.yeraygarcia.recipes.database.remote.Webservice
import com.yeraygarcia.recipes.util.Debug
import com.yeraygarcia.recipes.util.NetworkUtil
import kotlinx.android.synthetic.main.activity_sign_in.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignInActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        googleSignInButton.apply {
            setSize(SignInButton.SIZE_WIDE)
            setOnClickListener { signIn() }
        }
    }

    private fun setStatus(status: Int) {
        when (status) {
            SIGNING_IN -> {
                textViewSignInInfo.setText(R.string.sign_in_signing_in)
                textViewSignInInfo.visibility = View.VISIBLE
                progressBarSignIn.visibility = View.VISIBLE
                googleSignInButton.isEnabled = false
            }
            SIGNED_IN -> {
                textViewSignInInfo.setText(R.string.sign_in_signed_in)
                textViewSignInInfo.visibility = View.VISIBLE
                progressBarSignIn.visibility = View.INVISIBLE
                googleSignInButton.isEnabled = false
            }
            SIGNED_OUT -> {
                textViewSignInInfo.visibility = View.INVISIBLE
                progressBarSignIn.visibility = View.INVISIBLE
                googleSignInButton.isEnabled = true
            }
            else -> {
                textViewSignInInfo.visibility = View.INVISIBLE
                progressBarSignIn.visibility = View.INVISIBLE
                googleSignInButton.isEnabled = true
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Debug.d(this, "onStart(): getLastSignedInAccount()")

        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account?.isExpired == false) {
            Debug.d(this, "onStart(): account != null && !account.isExpired()")
            updateUI(account)
        } else {
            Debug.d(this, "onStart(): account == null || account.isExpired()")
            setStatus(SIGNING_IN)
            Debug.d(this, "onStart(): silentSignIn()")
            RetrofitClient.get(this).googleSignInClient.silentSignIn().addOnCompleteListener(this) {
                this.handleSignInResult(it)
            }
        }
    }

    private fun signIn() {
        Debug.d(this, "signIn(): show sign-in dialog")
        setStatus(SIGNING_IN)
        val signInIntent = RetrofitClient.get(this).googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Debug.d(this, "onActivityResult(): task = getSignedInAccountFromIntent()")
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        Debug.d(this, "handleSignInResult(task)")
        try {
            val account = completedTask.getResult(ApiException::class.java)
            val idToken = account?.idToken

            if (idToken != null) {

                if (NetworkUtil.isOnline(this)) {

                    val webservice = RetrofitClient.get(this).create(Webservice::class.java)
                    val call = webservice.postToken(idToken)

                    call.enqueue(object : Callback<ResourceData<User>> {
                        override fun onResponse(
                            call: Call<ResourceData<User>>,
                            response: Response<ResourceData<User>>
                        ) {
                            Debug.d(this, Gson().toJson(response))
                            if (response.isSuccessful) {
                                if (response.body()?.result?.username != null) {
                                    updateUI(account)
                                } else {
                                    updateUI(null, getString(R.string.sign_in_error_empty_response))
                                }
                            } else {
                                updateUI(null, response.message())
                            }
                        }

                        override fun onFailure(call: Call<ResourceData<User>>, t: Throwable) {
                            Debug.d(this, t.message)
                            updateUI(null, t.message)
                        }
                    })
                } else {
                    // sign in the user in offline mode
                    updateUI(account)
                }
            } else {
                updateUI(null, getString(R.string.sign_in_error_empty_token))
            }
        } catch (e: ApiException) {
            Debug.d(this, "signInResult:failed code=" + e.statusCode)
            updateUI(null)
        }

    }

    private fun updateUI(account: GoogleSignInAccount?, errorMessage: String? = null) {
        Debug.d(this, "updateUI(account, errorMessage)")
        if (account != null) {
            setStatus(SIGNED_IN)
            RetrofitClient.get(this).setIdToken(account.idToken)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            setStatus(SIGNED_OUT)
            if (errorMessage != null) {
                textViewSignInInfo.apply {
                    setText(R.string.sign_in_error_fail)
                    error = errorMessage
                    visibility = View.VISIBLE
                }
            }
        }
    }

    companion object {
        private const val RC_SIGN_IN = 1
        private const val SIGNING_IN = 1
        private const val SIGNED_IN = 2
        private const val SIGNED_OUT = 3
    }
}
