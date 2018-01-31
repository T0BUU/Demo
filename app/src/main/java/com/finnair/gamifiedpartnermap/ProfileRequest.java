package com.finnair.gamifiedpartnermap;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by axelv on 30.1.2018.
 */

public class ProfileRequest extends AsyncTask<URL, Void, String> {
    ProfileResponseHandler handler;

    @Override
    protected String doInBackground(URL... urls) {

        try {
            HttpsURLConnection conn = (HttpsURLConnection) urls[0].openConnection();
            InputStream stream = conn.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            StringBuilder b = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                b.append(line + "\n");
            }

            String profileResponse = b.toString();
            return profileResponse;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
      }

    @Override
    protected void onPostExecute(String s) {
        handler.onProfileResponseAcquired(s);
    }
}

