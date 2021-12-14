package com.example.asus.androideatitserver.Common;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.example.asus.androideatitserver.Model.Request;
import com.example.asus.androideatitserver.Model.User;
import com.example.asus.androideatitserver.Remote.APIService;
import com.example.asus.androideatitserver.Remote.FCMRetrofitClient;
import com.example.asus.androideatitserver.Remote.IGeoCoordinates;
import com.example.asus.androideatitserver.Remote.RetrofitClient;

import java.util.Calendar;
import java.util.Locale;

public class Common {

    public static final String SHIPPERS_TABLE = "Shippers";
    public static final String ORDER_NEED_TO_SHIP_TABLE = "OrdersNeedShip";

    public static User currentUser;
    public static Request currentRequest;

    public static final String PHONE_TEXT = "userPhone";

    public static String topicName = "News";

    public static final String UPDATE = "Update";
    public static final String DELETE = "Delete";
    public static final int PICK_IMAGE_REQUEST = 71;

    public static final String baseUrl = "...................";

    private static final String fcmUrl = "....................";

    public static final String API_KEY_MAPS = ".......................";

    public static String convertCodeToStatus(String code){

        if (code.equals("0"))
            return "Placed";

        else if (code.equals("1"))
            return "On my way";

        else if (code.equals("2"))
            return "Shipping";
        else
            return "Shipped";
    }

    public static IGeoCoordinates getGeoCodeService(){

        return RetrofitClient.getClient(baseUrl).create(IGeoCoordinates.class);
    }

    public static APIService getFCMClient(){

        return FCMRetrofitClient.getClient(fcmUrl).create(APIService.class);
    }

    public static Bitmap scaleBitmap(Bitmap bitmap , int newWidth , int newHeight){

        Bitmap scaledBitmap = Bitmap.createBitmap(newWidth , newHeight , Bitmap.Config.ARGB_8888);

        float scaleX = newWidth/(float)bitmap.getWidth();
        float scaleY = newHeight/(float)bitmap.getHeight();
        float pivotX=0 , pivotY=0;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(scaleX ,scaleY , pivotX , pivotY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bitmap , 0 , 0 , new Paint(Paint.FILTER_BITMAP_FLAG));

        return scaledBitmap;
    }

    public static boolean isConnectionToInternet(Context context){

        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null){

            NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
            if (info != null){

                for (int i = 0; i < info.length; i++) {

                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                        return true;
                }
            }
        }
        return false;
    }

    public static String getData(long time){

        Calendar calendar =  Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(time);
        StringBuilder data = new StringBuilder(
                android.text.format.DateFormat.format("dd-MM-yyyy HH:mm" ,
                        calendar).toString());

        return data.toString();
    }
}
