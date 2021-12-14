package com.example.asus.androideatit.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.example.asus.androideatit.Cart;
import com.example.asus.androideatit.Common.Common;
import com.example.asus.androideatit.Database.Database;
import com.example.asus.androideatit.Interface.ItemClickListener;
import com.example.asus.androideatit.Model.Food;
import com.example.asus.androideatit.Model.Order;
import com.example.asus.androideatit.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;



public class  CartAdapter extends RecyclerView.Adapter<CartViewHolder>{

    private List<Order> listData;
    private Cart cart;

    private String foodImage;
    Food currentFood;

    public CartAdapter(List<Order> listData, Cart cart) {
        this.listData = listData;
        this.cart = cart;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(cart).inflate(R.layout.cart_layout , parent , false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final CartViewHolder holder, final int position) {

        holder.txt_cart_name.setText(listData.get(position).getProductName());

        Picasso.with(cart.getBaseContext())
                .load(listData.get(position).getImage())
                .placeholder(android.R.color.holo_green_dark)
                .resize(70 , 70)
                .centerCrop()
                .into(holder.imgCart, new Callback() {
                    @Override
                    public void onSuccess() {
                       // Toast.makeText(cart, "", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError() {
                       Toast.makeText(cart, "ERROR", Toast.LENGTH_SHORT).show();
                    }
                });

        holder.btn_quantity.setNumber(listData.get(position).getQuantity());
        holder.btn_quantity.setOnValueChangeListener(new ElegantNumberButton.OnValueChangeListener() {
            @Override
            public void onValueChange(ElegantNumberButton view, int oldValue, int newValue) {

                Order order = listData.get(position);
                order.setQuantity(String.valueOf(newValue));
                new Database(cart).updateCart(order);

                //Update txtTotalPrice
                //Calculate total price
                int total = 0;
                List<Order> orders = new Database(cart).getCarts(Common.currentUser.getPhone());

                for (Order item : orders)
                    total += (Integer.parseInt(order.getPrice()) * Integer.parseInt(item.getQuantity()));

                Locale locale = new Locale("en" , "US");
                NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);

                cart.txtTotalPrice.setText(fmt.format(total));
            }
        });

        Locale locale = new Locale("en" , "US");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
        int price = Integer.parseInt(listData.get(position).getPrice())* Integer.parseInt(listData.get(position).getQuantity());
        holder.txt_price.setText(fmt.format(price));


    }
    @Override
    public int getItemCount() {
        return listData.size();
    }

    public Order getItem(int position){

        return listData.get(position);
    }

    public void removeItem(int position){

        listData.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(Order item, int position){

        listData.add(position , item);
        notifyItemInserted(position);
    }

}
