package com.teapot.lilbigb;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;

import android.location.LocationManager;
import android.os.Bundle;

import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.*;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends Activity implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    static LatLng myPos; // til at holde vores position
    private GoogleMap map;
    private GoogleApiClient mGoogleApiClient;
    private boolean bUpdatePosition = true;
    private LocationRequest mLocationRequest;
    private Marker myMarker;

    private Bitmap userBitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_layout);

        // check om gps er tændt
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
        }

        userBitmap = (Bitmap) getIntent().getParcelableExtra("UserBitmap");

        // vi instantierer kortet og sætter typen til satellit
        // kortet er et view af typen fragment med id map
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
                .getMap();

        // build client metoden . bruges til at indstille og forbinde til google api!
        // se metoden for mere
        buildGoogleApiClient();
        createLocationRequest();
    }

    protected synchronized void buildGoogleApiClient() {
        Toast toast = Toast.makeText(getApplicationContext(), "Bygger og forbinder API!", Toast.LENGTH_SHORT);
        toast.show();

        // nærmest som taget ud af googles docs
        // se evt. https://developer.android.com/training/location/retrieve-current.html

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {

        /*
        Når API'en er forbundet kan vi finde vores sidst kendte position
         */

        Toast toast = Toast.makeText(getApplicationContext(), "Vi er forbundet med API'en!", Toast.LENGTH_SHORT);
        toast.show();

        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        Bitmap userBitmapScaled = Bitmap.createScaledBitmap(userBitmap, 117, 117, false);
        SharedPreferences prefs = getSharedPreferences(loginActivity.PREFS_NAME, MODE_PRIVATE);
        String loggedInName = prefs.getString("loggedInName", null);

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inMutable = true;
        //modify canvas
        Bitmap navBG = BitmapFactory.decodeResource(getResources(), R.drawable.lilbigbro_marker_bg, opts);

        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap bmp = Bitmap.createBitmap(175, 204, conf);
        Canvas canvas1 = new Canvas(bmp);

        System.out.println("APP img dimensions: "+userBitmapScaled.getWidth()+", "+userBitmapScaled.getHeight());

        // paint defines the text color,
        // stroke width, size
        Paint color = new Paint();
        color.setTextSize(35);
        color.setColor(Color.WHITE);

        Bitmap navBGScaled = Bitmap.createScaledBitmap(navBG, 175,204, false);

        canvas1.drawBitmap(navBGScaled, 0, 0, color);
        canvas1.drawBitmap(userBitmapScaled, 30, 15, color);
        canvas1.drawText(loggedInName, 5, 170, color);


        if(mLastLocation != null){
            myPos = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            myMarker = map.addMarker(new MarkerOptions()
                    .position(myPos)
                    .icon(BitmapDescriptorFactory.fromBitmap(bmp))
                            //.icon((BitmapDescriptorFactory.fromResource(R.drawable.eye_icon)))
                    .title("Min sidst kendte position")
                    .anchor(0.5f, 1));
        } else { // skulle det ske at sidst kente placering ikke er kendt eller kan findes (null)
            myPos = new LatLng(56.1572, 10.2107); // sæt længde/bredde grad til Aarhus
        }

        // indstil cam.
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(myPos, 15));
        map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
        CameraPosition camPos = new CameraPosition.Builder()
                .target(myPos)
                .zoom(20)
                .bearing(90)
                .tilt(40)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(camPos));


        // setup kontinuerlig position
        //if(bUpdatePosition){
           startLocationUpdates();
        //}

    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi   .requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(500);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // hvis vi nu ikke kunme forbinde - send en "toast" notification til brugeren
        Toast toast = Toast.makeText(getApplicationContext(), "Der kunne ikke forbindes til API'en!", Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
        System.out.println("PAUSE!");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() && !bUpdatePosition) {
            startLocationUpdates();
        }

        System.out.println("RESUME!");
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    private void buildAlertMessageNoGps() {

        // metoden bliver kørt hvis GPS'en ikke er sat til

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("GPS er deaktiveret!\nKort fungerer bedst med aktiv GPS!")
                .setCancelable(false)
                .setPositiveButton("Gå til placerings indstillinger", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));

                    }
                })
                .setNegativeButton("Ignorer", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onLocationChanged(Location location) {

        // nærmest en komplet kopi af onconnected

        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);


        if(mLastLocation != null){
            myPos = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            myMarker.setPosition(myPos);
        }

        System.out.println("Location update: " + myPos.toString());
        map.animateCamera(CameraUpdateFactory.newLatLng(myPos), 2000, null);
    }
}
