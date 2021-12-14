package com.example.asus.androideatit;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.example.asus.androideatit.Common.Common;
import com.example.asus.androideatit.Database.Database;
import com.example.asus.androideatit.Interface.ItemClickListener;
import com.example.asus.androideatit.Model.Banner;
import com.example.asus.androideatit.Model.Category;
import com.example.asus.androideatit.Model.Token;
import com.example.asus.androideatit.Model.User;
import com.example.asus.androideatit.ViewHolder.MenuViewHolder;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rey.material.widget.CheckBox;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Home extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    FirebaseDatabase database;
    DatabaseReference category;

    TextView txtFullName;

    RecyclerView recycler_menu;
    FirebaseRecyclerAdapter<Category , MenuViewHolder> adapter;

    SwipeRefreshLayout swipeRefreshLayout;
    CounterFab fab;

    //Slider
    HashMap<String , String> image_list;
    SliderLayout mSlider;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));

    }

    @Override
    protected void onStop() {
        super.onStop();

        mSlider.stopAutoCycle();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Note: add this code before setContentView method
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/restaurant_font.otf")
                .setFontAttrId(R.attr.fontPath)
                .build());

        setContentView(R.layout.activity_home);

        //init Paper
        Paper.init(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Menu");
        setSupportActionBar(toolbar);

        //RecyclerView & Load menu
        recycler_menu = (RecyclerView)findViewById(R.id.recycler_menu);
        recycler_menu.setVisibility(View.VISIBLE);

        //Init Swipe Refresh Layout
        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark
                );
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if (Common.isConnectionToInternet(getBaseContext())){

                    loadMenu();
                    swipeRefreshLayout.setRefreshing(false);
                    startRecyclerViewAnimation();

                }
                else {
                    Toast.makeText(getBaseContext(), "Please check your connection !!!", Toast.LENGTH_SHORT).show();
                    return;
                }

            }
        });

        //Default , load for first time
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {

                if (Common.isConnectionToInternet(getBaseContext())){

                    loadMenu();
                    swipeRefreshLayout.setRefreshing(false);
                    startRecyclerViewAnimation();
                }
                else {
                    Toast.makeText(getBaseContext(), "Please check your connection !!!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });

        //Init Firebase
        database = FirebaseDatabase.getInstance();
        category = database.getReference("Category");

        //Make sure you move this function after database is getInstance()
        if (Common.isConnectionToInternet(getBaseContext())){

            loadMenu();
            recycler_menu.setAdapter(adapter);
            swipeRefreshLayout.setRefreshing(false);
            startRecyclerViewAnimation();
        }
        else {
            Toast.makeText(getBaseContext(), "Please check your connection !!!", Toast.LENGTH_SHORT).show();
            return;
        }


        fab = (CounterFab) findViewById(R.id.fab);
        fab.setColorFilter(getResources().getColor(R.color.colorPrimary));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent cartIntent = new Intent(Home.this , Cart.class);
                startActivity(cartIntent);
            }
        });
        fab.setCount(new Database(this).getCountCart(Common.currentUser.getPhone()));


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Set name for user
        View headerView = navigationView.getHeaderView(0);
        txtFullName = (TextView)headerView.findViewById(R.id.txtFullName);
        txtFullName.setText(Common.currentUser.getName());

        //Send Token
        updateToken(FirebaseInstanceId.getInstance().getToken());

        //Setup Slider
        //Need call this function after you init database FireBase
        setupSlider();
    }

    private void setupSlider() {

        mSlider = (SliderLayout)findViewById(R.id.slider);
        image_list = new HashMap<>();

        final DatabaseReference banners = database.getReference("Banner");
        banners.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot postSnapShot : dataSnapshot.getChildren()){

                    Banner banner = postSnapShot.getValue(Banner.class);

                    //We will concat string name and id like
                    //PIZZA@@@01 => And we will use , use PIZZA for show description , 01 for food id to click
                    image_list.put(banner.getName() + "@@@" + banner.getId() , banner.getImage());
                }

                for (String key : image_list.keySet()){

                    String[] keySplit = key.split("@@@");
                    String nameOfFood = keySplit[0];
                    String idOfFood = keySplit[1];

                    //Create Slider
                    final TextSliderView textSliderView = new TextSliderView(getBaseContext());
                    textSliderView
                            .description(nameOfFood)
                            .image(image_list.get(key))
                            .setScaleType(BaseSliderView.ScaleType.Fit)
                            .setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
                                @Override
                                public void onSliderClick(BaseSliderView slider) {

                                    Intent intent = new Intent(Home.this , FoodDetail.class);
                                    //We will send food id to FoodFetail
                                    intent.putExtras(textSliderView.getBundle());
                                    startActivity(intent);
                                }
                            });

                    //Add extra bundle
                    textSliderView.bundle(new Bundle());
                    textSliderView.getBundle().putString("FoodId" , idOfFood);

                    mSlider.addSlider(textSliderView);

                    //Remove event after finish
                    banners.removeEventListener(this);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mSlider.setPresetTransformer(SliderLayout.Transformer.Background2Foreground);
        mSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        mSlider.setCustomAnimation(new DescriptionAnimation());
        mSlider.setDuration(4000);


    }

    private void startRecyclerViewAnimation() {

        Context context = recycler_menu.getContext();
        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(
                context , R.anim.layout_fall_down);

        recycler_menu.setHasFixedSize(true);
        recycler_menu.setLayoutManager(new GridLayoutManager(this , 2));

        //Set Animation
        recycler_menu.setLayoutAnimation(controller);
        recycler_menu.getAdapter().notifyDataSetChanged();
        recycler_menu.scheduleLayoutAnimation();
    }

    private void updateToken(String token) {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference tokens = database.getReference("Tokens");
        Token data = new Token(token , false);  //false becuz this token send from Client App
        tokens.child(Common.currentUser.getPhone()).setValue(data);
    }

    private void loadMenu() {

        adapter = new FirebaseRecyclerAdapter<Category, MenuViewHolder>(
                Category.class ,
                R.layout.menu_item ,
                MenuViewHolder.class ,
                category) {
            @Override
            protected void populateViewHolder(MenuViewHolder viewHolder, Category model, int position) {

                viewHolder.txtMenuName.setText(model.getName());
                Picasso.with(getBaseContext())
                        .load(model.getImage())
                        .into(viewHolder.imageView);

                final Category clickItem = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {

                        //Get CategoryId and send to new Activity
                        Intent foodList = new Intent(Home.this , FoodList.class);

                        //Because CategoryId is key , so we just get key of this item
                        foodList.putExtra("CategoryId" , adapter.getRef(position).getKey());
                        startActivity(foodList);

                    }
                });
            }
        };

    }

    @Override
    protected void onResume() {
        super.onResume();
        fab.setCount(new Database(this).getCountCart(Common.currentUser.getPhone()));

        //Fix click back button from Food and don't see category
        if (adapter != null)
            adapter.notifyDataSetChanged();

        mSlider.startAutoCycle();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.menu_search){

            startActivity(new Intent(Home.this , SearchActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_menu) {
            // Handle the camera action
        }

        else if (id == R.id.nav_cart) {
            if(Common.isConnectionToInternet(this)){

                Intent cartIntent = new Intent(Home.this , Cart.class);
                startActivity(cartIntent);
            }
            else
                Toast.makeText(this, "Please check your connection !!!", Toast.LENGTH_SHORT).show();

        }
        else if (id == R.id.nav_orders) {

            if(Common.isConnectionToInternet(this)){

                Intent orderIntent = new Intent(Home.this , OrderStatus.class);
                orderIntent.putExtra("userPhone" , "-1");
                startActivity(orderIntent);

            }
            else
                Toast.makeText(this, "Please check your connection !!!", Toast.LENGTH_SHORT).show();

        }
        else if (id == R.id.nav_change_pwd) {

            if(Common.isConnectionToInternet(this)){

                showChangePasswordDialog();
            }
            else
                Toast.makeText(this, "Please check your connection !!!", Toast.LENGTH_SHORT).show();



        }
        else if (id == R.id.nav_change_name) {

            if(Common.isConnectionToInternet(this)){

                showChangeNameDialog();
            }
            else
                Toast.makeText(this, "Please check your connection !!!", Toast.LENGTH_SHORT).show();



        }
        else if (id == R.id.nav_log_out) {

            if(Common.isConnectionToInternet(this)){

                //Delete Remember user & password
                Paper.book().destroy();

                //Remove Carts
                new Database(getApplicationContext()).cleanCart(Common.currentUser.getPhone());

                //Remove the user's account in FireBase's database (case sign in by firebase account)
                //Remove the user's account in FireBase's database (case sign in by facebook account)
                DatabaseReference users = FirebaseDatabase.getInstance().getReference("User");
                users.child(Common.currentUser.getPhone())
                        .removeValue();

                //If sign in by facebook account => will remove it in Facebook account kit
                if (AccountKit.getCurrentAccessToken() != null){

                    AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                        @Override
                        public void onSuccess(Account account) {

                            if (account.getPhoneNumber().toString().equals(Common.currentUser.getPhone()))
                                AccountKit.logOut();
                        }

                        @Override
                        public void onError(AccountKitError accountKitError) {
                            Toast.makeText(Home.this, accountKitError.getErrorType().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }

                //Make  Common.currentUser is null
                Common.currentUser = null;

                Intent mainActivity = new Intent(Home.this , MainActivity.class);
                mainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(mainActivity);
            }
            else
                Toast.makeText(this, "Please check your connection !!!", Toast.LENGTH_SHORT).show();

        }
        else if (id == R.id.nav_home_address){

            showHomeAddressDialog();
        }
        else if (id == R.id.nav_settings){

            showSettingsDialog();
        }
        else if (id == R.id.nav_favorites){

            startActivity(new Intent(Home.this , FavoritesActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showHomeAddressDialog() {

        AlertDialog.Builder alertDialog= new AlertDialog.Builder(this)
                .setTitle("CHANGE HOME ADDRESS")
                .setMessage("Please fill all information");

        View view = getLayoutInflater().inflate(R.layout.home_address_layout , null);

        final MaterialEditText edtHomeAddress = (MaterialEditText)view.findViewById(R.id.edtHomeAddress);
        if (Common.currentUser.getHomeAddress() != null && !Common.currentUser.getHomeAddress().isEmpty())
            edtHomeAddress.setText(Common.currentUser.getHomeAddress());

        alertDialog.setView(view);

        alertDialog.setPositiveButton("CHANGE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {


                //set new Home Address
                Common.currentUser.setHomeAddress(edtHomeAddress.getText().toString());

                FirebaseDatabase.getInstance().getReference("User")
                        .child(Common.currentUser.getPhone())
                        .setValue(Common.currentUser)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()){
                                    Toast.makeText(Home.this, "Update Home Address successful !", Toast.LENGTH_SHORT)
                                            .show();
                                    changeCurrentUser();
                                }

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(Home.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });


        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialog.show();
    }

    private void showSettingsDialog() {

        AlertDialog.Builder alertDialog= new AlertDialog.Builder(this)
                .setTitle("SETTINGS")
                .setMessage("Please fill all information");

        View view = getLayoutInflater().inflate(R.layout.setting_layout , null);
        final CheckBox ckb_subscribe_new = (CheckBox)view.findViewById(R.id.ckb_sub_news);

        //Add code remember state of checkbox
        Paper.init(Home.this);

        String isSubscribe = Paper.book().read("sub_new");
        if (isSubscribe == null || TextUtils.isEmpty(isSubscribe) || isSubscribe.equals("false"))
            ckb_subscribe_new.setChecked(false);
        else
            ckb_subscribe_new.setChecked(true);

        alertDialog.setView(view);

        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

                if (ckb_subscribe_new.isChecked()){

                    FirebaseMessaging.getInstance().subscribeToTopic(Common.topicName);
                    //Write value
                    Paper.book().write("sub_new" , "true");
                }
                else {

                    FirebaseMessaging.getInstance().subscribeToTopic(Common.topicName);
                    //Write value
                    Paper.book().write("sub_new" , "false");
                }
            }
        });


        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialog.show();
    }

    private void showChangeNameDialog() {

        AlertDialog.Builder alertDialog= new AlertDialog.Builder(this)
                .setTitle("UPDATE Name")
                .setMessage("Please fill all information");

        View view = getLayoutInflater().inflate(R.layout.change_name_layout , null);
        final MaterialEditText edtName = (MaterialEditText)view.findViewById(R.id.edtName);
        alertDialog.setView(view);

        alertDialog.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //use android.app.AlertDialog for SpotsDialog , not from v7 in AlertDialog
                final android.app.AlertDialog waitingDialog = new SpotsDialog(Home.this);
                waitingDialog.show();
                waitingDialog.setMessage("Please waiting...");

                //Update Name
                Map<String , Object> update_name = new HashMap<>();
                update_name.put("name" , edtName.getText().toString());

                FirebaseDatabase.getInstance().getReference("User")
                        .child(Common.currentUser.getPhone())
                        .updateChildren(update_name)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                waitingDialog.dismiss();

                                if (task.isSuccessful()){
                                    Toast.makeText(Home.this, "Name was updated !", Toast.LENGTH_SHORT).show();
                                    changeCurrentUser();
                                }

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                waitingDialog.dismiss();
                                Toast.makeText(Home.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });


        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialog.show();

    }

    private void showChangePasswordDialog() {

        AlertDialog.Builder alertDialog= new AlertDialog.Builder(this)
                .setTitle("CHANGE PASSWORD")
                .setMessage("Please fill all information");

        View view = getLayoutInflater().inflate(R.layout.change_password_layout , null);
        final MaterialEditText edtPassword = (MaterialEditText)view.findViewById(R.id.edtPassword);
        final MaterialEditText edtNewPassword = (MaterialEditText)view.findViewById(R.id.edtNewPassword);
        final MaterialEditText edtRepeatPassword = (MaterialEditText)view.findViewById(R.id.edtRepeatPassword);
        alertDialog.setView(view);

        alertDialog.setPositiveButton("CHANGE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //Change password here
                
                //use android.app.AlertDialog for SpotsDialog , not from v7 in AlertDialog
                final android.app.AlertDialog waitingDialog = new SpotsDialog(Home.this);
                waitingDialog.show();
                waitingDialog.setMessage("Please waiting...");

                //Check old Password
                if (edtPassword.getText().toString().equals(Common.currentUser.getPassword())){

                    //Check new password and repeat password
                    if (edtNewPassword.getText().toString().equals(edtRepeatPassword.getText().toString())){

                        Map<String , Object> passwordUpdate = new HashMap<>();
                        passwordUpdate.put("password" , edtNewPassword.getText().toString());

                        //Make update
                        final DatabaseReference user = FirebaseDatabase.getInstance().getReference("User");
                        user.child(Common.currentUser.getPhone())
                                .updateChildren(passwordUpdate)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        changeCurrentUser();
                                        
                                        waitingDialog.dismiss();
                                        Toast.makeText(Home.this, "Password was updated !", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        Toast.makeText(Home.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                    }
                    else {
                        waitingDialog.dismiss();
                        Toast.makeText(Home.this, "New password doesn't match", Toast.LENGTH_SHORT).show();
                    }

                }
                else{
                    waitingDialog.dismiss();
                    Toast.makeText(Home.this, "Wrong old password !", Toast.LENGTH_SHORT).show();
                }


            }
        });

        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialog.show();

    }

    private void changeCurrentUser() {

        if (Common.isConnectionToInternet(Home.this)) {

            DatabaseReference user = database.getReference("User");

            user.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                    User user = dataSnapshot.child(Common.currentUser.getPhone()).getValue(User.class);
                    user.setPhone(Common.currentUser.getPhone()); //set Phone

                    Common.currentUser = user;
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        else {
            Toast.makeText(Home.this, "Plaese check your connection !!!", Toast.LENGTH_SHORT).show();
            return;
        }
    }
}
