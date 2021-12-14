package com.example.asus.androideatit;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import com.example.asus.androideatit.Common.Common;
import com.example.asus.androideatit.Model.Rating;
import com.example.asus.androideatit.ViewHolder.ShowCommentViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class ShowComment extends AppCompatActivity {

    RecyclerView recyclerView;

    FirebaseDatabase database;
    DatabaseReference ratingTbl;

    SwipeRefreshLayout mSwipeRefreshLayout;
    FirebaseRecyclerAdapter<Rating , ShowCommentViewHolder> adapter;

    String foodId = "";

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);


    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
        .setDefaultFontPath("fonts/restaurant_font.otf")
        .setFontAttrId(R.attr.fontPath)
        .build());

        setContentView(R.layout.activity_show_comment);

        //FireBase
        database = FirebaseDatabase.getInstance();
        ratingTbl = database.getReference("Rating");

        recyclerView = (RecyclerView)findViewById(R.id.recyclerComment);
        recyclerView.setVisibility(View.VISIBLE);

        //Swipe Refresh Layout
        mSwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if (getIntent().getExtras() != null)
                    foodId = getIntent().getStringExtra(Common.INTENT_FOOD_ID);

                if (!foodId.isEmpty() && foodId != null){

                    adapter = new FirebaseRecyclerAdapter<Rating, ShowCommentViewHolder>(
                            Rating.class,
                            R.layout.show_comment_layout,
                            ShowCommentViewHolder.class,
                            ratingTbl.orderByChild("foodId").equalTo(foodId)
                    ) {
                        @Override
                        protected void populateViewHolder(ShowCommentViewHolder viewHolder, Rating model, int position) {

                            viewHolder.ratingBar.setRating(Float.parseFloat(model.getRateValue()));
                            viewHolder.txtUserPhone.setText(model.getUserPhone());
                            viewHolder.txtComment.setText(model.getComment());
                        }
                    };
                    recyclerView.setAdapter(adapter);
                    mSwipeRefreshLayout.setRefreshing(false);
                    startAnimation();
                }

            }
        });

        //Thread to load comment on first lunch
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {

                mSwipeRefreshLayout.setRefreshing(true);

                if (getIntent().getExtras() != null)
                    foodId = getIntent().getStringExtra(Common.INTENT_FOOD_ID);

                if (!foodId.isEmpty() && foodId != null){

                    adapter = new FirebaseRecyclerAdapter<Rating, ShowCommentViewHolder>(
                            Rating.class,
                            R.layout.show_comment_layout,
                            ShowCommentViewHolder.class,
                            ratingTbl.orderByChild("foodId").equalTo(foodId)
                    ) {
                        @Override
                        protected void populateViewHolder(ShowCommentViewHolder viewHolder, Rating model, int position) {

                            viewHolder.ratingBar.setRating(Float.parseFloat(model.getRateValue()));
                            viewHolder.txtUserPhone.setText(model.getUserPhone());
                            viewHolder.txtComment.setText(model.getComment());
                        }
                    };

                    recyclerView.setAdapter(adapter);
                    mSwipeRefreshLayout.setRefreshing(false);
                    startAnimation();
                }
            }
        });
    }

    private void startAnimation() {

        Context context = recyclerView.getContext();
        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(context , R.anim.layout_slide_right);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerView.setLayoutAnimation(controller);
        recyclerView.getAdapter().notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();


    }
}
