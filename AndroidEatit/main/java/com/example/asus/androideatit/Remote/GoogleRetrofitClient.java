package com.example.asus.androideatit.Remote;

import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class GoogleRetrofitClient {

    private static Retrofit retrofitGoogleClient = null;

    public static Retrofit getGoogleClient(String baseURL){

        if (retrofitGoogleClient == null)
            retrofitGoogleClient = new Retrofit.Builder()
                    .baseUrl(baseURL)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build();

        return retrofitGoogleClient;
    }


}
