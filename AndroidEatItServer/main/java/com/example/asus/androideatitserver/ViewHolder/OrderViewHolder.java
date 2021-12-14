package com.example.asus.androideatitserver.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.asus.androideatitserver.Interface.ItemClickListener;
import com.example.asus.androideatitserver.R;

public class OrderViewHolder extends RecyclerView.ViewHolder {


    public TextView txtOrderId , txtOrderStatus , txtOrderPhone , txtOrderAddress , txtOrderDate;
    public Button btnEdit , btnRemove , btnDetail , btnDescription;

    public OrderViewHolder(View itemView) {
        super(itemView);

        txtOrderId = (TextView)itemView.findViewById(R.id.order_id);
        txtOrderStatus = (TextView)itemView.findViewById(R.id.order_status);
        txtOrderPhone = (TextView)itemView.findViewById(R.id.order_phone);
        txtOrderAddress = (TextView)itemView.findViewById(R.id.order_address);
        txtOrderDate = (TextView)itemView.findViewById(R.id.order_date);

        btnEdit = (Button)itemView.findViewById(R.id.btnEdit);
        btnRemove = (Button)itemView.findViewById(R.id.btnRemove);
        btnDetail = (Button)itemView.findViewById(R.id.btnDetail);
        btnDescription = (Button)itemView.findViewById(R.id.btnDescription);

    }

}
