package com.example.asus.androideatit.Common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.ParseException;

import com.example.asus.androideatit.Model.User;
import com.example.asus.androideatit.Remote.APIService;
import com.example.asus.androideatit.Remote.GoogleRetrofitClient;
import com.example.asus.androideatit.Remote.IGoogleService;
import com.example.asus.androideatit.Remote.RetrofitClient;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class Common {

    public static User currentUser;
    public static String currentKey;

    public static String topicName = "News";

    private static final String BASE_URL = "....................";
    private static final String GOOGLE_API_URL = "...................";

    public static String MAP_API_KEY = "........................";

    public static final String PHONE_TEXT = "userPhone";

    public static final String INTENT_FOOD_ID = "FoodId";

    public static final String DELETE = "Delete";
    public static final String USER_KEY = "User";
    public static final String PWD_KEY = "Password";

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

    public static APIService getFCMService(){

        return RetrofitClient.getClient(BASE_URL).create(APIService.class);
    }

    public static IGoogleService getGoogleMapAPI(){

        return GoogleRetrofitClient.getGoogleClient(GOOGLE_API_URL).create(IGoogleService.class);
    }

    //this function will convert current to number base on local
    public static BigDecimal formatCurrent(String amount , Locale locale) throws ParseException, java.text.ParseException {

        NumberFormat format = NumberFormat.getCurrencyInstance(locale);
        if (format instanceof DecimalFormat)
            ((DecimalFormat)format).setParseBigDecimal(true);

        return (BigDecimal)format.parse(amount.replace("[^\\d.,]" , ""));
    }

}
