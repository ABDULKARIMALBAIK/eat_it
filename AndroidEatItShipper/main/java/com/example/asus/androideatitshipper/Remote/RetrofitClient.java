package com.example.asus.androideatitshipper.Remote;

import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitClient {

    public static Retrofit retrofit = null;

    //We used Scalars converter , because the result is scalars (Maps)

    public static Retrofit getClient(String baseUrl){

        if (retrofit == null)
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build();

        return retrofit;
    }

}
