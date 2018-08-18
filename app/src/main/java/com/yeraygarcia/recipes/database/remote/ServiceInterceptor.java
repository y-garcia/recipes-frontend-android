package com.yeraygarcia.recipes.database.remote;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.yeraygarcia.recipes.R;
import com.yeraygarcia.recipes.util.Debug;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import okhttp3.Authenticator;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

public class ServiceInterceptor implements Interceptor, Authenticator {

    private final GoogleSignInClient mGoogleSignInClient;
    private String mIdToken;

    ServiceInterceptor(Context context) {
        mGoogleSignInClient = GoogleSignIn.getClient(context, new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.server_client_id))
                .requestEmail()
                .build());
    }

    public String getIdToken() {
        return mIdToken;
    }

    public void setIdToken(String idToken) {
        this.mIdToken = idToken;
    }

    public GoogleSignInClient getGoogleSignInClient() {
        Debug.d(this, "getGoogleSignInClient()");
        return mGoogleSignInClient;
    }

    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Debug.d(this, "intercept(chain)");
        Request request = chain.request();

        if (request.header("No-Authentication") == null && mIdToken != null) {
            Debug.d(this, "Appending token to header");
            request = request.newBuilder()
                    .addHeader("Authorization", "Bearer " + mIdToken)
                    .build();
        }

        return chain.proceed(request);
    }

    @Nullable
    @Override
    public Request authenticate(@NonNull Route route, @NonNull Response response) {
        Debug.d(this, "authenticate(route, response)");

        try {
            Debug.d(this, "silentSignIn()");
            Task<GoogleSignInAccount> task = mGoogleSignInClient.silentSignIn();
            GoogleSignInAccount account = Tasks.await(task);
            if (account != null) {
                Debug.d(this, "account != null");
                mIdToken = account.getIdToken();
                Debug.d("Token", mIdToken);
                return response.request().newBuilder()
                        .addHeader("Authorization", "Bearer " + mIdToken)
                        .build();
            }
            Debug.d(this, "account == null");
        } catch (InterruptedException | ExecutionException e) {
            Debug.d(this, e.getMessage());
        }

        return null;
    }
}
