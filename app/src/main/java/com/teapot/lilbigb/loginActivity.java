package com.teapot.lilbigb;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;


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
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    CallbackManager callbackManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        System.out.println("APP ID: " + FacebookSdk.getApplicationSignature(getApplicationContext()));

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        setContentView(R.layout.activity_login);
        callbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("user_friends");

        // Other app specific specialization

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code

                System.out.println("APP User AccesToken: "+loginResult.getAccessToken());
                System.out.println("APP: " + loginResult);
                GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject jsonObject, GraphResponse graphResponse) {
                        System.out.println("APP Graph Response Obj: "+jsonObject.toString());

                        Intent mainIntent = new Intent(getApplicationContext(), MainScreen.class);
                        prefsEditor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
                        try {
                            prefsEditor.putString("loggedInName", jsonObject.getString("first_name").toString());
                            prefsEditor.putString("FBUserID", jsonObject.getString("id").toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        prefsEditor.commit();
                        startActivity(mainIntent);
                        finish();

                    }
                });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,first_name,link");
                request.setParameters(parameters);
                request.executeAsync();



            }

            @Override
            public void onCancel() {
                // App code
                System.out.println("APP: Brugeren trykkede cancel!");
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
                System.out.println("APP: FB FEJL!");
                exception.printStackTrace();
            }
        });

        String loggedInName = prefs.getString("loggedInName", null);
        if (loggedInName != null) {
            /*
            loggedInName holder en værdi - hvilket betyder vi er logget ind mindst en gang før!
            Vi bygger en ny intention (naming android! WOW!) og går videre til main screen!
             */
            Intent mainIntent = new Intent(getApplicationContext(), MainScreen.class);
            startActivity(mainIntent);
            // vi afslutter den nuværende aktivitet efter vi har startet den nye!
            finish();

        } else {
            // håndter login!


        }


    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }
/*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            ImageView mImageView = (ImageView) findViewById(R.id.lilbigbro_logo);
            if(mImageView != null)
                mImageView.setImageBitmap(imageBitmap);

        }
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

}
