package com.teapot.lilbigb;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.widget.ListView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class GetDevicesTask extends AsyncTask<ArrayList<String>, String, ArrayList<String>> {

    String contact;// = jArray.getJSONObject(i).getString("contactName");
    String number; //= jArray.getJSONObject(i).getString("contactNumber");
    String devID;// = jArray.getJSONObject(i).getString("devID");
    String devName;// = jArray.getJSONObject(i).getString("devName");

    @Override
    protected ArrayList doInBackground(ArrayList<String>... params) {

        ArrayList<String> deviceList  = params[0];

        try {
            URL getUsers = new URL("http://46.101.251.99/lilbigbro/getBTdev?user="+MainScreen.getLoggedInName());
            HttpURLConnection conn = (HttpURLConnection) getUsers.openConnection();
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            if(conn.getResponseCode() == 200) {

                String jsonString = "";

                BufferedReader in = new BufferedReader(new InputStreamReader(
                        conn.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null)
                    jsonString += inputLine;
                in.close();
                conn.disconnect();

                JSONObject jobj = new JSONObject(jsonString);
                JSONArray jArray = jobj.getJSONArray("devices");
                for(int i = 0; i < jArray.length(); i++){
                    //System.out.println("USER "+jArray.getJSONObject(i).getString("username"));
                    contact = jArray.getJSONObject(i).getString("contactName");
                    number = jArray.getJSONObject(i).getString("contactNumber");
                    devID = jArray.getJSONObject(i).getString("devID");
                    devName = jArray.getJSONObject(i).getString("devName");
                }

            }

            System.out.println("RES: "+conn.getResponseCode());


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return deviceList;
    }

    @Override
    public void onPostExecute(ArrayList<String> list){

        int pos = 0;
        for(String dev : list){
            if(dev.equals(devName)) {
                System.out.println("DevList: "+dev);
                MainScreen.updateDeviceList(pos, devName + " (" + contact + ")");
                MainScreen.addNumber(pos, number);
            }
            pos++;
        }
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

}