package com.wkdnotes.root.safedrivescratch;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.wkdnotes.root.safedrivescratch.Util.Data;

import java.util.ArrayList;
import java.util.List;

import static android.graphics.Color.CYAN;
import static android.graphics.Color.GRAY;
import static android.graphics.Color.MAGENTA;
import static android.graphics.Color.WHITE;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, RoutingListener
{
    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    SupportMapFragment mapFragment;
    Location mLastLocation;
    LatLng lastLatLng;
    LatLng destinationLatLng;
    LocationRequest mLocationRequest;
    private Marker mDriverMarker;
    private Marker mDestinationMarker;
    public static Data data;
    private Data.onGpsServiceUpdate onGpsServiceUpdate;

    private static boolean initialMapLoadingFLag = false;

    FloatingActionButton fab;
    final int LOCATION_REQUEST_CODE = 1;
    private String destination;
    private ProgressDialog progressDialog;
    private List<Polyline> polylines;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location InitialLocation;
    private TextView speed;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mFusedLocationProviderClient=LocationServices.getFusedLocationProviderClient(this);

        fab=(FloatingActionButton)findViewById(R.id.fab);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        polylines = new ArrayList<>();
        mapFragment.getMapAsync(this);

        speed=(TextView)findViewById(R.id.speed);
       Typeface font=Typeface.createFromAsset(getAssets(),"font/digital.ttf");
       speed.setTypeface(font);
    //       speed.setTextAppearance(R.style.custom_name);
      //  speed.setTextColor(getResources().getColor(R.color.white));

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener()
        {
            @Override
            public void onPlaceSelected(Place place)
            {
                destination = place.getName().toString();
                destinationLatLng = place.getLatLng();
            }

            @Override
            public void onError(Status status)
            {
            }
        });

    }

    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json));
        //mMap.setMapType(mMap.MAP_TYPE_SATELLITE);

        //check for permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }

        buildGoogleApiClient();//initialize google API client

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setTiltGesturesEnabled(true);
        mMap.setTrafficEnabled(true);

        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick()
            {
                if(mLastLocation!=null)
                {
                    LatLng latLng = new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(18));
                }
                return true;
            }
        });

        getDeviceCurrentLocation();
    }

    public void getDeviceCurrentLocation()
    {
        try {
            if (true) {
                Task locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {

                            // Set the map's camera position to the current location of the device.
                            InitialLocation = (Location) task.getResult();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(InitialLocation.getLatitude(), InitialLocation.getLongitude()), 18));

                        } else {
                            Log.d("Maps Activity", "Current location is null. Using defaults.");
                            Log.e("Maps Activity", "Exception: %s", task.getException());
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }
    protected synchronized void buildGoogleApiClient() //initialize google API client
    {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location)
    {

        updateCameraBearing(mMap, location.getBearing());
        if(mLastLocation!=null)
        {
            lastLatLng = new LatLng(location.getLatitude(), location.getLongitude());


            float bearing = mLastLocation.bearingTo(location);

            if(mDriverMarker!=null)
            {
                mDriverMarker.remove();
            }
            mDriverMarker = mMap.addMarker(new MarkerOptions().position(lastLatLng).title("you are here").icon(BitmapDescriptorFactory.fromResource(R.mipmap.car)).anchor(0.5f, 0.5f).rotation(bearing).flat(true));
        }
        else
        {
            lastLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            if(mDriverMarker!=null)
            {
                mDriverMarker.remove();
            }
            mDriverMarker = mMap.addMarker(new MarkerOptions().position(lastLatLng).title("you are here").icon(BitmapDescriptorFactory.fromResource(R.mipmap.car)));
        }


        mLastLocation = location;


        //Toast.makeText(getApplicationContext(),String.valueOf(Data.speedForTraffic),Toast.LENGTH_SHORT).show();

        String speedunit;
         if(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("miles_per_hour",false))
             speedunit="mi/h";
         else
             speedunit="km/h";
        SpannableString s = new SpannableString(String.format("%.0f", Data.speedForTraffic) +speedunit );
        s.setSpan(new RelativeSizeSpan(0.5f), s.length() - 4, s.length(), 0);
        speed.setText(s);
    }

    private void updateCameraBearing(GoogleMap googleMap, float bearing)
    {
        if ( googleMap == null)
            return;

        CameraPosition camPos = CameraPosition.builder(googleMap.getCameraPosition()).bearing(bearing).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos));
    }

    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setSmallestDisplacement(2);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        //request for location permission
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(MapsActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i)
    {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode)
        {
            case LOCATION_REQUEST_CODE:
                if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED)
                {
                    mapFragment.getMapAsync(this);
                }
                else
                {
                    Toast.makeText(MapsActivity.this,"Please Provide the permission",Toast.LENGTH_SHORT).show();
                    ActivityCompat.requestPermissions(MapsActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);
                }
        }
    }

    /////////////////////////////////////////////Routing PolyLine Drawing//////////////////////////////////////////////////////////////////


    public void route(View view)
    {
        if(destinationLatLng!=null)
        {
            if (mDestinationMarker != null)
            {
                mDestinationMarker.remove();
            }
            mDestinationMarker = mMap.addMarker(new MarkerOptions().position(destinationLatLng).title("Destination").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_pickup)));

            progressDialog = ProgressDialog.show(this, "Please wait.", "Fetching route information.", true);

            Routing routing = new Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(this)
                    .alternativeRoutes(true)
                    .waypoints(lastLatLng, destinationLatLng)
                    .build();
            routing.execute();
        }
        else
        {
            Toast.makeText(getApplicationContext(),"Select destination",Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onRoutingFailure(RouteException e)
    {
        progressDialog.dismiss();
        Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRoutingStart()
    {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex)
    {
        progressDialog.dismiss();

        if(polylines.size()>0)
        {
            for (Polyline poly : polylines)
            {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();

        for (int i = 0; i <route.size(); i++)
        {
            //In case of more than 5 alternative routes
            // int colorIndex = i / COLORS.length;
            int color = 0;

            switch(i)
            {
                case 0:
                    color = CYAN;
                    break;
                case 1:
                    color = WHITE;
                    break;
                case 2:
                    color = GRAY;
                    break;
                case 3:
                    color = MAGENTA;
                    break;
            }

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(color);
            polyOptions.width(17 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

        }

    }

    @Override
    public void onRoutingCancelled()
    {

    }

//    public class SpeedReceiver extends BroadcastReceiver {
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            // Here you have the received broadcast
//            // And if you added extras to the intent get them here too
//            // this needs some null checks
//            Bundle b = intent.getExtras();
//            String yourValue = b.getString("speed");
//            speed.setText(yourValue);
//            double someDouble = b.getDouble("doubleName");
//            ///do something with someDouble
//        }
//    }


}
