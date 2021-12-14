package com.example.asus.androideatit.ViewHolder;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.asus.androideatit.Common.Common;
import com.example.asus.androideatit.FoodDetail;
import com.example.asus.androideatit.FoodList;
import com.example.asus.androideatit.Interface.ItemClickListener;
import com.example.asus.androideatit.Model.Favorites;
import com.example.asus.androideatit.Model.Food;
import com.example.asus.androideatit.Model.Order;
import com.example.asus.androideatit.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesViewHolder>{

    private Context context;
    private List<Favorites> favoritesList;

    public FavoritesAdapter(Context context, List<Favorites> favoritesList) {
        this.context = context;
        this.favoritesList = favoritesList;
    }

    @NonNull
    @Override
    public FavoritesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.favorites_item , parent , false);

        return new FavoritesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoritesViewHolder viewHolder, int position) {

        viewHolder.food_name.setText(favoritesList.get(position).getFoodName());
        Picasso.with(context)
                .load(favoritesList.get(position).getFoodImage())
                .into(viewHolder.food_image);

        final Favorites local = favoritesList.get(position);
        viewHolder.setItemClickListener(new ItemClickListener() {
            @Override
            public void onClick(View view, int position, boolean isLongClick) {

                //Start new Activity
                Intent foodDetail = new Intent(context , FoodDetail.class);
                foodDetail.putExtra("FoodId" , favoritesList.get(position).getFoodId()); //Send Food Id to new Activity
                context.startActivity(foodDetail);
            }
        });
    }

    @Override
    public int getItemCount() {
        return favoritesList.size();
    }

    public void removeItem(int position){

        favoritesList.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(Favorites item, int position){

        favoritesList.add(position , item);
        notifyItemInserted(position);
    }

    public Favorites getItem(int position){

        return favoritesList.get(position);
    }

}
