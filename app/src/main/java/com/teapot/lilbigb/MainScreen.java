package com.teapot.lilbigb;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/*
MainScreen er som navnet antyder hovedskærmen, der præsenterer brugeren for p.t. 3 valg
Kort - bruger Google Maps til at vise vores sidst kendte placering
Indstillinger - der endnu ikke er implementeret
Log ud - som lukker app'en og fjerner det gemte navn.
 */

public class MainScreen extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        // hent navnet fra shared prefs!
        SharedPreferences prefs = getSharedPreferences(loginActivity.PREFS_NAME, MODE_PRIVATE);
        String loggedInName = prefs.getString("loggedInName", null);

        // set navnet som titel :)
        getActionBar().setTitle("lil'BIGbro > "+loggedInName);

        // knapper
        Button kortBtn = (Button) findViewById(R.id.kortBtn);
        Button settingsBtn = (Button) findViewById(R.id.settingsBtn);
        Button logoutBtn = (Button) findViewById(R.id.logoutBtn);

        if(kortBtn != null && settingsBtn != null && logoutBtn != null){
            kortBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // start kort aktiviteten
                    Intent mapsActivity = new Intent(getApplicationContext(), MapActivity.class);
                    startActivity(mapsActivity);
                }
            });

            settingsBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Indstillinger er endnu ikke implementeret", Toast.LENGTH_LONG);
                    toast.show();
                }
            });

            logoutBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences.Editor prefsEditor = getSharedPreferences(loginActivity.PREFS_NAME, MODE_PRIVATE).edit();
                    prefsEditor.remove("loggedInName");
                    prefsEditor.commit();
                    Toast toast = Toast.makeText(getApplicationContext(), "Brugeren er logget ud og app'en lukkes!", Toast.LENGTH_LONG);
                    toast.show();
                    finish();
                }
            });

        }

    }

    // ingen grund til at fjerne nedenstående - det er ikke utænkeligt at vi vil bruge dropdown settings menuen senere

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    } */
}
