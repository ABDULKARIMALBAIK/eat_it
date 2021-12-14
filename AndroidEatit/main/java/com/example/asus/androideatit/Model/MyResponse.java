package com.example.asus.androideatit.Model;

import java.util.List;

public class MyResponse {

    //Response to notification from FCM , will be Like:
    public long multicast_id;
    public int success;
    public int failure;
    public int canonical_ids;
    public List<Result> results;

}
