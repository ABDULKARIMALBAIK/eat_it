package com.example.asus.androideatitserver.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.asus.androideatitserver.Common.Common;
import com.example.asus.androideatitserver.Interface.ItemClickListener;
import com.example.asus.androideatitserver.R;

public class FoodViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener , View.OnCreateContextMenuListener{

    public TextView food_name;
    public ImageView food_image;

    private ItemClickListener itemClickListener;

    public FoodViewHolder(View itemView) {
        super(itemView);

        food_name = (TextView)itemView.findViewById(R.id.food_name);
        food_image = (ImageView)itemView.findViewById(R.id.food_image);

        itemView.setOnCreateContextMenuListener(this); //For Context Menu
        itemView.setOnClickListener(this);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View v) {

        itemClickListener.onClick(v , getAdapterPosition() , false);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        menu.setHeaderTitle("Select the action");
        menu.add(0 , 0 , getAdapterPosition() , Common.UPDATE);
        menu.add(0 , 1 , getAdapterPosition() , Common.DELETE);
    }
}
