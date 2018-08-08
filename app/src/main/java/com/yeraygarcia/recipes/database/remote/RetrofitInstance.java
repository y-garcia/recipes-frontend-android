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

    private static RetrofitInstance mRetrofitInstance;

    private final GsonConverterFactory mGsonConverter;
    private final LiveDataCallAdapterFactory mLiveDataCallAdapter;
    private final ServiceInterceptor mInterceptor;
    private final OkHttpClient mOkHttpClient;
    private Retrofit mRetrofit;

    private RetrofitInstance() {
        mGsonConverter = GsonConverterFactory.create();
        mLiveDataCallAdapter = new LiveDataCallAdapterFactory();
        mInterceptor = new ServiceInterceptor();
        mOkHttpClient = new OkHttpClient.Builder().addInterceptor(mInterceptor).build();
    }

    public static RetrofitInstance get() {
        if (mRetrofitInstance == null) {
            mRetrofitInstance = new RetrofitInstance();
        }
        return mRetrofitInstance;
    }

    public static void clear() {
        mRetrofitInstance = null;
    }

    /**
     * Gets an instance of Retrofit object and creates a service from it
     */
    public <T> T create(Class<T> service) {
        Debug.d("RetrofitInstance", "get()");
        if (mRetrofit == null) {
            Debug.d("RetrofitInstance", "mRetrofit == null");

            mRetrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(mGsonConverter)
                    .addCallAdapterFactory(mLiveDataCallAdapter)
                    .client(mOkHttpClient)
                    .build();
        }
        return mRetrofit.create(service);
    }

    public void setIdToken(String idToken){
        mInterceptor.setIdToken(idToken);
    }

    public String getIdToken(){
        return mInterceptor.getIdToken();
    }
}
