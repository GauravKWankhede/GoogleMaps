package com.example.googlemaps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.EncodedPolyline;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    GoogleMap map;
    LocationManager locationManager;
    double longi,lat;
    private static final int REQUEST_LOCATION = 1;
    SupportMapFragment mapFragment;
    SearchView source_searchView,destination_searchView;
    Button routeButton;
    TextView distanceTV;
    double sourceLatitude,sourceLongitude,destinationLatitude,destinationLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        source_searchView = findViewById(R.id.source_location);
        destination_searchView = findViewById(R.id.destination_location);
        routeButton = findViewById(R.id.search_route_button);
        distanceTV =findViewById(R.id.distanceTV);

         mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        source_searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                String sourcelocation =  source_searchView.getQuery().toString();
                List<Address> addressList = null;
                if (sourcelocation != null || !sourcelocation.equals(""))
                {
                    Geocoder geocoder = new Geocoder(MainActivity.this);
                    try {
                        addressList = geocoder.getFromLocationName(sourcelocation,1);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Address address  =addressList.get(0);
                     sourceLatitude = address.getLatitude();
                     sourceLongitude = address.getLongitude();
                    LatLng latLng = new LatLng(address.getLatitude(),address.getLongitude());
                    map.addMarker(new MarkerOptions().position(latLng).title(sourcelocation).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        destination_searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                String destinationlocation =  destination_searchView.getQuery().toString();
                List<Address> addressList = null;
                if (destinationlocation != null || !destinationlocation.equals(""))
                {
                    Geocoder geocoder = new Geocoder(MainActivity.this);
                    try {
                        addressList = geocoder.getFromLocationName(destinationlocation,1);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Address address  =addressList.get(0);
                    destinationLatitude = address.getLatitude();
                    destinationLongitude = address.getLongitude();
                    LatLng latLng = new LatLng(address.getLatitude(),address.getLongitude());
                    map.addMarker(new MarkerOptions().position(latLng).title(destinationlocation).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            OnGPS();
        }
        else
        {
            getCurrentLocation();
        }

        routeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TrackRoute(sourceLatitude,sourceLongitude,destinationLatitude,destinationLongitude);
                calculateDisatance(sourceLatitude,sourceLongitude,destinationLatitude,destinationLongitude);
            }

        });
    }

    private void calculateDisatance(double sourceLatitude, double sourceLongitude, double destinationLatitude, double destinationLongitude) {
        int Radius = 6371;
        double dLat = Math.toRadians(destinationLatitude-sourceLatitude);
        double dLon = Math.toRadians(destinationLongitude-sourceLongitude);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(sourceLatitude))
                * Math.cos(Math.toRadians(destinationLatitude)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));

        String distance = kmInDec+"  km";
        distanceTV.setVisibility(View.VISIBLE);
        distanceTV.setText(distance);
    }


    private void OnGPS(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("Yes", new  DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    private void getCurrentLocation(){
        if (ActivityCompat.checkSelfPermission(
                MainActivity.this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (locationGPS != null) {

                lat = locationGPS.getLatitude();

                longi = locationGPS.getLongitude();

            } else {
                Toast.makeText(this, "Unable to find location.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        LatLng location = new LatLng(lat, longi);
        map.addMarker(new MarkerOptions().position(location).title("Current Location"));
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(location,15));
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.getUiSettings().setZoomGesturesEnabled(true);
    }

    private void TrackRoute(double sourceLatitude, double sourceLongitude, double destinationLatitude, double destinationLongitude) {

        map.clear();
        String sourceLat = Double.toString(sourceLatitude);
        String sourceLongi = Double.toString(sourceLongitude);
        String sourceLocation = sourceLat +","+ sourceLongi;
        String destinationLat = Double.toString(destinationLatitude);
        String destinationLongi = Double.toString(destinationLongitude);
        String destinationLocation = destinationLat +","+ destinationLongi;

        List<LatLng> path = new ArrayList<>();

        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey("AIzaSyDzxng0yhad_q6VEqj_StG5SSte_hVE6WU")
                .build();

        DirectionsApiRequest req = DirectionsApi.getDirections(context,sourceLocation,destinationLocation);
        try {
            DirectionsResult res = req.await();

            //Loop through legs and steps to get encoded polylines of each step
            if (res.routes != null && res.routes.length > 0) {
                DirectionsRoute route = res.routes[0];

                if (route.legs !=null) {
                    for(int i=0; i<route.legs.length; i++) {
                        DirectionsLeg leg = route.legs[i];
                        if (leg.steps != null) {
                            for (int j=0; j<leg.steps.length;j++){
                                DirectionsStep step = leg.steps[j];
                                if (step.steps != null && step.steps.length >0) {
                                    for (int k=0; k<step.steps.length;k++){
                                        DirectionsStep step1 = step.steps[k];
                                        EncodedPolyline points1 = step1.polyline;
                                        if (points1 != null) {
                                            //Decode polyline and add points to list of route coordinates
                                            List<com.google.maps.model.LatLng> coords1 = points1.decodePath();
                                            for (com.google.maps.model.LatLng coord1 : coords1) {
                                                path.add(new LatLng(coord1.lat, coord1.lng));
                                            }
                                        }
                                    }
                                }else {
                                    EncodedPolyline points = step.polyline;
                                    if (points != null) {
                                        //Decode polyline and add points to list of route coordinates
                                        List<com.google.maps.model.LatLng> coords = points.decodePath();
                                        for (com.google.maps.model.LatLng coord : coords) {
                                            path.add(new LatLng(coord.lat, coord.lng));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e("Exception", ""+e);
        }
        if (path.size() > 0) {
            PolylineOptions opts = new PolylineOptions().addAll(path).color(Color.BLUE).width(5);
            map.addPolyline(opts);
        }
        map.getUiSettings().setZoomControlsEnabled(true);


    }
}