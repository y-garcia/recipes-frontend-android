package com.yeraygarcia.recipes.database.remote;

import android.support.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class ServiceInterceptor implements Interceptor {

    private String idToken;

    ServiceInterceptor() {
    }

    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();

        if (request.header("No-Authentication") == null && idToken != null) {
            request = request.newBuilder()
                    .addHeader("Authorization", "Bearer " + idToken)
                    .build();
        }

        return chain.proceed(request);
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }
}
