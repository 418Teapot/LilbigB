package com.teapot.lilbigb;

import android.os.AsyncTask;

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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class GetUsersTask extends AsyncTask<ArrayList<ArrayList<String>>, Void, ArrayList<ArrayList<String>>> {

    @Override
    protected ArrayList<ArrayList<String>> doInBackground(ArrayList<ArrayList<String>>... params) {

        ArrayList<ArrayList<String>> returnList = params[0];//new ArrayList<ArrayList<String>>();

        try {
            URL getUsers = new URL("http://46.101.251.99/lilbigbro/users");
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
                JSONArray jArray = jobj.getJSONArray("users");
                for(int i = 0; i < jArray.length(); i++){
                    //System.out.println("USER "+jArray.getJSONObject(i).getString("username"));
                    String uname = jArray.getJSONObject(i).getString("username");
                    String loc = jArray.getJSONObject(i).getString("location");
                    String imgpth = jArray.getJSONObject(i).getString("imageurl");

                    ArrayList<String> userList = new ArrayList<String>();
                    userList.add(uname); userList.add(loc); userList.add(imgpth);
                    returnList.add(userList);
                }

            }

            System.out.println("USER RES: "+conn.getResponseCode());


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }



        return returnList;
    }

    @Override
    public void onPostExecute(ArrayList<ArrayList<String>> list){
        MapActivity.serverUsers = list;
        MapActivity.buildUserList();
        MapActivity.addUserMarkers();
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