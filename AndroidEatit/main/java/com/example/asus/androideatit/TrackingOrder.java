package com.example.asus.androideatit;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.example.asus.androideatit.Common.Common;
import com.example.asus.androideatit.Helper.DirectionJSONParser;
import com.example.asus.androideatit.Model.Request;
import com.example.asus.androideatit.Model.ShippingInformation;
import com.example.asus.androideatit.Remote.IGoogleService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TrackingOrder extends FragmentActivity implements OnMapReadyCallback , ValueEventListener{

    private GoogleMap mMap;

    FirebaseDatabase database;
    DatabaseReference requests , shippingOrders;

    Request currentOrder;

    IGoogleService mService;
    Marker shipperMarker;

    Polyline polyline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking_order);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");
        shippingOrders = database.getReference("ShippingOrders");

        mService = Common.getGoogleMapAPI();

    }

    @Override
    protected void onStop() {

        shippingOrders.removeEventListener(this);
        super.onStop();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);

        trackLocation();
    }

    private void trackLocation() {

        requests.child(Common.currentKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        currentOrder = dataSnapshot.getValue(Request.class);

                        //If order has address
                        if (currentOrder.getAddress() != null && !currentOrder.getAddress().isEmpty()){

                            mService.getLocationFromAddress(new StringBuilder(".............................")
                            .append(currentOrder.getAddress()).toString() , Common.MAP_API_KEY)
                            .enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(Call<String> call, Response<String> response) {

                                    //Location client
                                    try {

                                        JSONObject jsonObject = new JSONObject(response.body());

                                        String lat = ((JSONArray)jsonObject.get("results"))
                                                .getJSONObject(0)
                                                .getJSONObject("geometry")
                                                .getJSONObject("location")
                                                .get("lat").toString();

                                        String lng = ((JSONArray)jsonObject.get("results"))
                                                .getJSONObject(0)
                                                .getJSONObject("geometry")
                                                .getJSONObject("location")
                                                .get("lng").toString();

                                        LatLng location;

                                        if (lat.isEmpty() || lat == null || lng.isEmpty() || lng == null)
                                             location = new LatLng(36.192984,37.117703);  //Default
                                        else
                                            location = new LatLng(Double.parseDouble(lat) , Double.parseDouble(lng));

                                        mMap.addMarker(new MarkerOptions().position(location)
                                        .title("Order destination")
                                        .icon(BitmapDescriptorFactory.defaultMarker()));


                                        //Set Shipper Location
                                        shippingOrders.child(Common.currentKey)
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                                        ShippingInformation shippingInformation = dataSnapshot.getValue(ShippingInformation.class);

                                                        LatLng shipperLocation;
                                                        if (String.valueOf(shippingInformation.getLat()) != null &&  String.valueOf(shippingInformation.getLng()) != null)
                                                            shipperLocation = new LatLng(
                                                                shippingInformation.getLat() , shippingInformation.getLng());
                                                        else
                                                            shipperLocation = new LatLng(36.192984,37.117703);

                                                        if (shipperMarker == null){

                                                            shipperMarker = mMap.addMarker(
                                                                    new MarkerOptions().position(shipperLocation)
                                                                    .title("Shipper #" + shippingInformation.getOrderId())
                                                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                                                            );
                                                        }
                                                        else {

                                                            shipperMarker.setPosition(shipperLocation);
                                                        }

                                                        //Update Camera
                                                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                                                .target(shipperLocation)
                                                                .zoom(16)
                                                                .bearing(0)
                                                                .tilt(45)
                                                                .build();

                                                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                                                        //draw routes
                                                        if (polyline != null)
                                                            polyline.remove();

                                                        mService.getDirection(shipperLocation.latitude + "," + shipperLocation.longitude ,
                                                                currentOrder.getAddress() , Common.MAP_API_KEY)
                                                                .enqueue(new Callback<String>() {
                                                                    @Override
                                                                    public void onResponse(Call<String> call, Response<String> response) {

                                                                        new ParseTask().execute(response.body().toString());
                                                                    }

                                                                    @Override
                                                                    public void onFailure(Call<String> call, Throwable t) {

                                                                    }
                                                                });
                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onFailure(Call<String> call, Throwable t) {

                                }
                            });
                        }
                        //If order has latLng
                        else if (currentOrder.getLatLng() != null && !currentOrder.getLatLng().isEmpty()){

                            mService.getLocationFromAddress(new StringBuilder(".......................")
                                    .append(currentOrder.getLatLng()).toString() , Common.MAP_API_KEY)
                                    .enqueue(new Callback<String>() {
                                        @Override
                                        public void onResponse(Call<String> call, Response<String> response) {

                                            try {

                                                JSONObject jsonObject = new JSONObject(response.body());

                                                String lat = ((JSONArray)jsonObject.get("results"))
                                                        .getJSONObject(0)
                                                        .getJSONObject("geometry")
                                                        .getJSONObject("location")
                                                        .get("lat").toString();

                                                String lng = ((JSONArray)jsonObject.get("results"))
                                                        .getJSONObject(0)
                                                        .getJSONObject("geometry")
                                                        .getJSONObject("location")
                                                        .get("lng").toString();

                                                LatLng location;

                                                if (lat.isEmpty() || lat == null || lng.isEmpty() || lng == null)
                                                    location = new LatLng(36.192984,37.117703);
                                                else
                                                    location = new LatLng(Double.parseDouble(lat) , Double.parseDouble(lng));

                                                mMap.addMarker(new MarkerOptions().position(location)
                                                        .title("Order destination")
                                                        .icon(BitmapDescriptorFactory.defaultMarker()));

                                                //Set Shipper Location
                                                shippingOrders.child(Common.currentKey)
                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                                ShippingInformation shippingInformation = dataSnapshot.getValue(ShippingInformation.class);

                                                                LatLng shipperLocation;
                                                                if (String.valueOf(shippingInformation.getLat()) != null &&  String.valueOf(shippingInformation.getLng()) != null)
                                                                    shipperLocation = new LatLng(
                                                                        shippingInformation.getLat() , shippingInformation.getLng());
                                                                else
                                                                    shipperLocation = new LatLng(36.192984,37.117703);

                                                                if (shipperMarker == null){

                                                                    shipperMarker = mMap.addMarker(
                                                                            new MarkerOptions().position(shipperLocation)
                                                                                    .title("Shipper #" + shippingInformation.getOrderId())
                                                                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                                                                    );
                                                                }
                                                                else {

                                                                    shipperMarker.setPosition(shipperLocation);
                                                                }

                                                                //Update Camera
                                                                CameraPosition cameraPosition = new CameraPosition.Builder()
                                                                        .target(shipperLocation)
                                                                        .zoom(16)
                                                                        .bearing(0)
                                                                        .tilt(45)
                                                                        .build();

                                                                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                                                                //draw routes
                                                                if (polyline != null)
                                                                    polyline.remove();

                                                                mService.getDirection(shipperLocation.latitude + "," + shipperLocation.longitude ,
                                                                        currentOrder.getLatLng() , Common.MAP_API_KEY)
                                                                        .enqueue(new Callback<String>() {
                                                                            @Override
                                                                            public void onResponse(Call<String> call, Response<String> response) {

                                                                                new ParseTask().execute(response.body().toString());
                                                                            }

                                                                            @Override
                                                                            public void onFailure(Call<String> call, Throwable t) {

                                                                            }
                                                                        });
                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });

                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<String> call, Throwable t) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {

        trackLocation();
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }

    private class ParseTask extends AsyncTask<String , Integer , List<List<HashMap<String , String>>>> {

        AlertDialog progressDialog = new SpotsDialog(TrackingOrder.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog.setMessage("Please waiting...");
            progressDialog.show();
        }

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {

            JSONObject jsonObject;
            List<List<HashMap<String , String>>> routes = null;

            try{
                jsonObject = new JSONObject(strings[0]);
                DirectionJSONParser parser = new DirectionJSONParser();

                routes =  parser.parse(jsonObject);
            }
            catch (JSONException e) {
                e.printStackTrace();
            }

            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            super.onPostExecute(lists);

            progressDialog.dismiss();

            ArrayList<LatLng> points = new ArrayList<LatLng>();;
            PolylineOptions lineOptions = new PolylineOptions();;
            lineOptions.width(2);
            lineOptions.color(Color.RED);
            MarkerOptions markerOptions = new MarkerOptions();
            // Traversing through all the routes
            for(int i=0;i<lists.size();i++){
                // Fetching i-th route
                List<HashMap<String, String>> path = lists.get(i);
                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }
                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);

            }
            // Drawing polyline in the Google Map for the i-th route
            if(points.size()!=0)mMap.addPolyline(lineOptions);//to avoid crash
        }
    }

}




