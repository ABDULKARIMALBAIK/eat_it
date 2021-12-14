package com.example.asus.androideatit.Helper;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDex;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.HashMap;
import java.util.Map;

public class App extends Application {

    FirebaseRemoteConfig remoteConfig;

    //This class need it to fetch data from Firebase Remote Config , we named of this class (Wrapper Class)
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        remoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings settings = new FirebaseRemoteConfigSettings.Builder().build();
        remoteConfig.setConfigSettings(settings);

        //Default value
        Map<String , Object> defaultValue = new HashMap<>();
        defaultValue.put(UpdateHelper.KEY_UPDATE_ENABLE , false);
        defaultValue.put(UpdateHelper.KEY_UPDATE_VERSION , "1.0");
        defaultValue.put(UpdateHelper.KEY_UPDATE_URL , "your app url on App Store");

        remoteConfig.setDefaults(defaultValue);
        remoteConfig.fetch(0)   //fetch data from Firebase every 5 seconds ,in real world ,you need make it to 1min, 5min...etc
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()){

                                remoteConfig.activateFetched();

                            }

                        }
                    });

    }
}
