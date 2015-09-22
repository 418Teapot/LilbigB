package com.teapot.lilbigb;

import android.os.AsyncTask;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class UpdatePositionTask extends AsyncTask<String, String, String> {

    @Override
    protected String doInBackground(String... params) {
        String ret = "ERROR";
        try {
            URL updateUrl = new URL("http://46.101.251.99/lilbigbro/updatePosition");
            HttpURLConnection conn = (HttpURLConnection) updateUrl.openConnection();
            conn.setRequestMethod("POST");
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);

            List<NameValuePair> parameters = new ArrayList<NameValuePair>();
            parameters.add(new BasicNameValuePair("username", params[0]));
            parameters.add(new BasicNameValuePair("location", params[1]));

            System.out.println(params[0]+", Location: "+params[1]);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(getQuery(parameters));
            writer.flush();
            writer.close();
            os.close();

            conn.connect();

            ret = ""+conn.getResponseCode();



        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return ret;
    }

    @Override
    protected void onPostExecute(String result){
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
}