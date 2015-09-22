package com.teapot.lilbigb;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;

import org.apache.http.HttpRequest;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/*
MainScreen er som navnet antyder hovedskærmen, der præsenterer brugeren for p.t. 3 valg
Kort - bruger Google Maps til at vise vores sidst kendte placering
Indstillinger - der endnu ikke er implementeret
Log ud - som lukker app'en og fjerner det gemte navn.
 */

public class MainScreen extends Activity {

    public Bitmap userBitmap;
    //public String userName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        FacebookSdk.sdkInitialize(getApplicationContext());
        // hent navnet fra shared prefs!
        SharedPreferences prefs = getSharedPreferences(loginActivity.PREFS_NAME, MODE_PRIVATE);
        String loggedInName = prefs.getString("loggedInName", null);
        String fbUserID = prefs.getString("FBUserID", null);
        final String userName = loggedInName;
        // setup actionbar
        ActionBar mActionBar = getActionBar();
        mActionBar.setDisplayShowHomeEnabled(false);
        mActionBar.setDisplayShowTitleEnabled(false);
        LayoutInflater mInflater = LayoutInflater.from(this);

        View mCustomView = mInflater.inflate(R.layout.menu_with_image_view, null);
        TextView mTitleTextView = (TextView) mCustomView.findViewById(R.id.title_text);
        mTitleTextView.setText(" " + loggedInName);

        final ImageView userImage = (ImageView) mCustomView.findViewById(R.id.userImage);

        if(fbUserID != null) {
            /* make the API call */

            Bundle params = new Bundle();
            params.putBoolean("redirect", false);

            new GraphRequest(
                    AccessToken.getCurrentAccessToken(),
                    "me/picture",
                    params,
                    HttpMethod.GET,
                    new GraphRequest.Callback() {
                        public void onCompleted(final GraphResponse response) {

                            try {
                                String picUrlString = (String) response.getJSONObject().getJSONObject("data").get("url");
                                System.out.println(picUrlString);

                                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                                StrictMode.setThreadPolicy(policy);

                                URL url = new URL(picUrlString);
                                Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                                userBitmap = bmp;
                                if(userImage != null) {
                                    userImage.setImageBitmap(bmp);

                                    // nu har vi et billede upload det

                                    new AsyncTask<ImageView, String, String>() {

                                        ImageView img;
                                        String imageFilePath;

                                        @Override
                                        protected String doInBackground(ImageView... imgView) {
                                            img = imgView[0];
                                            Bitmap userBitmap = ((BitmapDrawable)img.getDrawable()).getBitmap();

                                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                            userBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                            byte[] imageBytes = baos.toByteArray();
                                            String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);

                                            URL url = null;
                                            try {
                                                url = new URL("http://46.101.251.99/lilbigbro/uploadImage");

                                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                            conn.setReadTimeout(10000);
                                            conn.setConnectTimeout(15000);
                                            conn.setRequestMethod("POST");
                                            conn.setDoInput(true);
                                            conn.setDoOutput(true);

                                            List<NameValuePair> params = new ArrayList<NameValuePair>();
                                            params.add(new BasicNameValuePair("user", userName));
                                            params.add(new BasicNameValuePair("image", encodedImage));

                                            OutputStream os = conn.getOutputStream();
                                            BufferedWriter writer = new BufferedWriter(
                                                    new OutputStreamWriter(os, "UTF-8"));
                                            writer.write(getQuery(params));
                                            writer.flush();
                                            writer.close();
                                            os.close();

                                            conn.connect();

                                                if(conn.getResponseCode() == 200){
                                                    BufferedReader in = new BufferedReader(new InputStreamReader(
                                                            conn.getInputStream()));
                                                    String inputLine;
                                                    while ((inputLine = in.readLine()) != null)
                                                        imageFilePath = inputLine;
                                                    in.close();
                                                    conn.disconnect();

                                                }
                                            } catch (MalformedURLException e) {
                                                e.printStackTrace();
                                            } catch (ProtocolException e) {
                                                e.printStackTrace();
                                            } catch (UnsupportedEncodingException e) {
                                                e.printStackTrace();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }

                                            return "bg";
                                        }

                                        @Override
                                        protected void onPostExecute(String result) {
                                            System.out.println("RES: "+result);
                                            System.out.println(img.toString());
                                        }

                                        private String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException
                                        {
                                            StringBuilder result = new StringBuilder();
                                            boolean first = true;

                                            for (NameValuePair pair : params)
                                            {
                                                if (first)
                                                    first = false;
                                                else
                                                    result.append("&");

                                                result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
                                                result.append("=");
                                                result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
                                            }

                                            return result.toString();
                                        }
                                    }.execute(userImage);

                                } else {
                                    System.out.println("APP: User image is NULL!");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }


                        }
                    }
            ).executeAsync();
        }
/*
        if(userImage != null){
            String imgUrl = "http://graph.facebook.com/"+fbUserID+"/picture";
            userImage.setTag(imgUrl);
            new DownloadImageTask().execute(userImage);
        }
*/
        mActionBar.setCustomView(mCustomView);
        mActionBar.setDisplayShowCustomEnabled(true);



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
                    mapsActivity.putExtra("UserBitmap", userBitmap);
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

                    LoginManager.getInstance().logOut();

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
