package com.example.asus.androideatitserver;

import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.asus.androideatitserver.Common.Common;
import com.example.asus.androideatitserver.Model.DataMessage;
import com.example.asus.androideatitserver.Model.MyResponse;
import com.example.asus.androideatitserver.Remote.APIService;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SendMessage extends AppCompatActivity {

    MaterialEditText edtTitle , edtMessage;
    Button btnSend;
    RelativeLayout rootLayout;

    APIService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);

        mService = Common.getFCMClient();

        edtTitle = (MaterialEditText)findViewById(R.id.edtTitle);
        edtMessage = (MaterialEditText)findViewById(R.id.edtMessage);
        btnSend = (Button)findViewById(R.id.btnSend);
        rootLayout = (RelativeLayout)findViewById(R.id.rootLayout);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Create Message
//                Notification notification = new Notification(edtTitle.getText().toString() , edtMessage.getText().toString());
//                Sender toTopic = new Sender();
                Map<String , String> data = new HashMap<>();
                data.put("title" , edtTitle.getText().toString());
                data.put("message" , edtMessage.getText().toString());

                DataMessage dataMessage = new DataMessage(new StringBuilder("/topics/").append(Common.topicName).toString() , data);

//                toTopic.to = new StringBuilder("/topics/").append(Common.topicName).toString();
//                toTopic.notification = notification;

                mService.sendNotification(dataMessage)
                        .enqueue(new Callback<MyResponse>() {
                            @Override
                            public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {

                                if (response.isSuccessful())
                                    Snackbar.make(rootLayout  , "Message Sent" , Snackbar.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(Call<MyResponse> call, Throwable t) {
                                Toast.makeText(SendMessage.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }
}
