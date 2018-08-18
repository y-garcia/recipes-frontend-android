package com.yeraygarcia.recipes.database.remote;

import android.content.Context;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.yeraygarcia.recipes.util.Debug;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = "http://192.168.178.61/recipes-api/src/api/";
    //private static final String BASE_URL = "https://yeraygarcia.com/recipes/src/";

    private static RetrofitClient mRetrofitClient;

    private final GsonConverterFactory mGsonConverter;
    private final LiveDataCallAdapterFactory mLiveDataCallAdapter;
    private final ServiceInterceptor mInterceptor;
    private final OkHttpClient mOkHttpClient;
    private Retrofit mRetrofit;

    private RetrofitClient(Context context) {
        mGsonConverter = GsonConverterFactory.create();
        mLiveDataCallAdapter = new LiveDataCallAdapterFactory();
        mInterceptor = new ServiceInterceptor(context);
        mOkHttpClient = new OkHttpClient.Builder()
                .addInterceptor(mInterceptor)
                .authenticator(mInterceptor)
                .build();
    }

    public static RetrofitClient get(Context context) {
        Debug.d("RetrofitClient", "get(context)");
        if (mRetrofitClient == null) {
            Debug.d("RetrofitClient", "mRetrofitClient == null");
            mRetrofitClient = new RetrofitClient(context);
        }
        return mRetrofitClient;
    }

    public static void clear() {
        Debug.d("RetrofitClient", "clear()");
        mRetrofitClient = null;
    }

    /**
     * Gets an instance of Retrofit object and creates a service from it
     */
    public <T> T create(Class<T> service) {
        Debug.d("RetrofitClient", "create(" + service.getSimpleName() + ")");
        if (mRetrofit == null) {
            Debug.d("RetrofitClient", "mRetrofit == null");

            mRetrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(mGsonConverter)
                    .addCallAdapterFactory(mLiveDataCallAdapter)
                    .client(mOkHttpClient)
                    .build();
        }
        return mRetrofit.create(service);
    }

    public void setIdToken(String idToken) {
        Debug.d(this, "Setting token: " + idToken);
        mInterceptor.setIdToken(idToken);
    }

    public GoogleSignInClient getGoogleSignInClient() {
        return mInterceptor.getGoogleSignInClient();
    }
}
