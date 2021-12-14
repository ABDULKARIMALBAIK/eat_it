package com.example.asus.androideatit.Remote;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface IGoogleService {

    @GET
    Call<String> getAddressName(@Url String url , @Query("key") String key);

    @GET
    Call<String> getLocationFromAddress(@Url String url , @Query("key") String key);

    @GET("maps/api/directions/json")
    Call<String> getDirection(@Query("origin") String origin , @Query("destination") String destination , @Query("key") String key);

}
