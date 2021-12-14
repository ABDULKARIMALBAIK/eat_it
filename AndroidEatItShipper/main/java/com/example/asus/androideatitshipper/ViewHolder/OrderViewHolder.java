package com.example.asus.androideatitshipper.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.asus.androideatitshipper.R;

public class OrderViewHolder  extends RecyclerView.ViewHolder {


    public TextView txtOrderId , txtOrderStatus , txtOrderPhone , txtOrderAddress , txtOrderDate;
    public Button btnShipping;

    public OrderViewHolder(View itemView) {
        super(itemView);

        txtOrderId = (TextView)itemView.findViewById(R.id.order_id);
        txtOrderStatus = (TextView)itemView.findViewById(R.id.order_status);
        txtOrderPhone = (TextView)itemView.findViewById(R.id.order_phone);
        txtOrderAddress = (TextView)itemView.findViewById(R.id.order_address);
        txtOrderDate = (TextView)itemView.findViewById(R.id.order_date);

        btnShipping = (Button)itemView.findViewById(R.id.btnShipping);

    }

}