package com.teapot.lilbigb;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.*;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends Activity implements ConnectionCallbacks, OnConnectionFailedListener {

    static LatLng myPos; // til at holde vores position
    private GoogleMap map;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_layout);

        // vi instantierer kortet og sætter typen til satellit
        // kortet er et view af typen fragment med id map
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
                .getMap();
        map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

        // build client metoden . bruges til at indstille og forbinde til google api!
        // se metoden for mere
        buildGoogleApiClient();
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

        if(mLastLocation != null){
            myPos = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            map.addMarker(new MarkerOptions()
                    .position(myPos)
                            //.icon((BitmapDescriptorFactory.fromResource(R.drawable.eye_icon)))
                    .title("Min sidst kendte position"));
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
}
