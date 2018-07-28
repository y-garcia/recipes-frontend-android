package com.yeraygarcia.recipes.database.remote;

import com.yeraygarcia.recipes.util.Debug;
import com.yeraygarcia.recipes.util.LiveDataCallAdapterFactory;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitInstance {

    //private static final String BASE_URL = "http://192.168.178.61/recipes-api/src/";
    private static final String BASE_URL = "https://yeraygarcia.com/recipes/src/";

    private static Retrofit standardRetrofit;
    private static Retrofit liveDataRetrofit;

    private static String idToken;

    /**
     * Create an instance of Retrofit object
     */
    public static Retrofit getLiveDataRetrofitInstance() {
        Debug.d("RetrofitInstance", "getLiveDataRetrofitInstance()");
        if (liveDataRetrofit == null) {
            Debug.d("RetrofitInstance", "liveDataRetrofit == null");

            Retrofit.Builder builder = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(new LiveDataCallAdapterFactory());

            Debug.d("RetrofitInstance", "idToken == " + (idToken == null ? "null" : idToken));

            if (idToken != null) {
                OkHttpClient okHttpClient = new OkHttpClient.Builder().addInterceptor(chain -> {
                    Request originalRequest = chain.request();
                    Request newRequest = originalRequest.newBuilder().header("Authorization", "Bearer " + idToken).build();
                    return chain.proceed(newRequest);
                }).build();

                builder.client(okHttpClient);
            }

            return builder.build();
        }
        return liveDataRetrofit;
    }

    /**
     * Create an instance of Retrofit object
     */
    public static Retrofit getStandardRetrofitInstance() {
        if (standardRetrofit == null) {

            standardRetrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return standardRetrofit;
    }

    public static void setIdToken(String idToken) {
        RetrofitInstance.idToken = idToken;
    }
}
