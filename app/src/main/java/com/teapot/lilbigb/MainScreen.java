package com.teapot.lilbigb;

import android.app.Activity;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioAttributes;
import android.media.Image;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.Vibrator;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/*
MainScreen er som navnet antyder hovedskærmen, der præsenterer brugeren for p.t. 3 valg
Kort - bruger Google Maps til at vise vores sidst kendte placering
Indstillinger - der endnu ikke er implementeret
Log ud - som lukker app'en og fjerner det gemte navn.
 */

public class MainScreen extends AppCompatActivity {

    public Bitmap userBitmap;
    //public String userName = "";
    private final static int REQUEST_ENABLE_BT = 1; //because maybe some day someone would need it to be !=1 ?!

    private static ArrayAdapter<String> mArrayAdapter; //to store the discovered bluetooth devices
    private ListAdapter btAdapter;

    private static ListView listView;
    private static ArrayList<String> deviceList = new ArrayList<String>();
    IntentFilter bluetoothFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
    IntentFilter bluetoothFilterEnd = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
    private BluetoothAdapter mBluetoothAdapter;
    private WifiManager wifiManager;
    private List<ScanResult> wifiNetworks;

    private ListView wifiListView;
    private ArrayList<String> wifiList = new ArrayList<String>();
    private ArrayAdapter<String> wifiListAdapter;

    private static final int RESULT_PICK_CONTACT = 0;
    private static final int NOTIFICATION_ID = 16;

    private Cursor mCursor;

    private ArrayList<String> btAddr = new ArrayList<String>();
    private static ArrayList<String> devNrs = new ArrayList<String>();

    private static int selectedPos;
    private String selectedBtAddr = null;
    private String selectedBtName = null;
    private String selectedCtctName = null;
    private String selectedCtctNumber = null;

    private static boolean notifyOnKnownDevice = false;

    private static String loginName;

    private SharedPreferences prefs;
    public static final String PREFS_NAME = "lilBIGbroPrefs";

    private static Context ctx;

    public static int getSelectedPos(){
        return selectedPos;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        //WIFI:
        ctx = getApplicationContext();
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if(!wifiManager.isWifiEnabled()){
            wifiManager.setWifiEnabled(true);
            System.out.println("Enabling WIFI!");
        }

        wifiListView = (ListView) findViewById(R.id.foundWlan);

        wifiNetworks = wifiManager.getScanResults();
        for(ScanResult nw : wifiNetworks){
            System.out.println("WIFI: " + nw.SSID + " - " + nw.BSSID + " - " + nw.capabilities);
          //  wifiList.add(hm);
            wifiList.add(nw.SSID+" - "+nw.BSSID);
            wifiListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, wifiList);
        }

        wifiListView.setAdapter(wifiListAdapter);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        notifyOnKnownDevice = prefs.getBoolean("notifyDevice", false);

        System.out.println("WIFILIST: "+wifiList.toString());

        //BLUETOOTH:
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            System.out.println("Device doesn't have bluetooth. Throw it out.");
        }
        else if (!mBluetoothAdapter.isEnabled()) {
            System.out.println("BT off, turning on...");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
            /*MAKE DISCOVERABLE for 3600 seconds*/
            //Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            //discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 3600); //dirty, hackish approximation to "always"
            //startActivity(discoverableIntent);
            //1System.out.println("This device is now discoverable via bluetooth. Be carefull..");

        listView = (ListView) findViewById(R.id.foundBTItems);
        mArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, deviceList);

        SharedPreferences prefs = getSharedPreferences(loginActivity.PREFS_NAME, MODE_PRIVATE);
        final String loggedInName = prefs.getString("loggedInName", null);
        loginName = loggedInName;
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> arg0,
                                    View arg1, int position, long arg3) {
                Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                startActivityForResult(contactPickerIntent, RESULT_PICK_CONTACT);
                Toast.makeText(getApplicationContext(), "Vælg en kontakt der skal associeres til enheden", Toast.LENGTH_LONG).show();

                selectedPos = position;
                selectedBtAddr = btAddr.get(position);
                selectedBtName = (String) listView.getItemAtPosition(position);

            }


        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String name = (String) listView.getItemAtPosition(position);
                if (name.contains(" (")) {
                    // vi antar' så at vi har et nummer!
                    System.out.println("SMS TIL DEV!");

                    if (!devNrs.get(position).equals("null")) {
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(devNrs.get(position), null, "Hi from " + loggedInName + " LilBigBro is watching you!", null, null);
                        Toast.makeText(getApplicationContext(), "Sending SMS", Toast.LENGTH_LONG).show();
                    }
                }

                return true;
            }
        });



        registerReceiver(mReceiver, bluetoothFilter); // Don't forget to unregister during onDestroy
        registerReceiver(mReceiver, bluetoothFilterEnd);

        if(mBluetoothAdapter.startDiscovery()) System.out.println("Bluetooth discovery started...");



        FacebookSdk.sdkInitialize(getApplicationContext());
        // hent navnet fra shared prefs!

        String fbUserID = prefs.getString("FBUserID", null);
        final String userName = loggedInName;
        // setup actionbar
        ActionBar mActionBar = getSupportActionBar();
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
                                            params.add(new BasicNameValuePair("username", userName));
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

                                                } else {

                                                    return "ERROR "+conn.getResponseCode();
                                                }

                                             // since all of this is good post og registrer dig selv på serveren!
                                                URL regUrl = new URL("http://46.101.251.99/lilbigbro/register");
                                                conn = (HttpURLConnection) regUrl.openConnection();
                                                conn.setReadTimeout(10000);
                                                conn.setConnectTimeout(15000);
                                                conn.setRequestMethod("POST");
                                                conn.setDoOutput(true);
                                                conn.setDoInput(true);

                                                params = new ArrayList<NameValuePair>();
                                                params.add(new BasicNameValuePair("username", userName));
                                                params.add(new BasicNameValuePair("imageurl", imageFilePath));

                                                os = conn.getOutputStream();
                                                writer = new BufferedWriter(
                                                        new OutputStreamWriter(os, "UTF-8"));
                                                writer.write(getQuery(params));
                                                writer.flush();
                                                writer.close();
                                                os.close();

                                                conn.connect();

                                                String ret = "";
                                                if(conn.getResponseCode() == 200) {
                                                    BufferedReader in = new BufferedReader(new InputStreamReader(
                                                            conn.getInputStream()));
                                                    String inputLine;
                                                    while ((inputLine = in.readLine()) != null)
                                                        System.out.println(inputLine);
                                                        ret = inputLine;
                                                    in.close();
                                                    conn.disconnect();

                                                }

                                                return ret;

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
                    //Toast toast = Toast.makeText(getApplicationContext(), "Indstillinger er endnu ikke implementeret", Toast.LENGTH_LONG);
                    //toast.show();
                    Intent notificationActivity = new Intent(getApplicationContext(), NotificationsActivity.class);
                    startActivity(notificationActivity);
                }
            });

            logoutBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences.Editor prefsEditor = getSharedPreferences(loginActivity.PREFS_NAME, MODE_PRIVATE).edit();
                    prefsEditor.remove("loggedInName");
                    prefsEditor.commit();

                    LoginManager.getInstance().logOut();

                    // slet brugeren fra serveren
                    new AsyncTask<String, String, String>() {


                        @Override
                        protected String doInBackground(String... params) {
                            String ret = "ERROR";
                            try {
                                URL delURL = new URL("http://46.101.251.99/lilbigbro/removeUser");
                                HttpURLConnection conn = (HttpURLConnection) delURL.openConnection();
                                conn.setRequestMethod("POST");
                                conn.setDoInput(true);

                                List<NameValuePair> paramsLogout = new ArrayList<NameValuePair>();
                                paramsLogout.add(new BasicNameValuePair("username", loggedInName));

                                OutputStream os = conn.getOutputStream();
                                BufferedWriter writer = new BufferedWriter(
                                        new OutputStreamWriter(os, "UTF-8"));
                                writer.write(getQuery(paramsLogout));
                                writer.flush();
                                writer.close();
                                os.close();

                                conn.connect();

                                if(conn.getResponseCode() == 200) {
                                    BufferedReader in = new BufferedReader(new InputStreamReader(
                                            conn.getInputStream()));
                                    String inputLine;
                                    while ((inputLine = in.readLine()) != null)
                                        System.out.println(inputLine);
                                    ret = inputLine;
                                    in.close();
                                    conn.disconnect();

                                } else {
                                    ret = "ERROR "+conn.getResponseCode();
                                }

                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            return ret;
                        }

                        @Override
                        protected void onPostExecute(String result) {
                            System.out.println("RES: "+result);
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

                    }.execute(loggedInName);


                    Toast toast = Toast.makeText(getApplicationContext(), "Brugeren er logget ud og app'en lukkes!", Toast.LENGTH_LONG);
                    toast.show();
                    finish();
                }
            });

        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //unregisterReceiver(mReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, bluetoothFilter);
        registerReceiver(mReceiver, bluetoothFilterEnd);
    }


    private Uri uriContact;

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                System.out.println("BT ENABLED!");
                registerReceiver(mReceiver, bluetoothFilter); // Don't forget to unregister during onDestroy
                registerReceiver(mReceiver, bluetoothFilterEnd);

                if(mBluetoothAdapter.startDiscovery()) System.out.println("Bluetooth discovery started...");
            } else {
                System.out.println("BT ERROR!");
            }
        }

        if(requestCode == RESULT_PICK_CONTACT) {
            if(resultCode == RESULT_OK){
                System.out.println("Contact selected");
                System.out.println("Contact data: "+data);
                uriContact = data.getData();

                // hent contact name og evt. nummer

                String contactName = null;
                String hasNumber = null;
                String phoneNumer = null;
                // querying contact data store
                Cursor cursor = getContentResolver().query(uriContact, null, null, null, null);
                if (cursor.moveToFirst()) {
                    // DISPLAY_NAME = The display name for the contact.
                    // HAS_PHONE_NUMBER =   An indicator of whether this contact has at least one phone number.
                    contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    phoneNumer = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    hasNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                }
                cursor.close();
                System.out.println("Contact Name: " + contactName + " number: " + phoneNumer);

                // gem kontakten på serveren associeret med btdevice id

                new AsyncTask<String,String,String>() {

                    @Override
                    protected String doInBackground(String... params) {
                        String ret = "ERROR";

                        String contactName = params[0];
                        String contactNumber = params[1];
                        String devId = params[2];
                        String devName = params[3];

                        try {
                            URL delURL = new URL("http://46.101.251.99/lilbigbro/regBTdev");
                            HttpURLConnection conn = (HttpURLConnection) delURL.openConnection();
                            conn.setRequestMethod("POST");
                            conn.setDoInput(true);

                            List<NameValuePair> paramsReg = new ArrayList<NameValuePair>();
                            paramsReg.add(new BasicNameValuePair("contactName", contactName));
                            paramsReg.add(new BasicNameValuePair("contactNumber", contactNumber));
                            paramsReg.add(new BasicNameValuePair("devID", devId));
                            paramsReg.add(new BasicNameValuePair("devName", devName));
                            paramsReg.add(new BasicNameValuePair("userName", loginName));


                            OutputStream os = conn.getOutputStream();
                            BufferedWriter writer = new BufferedWriter(
                                    new OutputStreamWriter(os, "UTF-8"));
                            writer.write(getQuery(paramsReg));
                            writer.flush();
                            writer.close();
                            os.close();

                            conn.connect();

                            if(conn.getResponseCode() == 200) {
                                BufferedReader in = new BufferedReader(new InputStreamReader(
                                        conn.getInputStream()));
                                String inputLine;
                                while ((inputLine = in.readLine()) != null)
                                    System.out.println(inputLine);
                                ret = inputLine;
                                in.close();
                                conn.disconnect();

                            } else {
                                ret = "ERROR "+conn.getResponseCode();
                            }

                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        return ret;
                    }

                    @Override
                    protected void onPostExecute(String result) {
                        // opdatér listview med ny titel
                        System.out.println("RES: "+result);
                        new GetDevicesTask().execute(deviceList);
                        listView.setAdapter(new ArrayAdapter<String>(MainScreen.this, android.R.layout.simple_list_item_1, deviceList));
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
                }.execute(contactName, phoneNumer, selectedBtAddr, selectedBtName);



            }

        }
    }

    private void retrieveContactName() {

        String contactName = null;

        // querying contact data store
        Cursor cursor = getContentResolver().query(uriContact, null, null, null, null);

        if (cursor.moveToFirst()) {

            // DISPLAY_NAME = The display name for the contact.
            // HAS_PHONE_NUMBER =   An indicator of whether this contact has at least one phone number.

            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        }

        cursor.close();

        System.out.println("Contact Name: " + contactName);

    }


    public static void updateDeviceList(int pos, String device){
        deviceList.set(pos, device);
        mArrayAdapter.notifyDataSetChanged();

        // det her sker kun hvis vi kender device

            Vibrator v = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
            // Vibrate for 500 milliseconds
            v.vibrate(500);

            // crazy hack for at vise at vi kan lave en alarm
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(ctx)
                            .setSmallIcon(R.drawable.eye_icon_white)
                            .setContentTitle("LilBigBro")
                            .setContentText("Someone we know came close to us: "+deviceList.get(pos));
// Creates an explicit intent for an Activity in your app
            NotificationManager mNotificationManager =
                    (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());




    }

    public static void addNumber(int pos, String number){
        devNrs.set(pos, number);
    }

    public static boolean shouldINotify(){
        return notifyOnKnownDevice;
    }

    // Create a bluetooth BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
                //mArrayAdapter.add(device.getName() + "\n" + device.getAddress());

                // find ui!
                deviceList.add(device.getName());
                devNrs.add("null");
                btAddr.add(device.getAddress());

                //Toast toast = Toast.makeText(getApplicationContext(), "Bluetooth Discovered: " +device.getName() + "Adr: "+device.getAddress(), Toast.LENGTH_LONG);
                //toast.show();
                System.out.println("Bluetooth Discovered: " + device.getName());

                new GetDevicesTask().execute(deviceList);


                mArrayAdapter = new ArrayAdapter<String>(MainScreen.this, android.R.layout.simple_list_item_1, deviceList);
                listView.setAdapter(mArrayAdapter);



            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
            {
                System.out.println("Entered the Finished ");
                // dirty hack clear all devices
                deviceList = new ArrayList<String>();
                mBluetoothAdapter.startDiscovery();
            }
        }
    };

    public static String getLoggedInName(){
        return loginName;
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
