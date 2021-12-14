package com.example.asus.androideatitserver.Remote;

import com.example.asus.androideatitserver.Model.DataMessage;
import com.example.asus.androideatitserver.Model.MyResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    //To get key for authorization -> go to FireBase -> project Settings -> Cloud Messaging -> Copy Server Key & paste to key below

    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=...................................."
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body DataMessage body);
}
