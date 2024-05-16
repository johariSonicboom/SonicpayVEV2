package com.sonicboom.sonicpayvui;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    public static FileUploadService getService(String baseURL) {
        // Create an OkHttpClient with logging interceptor
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();

        // Create Retrofit instance
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseURL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Return the Retrofit service
        return retrofit.create(FileUploadService.class);
    }

    public static AppUpdateService getAppUpdateService(String baseURL) {
        // Create an OkHttpClient with logging interceptor
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();

        // Create Retrofit instance
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseURL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Return the Retrofit service
        return retrofit.create(AppUpdateService.class);
    }
}
