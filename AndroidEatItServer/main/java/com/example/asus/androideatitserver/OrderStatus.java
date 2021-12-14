package com.example.asus.androideatitserver;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.asus.androideatitserver.Common.Common;
import com.example.asus.androideatitserver.Model.DataMessage;
import com.example.asus.androideatitserver.Model.MyResponse;
import com.example.asus.androideatitserver.Model.Request;
import com.example.asus.androideatitserver.Model.Token;
import com.example.asus.androideatitserver.Remote.APIService;
import com.example.asus.androideatitserver.ViewHolder.OrderViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderStatus extends AppCompatActivity {

    RecyclerView recyclerView;
    MaterialSpinner spinner , shipperSpinner;

    FirebaseRecyclerAdapter<Request , OrderViewHolder> adapter;
    FirebaseDatabase database;
    DatabaseReference requests;

    APIService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);

        //FireBase
        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");

        //Init Service
        mService = Common.getFCMClient();

        //Init
        recyclerView = (RecyclerView)findViewById(R.id.listOrders);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadOrders(); //load all orders

    }

    private void loadOrders() {

        adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(
                Request.class,
                R.layout.order_layout,
                OrderViewHolder.class,
                requests
        ) {
            @Override
            protected void populateViewHolder(OrderViewHolder viewHolder, final Request model, final int position) {

                viewHolder.txtOrderId.setText(adapter.getRef(position).getKey());
                viewHolder.txtOrderStatus.setText(Common.convertCodeToStatus(model.getStatus()));
                viewHolder.txtOrderPhone.setText(model.getPhone());
                viewHolder.txtOrderAddress.setText(model.getAddress());
                viewHolder.txtOrderDate.setText(Common.getData(Long.parseLong(adapter.getRef(position).getKey())));

                viewHolder.btnEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        showUpdateDialog(adapter.getRef(position).getKey() , adapter.getItem(position));
                    }
                });

                viewHolder.btnRemove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        deleteOrder(adapter.getRef(position).getKey());
                    }
                });

                viewHolder.btnDetail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent orderDetail = new Intent(OrderStatus.this , OrderDetail.class);
                        Common.currentRequest = model;
                        orderDetail.putExtra("OrderId" , adapter.getRef(position).getKey());
                        startActivity(orderDetail);

                    }
                });

                viewHolder.btnDescription.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent trackingOrder = new Intent(OrderStatus.this , TrackingOrder.class);
                        Common.currentRequest = model;
                        startActivity(trackingOrder);
                    }
                });


            }
        };

        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
    }

    private void showUpdateDialog(String key, final Request item) {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Update Order");
        alertDialog.setMessage("Please choose status");

        View view = getLayoutInflater().inflate(R.layout.update_order_layout , null);

        spinner = (MaterialSpinner)view.findViewById(R.id.statusSpinner);
        spinner.setItems("Placed" , "On may way" , "Shipping");

        shipperSpinner = (MaterialSpinner)view.findViewById(R.id.shipperSpinner);
        //Load all shipper phone to spinner
        final List<String> shipperList = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference(Common.SHIPPERS_TABLE)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot shipperSnapShot : dataSnapshot.getChildren())
                            shipperList.add(shipperSnapShot.getKey());

                        shipperSpinner.setItems(shipperList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        alertDialog.setView(view);

        final String localKey = key;

        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
                item.setStatus(String.valueOf(spinner.getSelectedIndex()));

                if (item.getStatus().equals("2")){

                    //Copy item to table "OrderNeedShip"
                    FirebaseDatabase.getInstance().getReference(Common.ORDER_NEED_TO_SHIP_TABLE)
                            .child(shipperSpinner.getItems().get(shipperSpinner.getSelectedIndex()).toString())
                            .child(localKey)
                            .setValue(item);

                    requests.child(localKey).setValue(item);
                    adapter.notifyDataSetChanged();  //Add to update  item size

                    sendOrderStatusToUser(localKey , item);
                    sendOrderShipRequestToShipper(shipperSpinner.getItems().get(shipperSpinner.getSelectedIndex()).toString() , item);
                }
                else {

                    requests.child(localKey).setValue(item);
                    adapter.notifyDataSetChanged();  //Add to update  item size

                    sendOrderStatusToUser(localKey , item);
                }


            }
        });

        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialog.show();

    }

    private void sendOrderShipRequestToShipper(String shipperPhone, Request item) {

        DatabaseReference tokens = database.getReference("Tokens");

        tokens.child(shipperPhone)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                       if (dataSnapshot.exists()){

                           Token token = dataSnapshot.getValue(Token.class);

                           //Make raw payload
                           Map<String , String> data = new HashMap<>();
                           data.put("title" , "ABD");
                           data.put("message" , "You have new order need ship");
                           DataMessage dataMessage = new DataMessage(token.getToken() , data);

                           mService.sendNotification(dataMessage)
                                   .enqueue(new Callback<MyResponse>() {
                                       @Override
                                       public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {

                                           if (response.body().success == 1){

                                               Toast.makeText(OrderStatus.this, "Sent to shipper", Toast.LENGTH_SHORT).show();
                                           }
                                           else {
                                               Toast.makeText(OrderStatus.this, "failed to send notification !",
                                                       Toast.LENGTH_SHORT).show();
                                           }
                                       }

                                       @Override
                                       public void onFailure(Call<MyResponse> call, Throwable t) {
                                           Log.e("ERROR" , t.getMessage());
                                       }
                                   });
                       }
                       else
                           Toast.makeText(OrderStatus.this, "ERROR", Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void sendOrderStatusToUser(final String key , final Request item) {

        DatabaseReference tokens = database.getReference("Tokens");
        tokens.child(item.getPhone())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                       if (dataSnapshot.exists()){

                           Token token = dataSnapshot.getValue(Token.class);

                           //Make raw payload
                           Map<String , String> data = new HashMap<>();
                           data.put("title" , "ABD");
                           data.put("message" , "You order " + key + " was updated !");
                           DataMessage dataMessage = new DataMessage(token.getToken() , data);

                           mService.sendNotification(dataMessage)
                                   .enqueue(new Callback<MyResponse>() {
                                       @Override
                                       public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {

                                           if (response.body().success == 1){

                                               Toast.makeText(OrderStatus.this, "Order was updated !", Toast.LENGTH_SHORT).show();
                                           }
                                           else {
                                               Toast.makeText(OrderStatus.this, "Order was updated but failed to send notification !",
                                                       Toast.LENGTH_SHORT).show();
                                           }
                                       }

                                       @Override
                                       public void onFailure(Call<MyResponse> call, Throwable t) {
                                           Log.e("ERROR" , t.getMessage());
                                       }
                                   });
                       }
                       else
                           Toast.makeText(OrderStatus.this, "ERROR", Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void deleteOrder(String key) {
        requests.child(key).removeValue();
        adapter.notifyDataSetChanged();
    }

}
