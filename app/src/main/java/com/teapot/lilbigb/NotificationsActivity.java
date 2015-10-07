package com.teapot.lilbigb;

import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.ToggleButton;


public class NotificationsActivity extends ActionBarActivity {

    private SharedPreferences.Editor prefsEditor;
    private SharedPreferences prefs;
    public static final String PREFS_NAME = "lilBIGbroPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefsEditor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        boolean notifyLocation = prefs.getBoolean("notifyLocation", false);
        boolean notifyDevice = prefs.getBoolean("notifyDevice", false);

        ToggleButton toggleLocation = (ToggleButton) findViewById(R.id.toogleLOCnotification);
        ToggleButton toggleBT = (ToggleButton) findViewById(R.id.toggleBTnotification);

        if(toggleLocation != null && toggleBT != null){
            toggleLocation.setChecked(notifyLocation);
            toggleBT.setChecked(notifyDevice);

            System.out.println(notifyDevice+", "+notifyLocation);

            toggleBT.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        prefsEditor.putBoolean("notifyDevice", true);
                    } else {
                        prefsEditor.putBoolean("notifyDevice", false);
                    }

                    prefsEditor.commit();
                }
            });

            toggleLocation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        prefsEditor.putBoolean("notifyLocation", true);
                        System.out.println("ON");
                    } else {
                        prefsEditor.putBoolean("notifyLocation", false);
                        System.out.println("OFF");
                    }
                    prefsEditor.commit();
                }
            });

        }
    }


}
