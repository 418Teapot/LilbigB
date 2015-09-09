package com.teapot.lilbigb;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;


public class loginActivity extends Activity {

    /**
     * Login class til vores lil'BIGbro app!
     * Planen er i første omgang at benytte sig af shared preferences i Android
     * til at gemme et brugernavn! Hvis brugernavnet er tomt vises login skærmen
     * ellers går app'en videre til main screen
     * @param savedInstanceState
     */

    // prefs/settings til at gemme vores navn!
    private SharedPreferences.Editor prefsEditor;
    private SharedPreferences prefs;
    public static final String PREFS_NAME = "lilBIGbroPrefs";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String loggedInName = prefs.getString("loggedInName", null);
        if (loggedInName != null) {
            /*
            loggedInName holder en værdi - hvilket betyder vi er logget ind mindst en gang før!
            Vi bygger en ny intention (naming android! WOW!) og går videre til main screen!
             */
            Intent mainIntent = new Intent(getApplicationContext(), MainScreen.class);
            /* vi putter en ekstra streng værdi i det nye intent - så vi kan bruge navnet!
            kunne også sagtens implementeres til altid at hente fra sharedprefs! */
            mainIntent.putExtra("loggedInName", loggedInName);
            startActivity(mainIntent);
            // vi afslutter den nuværende aktivitet efter vi har startet den nye!
            finish();

        } else {
            // håndter login!

            // vi finder vores "views" (knapper og tekstfelter - igen wow naming android :) )
            Button loginButton = (Button) findViewById(R.id.loginButton);
            final EditText nameText = (EditText) findViewById(R.id.navnText);

            // hvis elementerne vi skal bruge findes!
            if(loginButton != null && nameText != null){
                loginButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // vi gemmer vores login navn i shared prefs!
                        prefsEditor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
                        prefsEditor.putString("loggedInName", nameText.getText().toString());
                        prefsEditor.commit();

                        // same as above ;)
                        Intent mainIntent = new Intent(getApplicationContext(), MainScreen.class);
                        mainIntent.putExtra("loggedInName", nameText.getText().toString());
                        startActivity(mainIntent);
                        finish();
                    }
                });
            }

        }


    }

}
