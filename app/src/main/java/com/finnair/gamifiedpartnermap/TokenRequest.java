package com.finnair.gamifiedpartnermap;

import android.app.Activity;
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

public class TokenRequest extends AsyncTask<URL, Void, String> {

    TokenResponseHandler handler = null;

    protected String doInBackground(URL... urls) {
        String[] arguments;
        try {
            HttpsURLConnection conn = (HttpsURLConnection) urls[0].openConnection();
            InputStream stream = conn.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            StringBuilder b = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                b.append(line + "\n");
            }
            String tokenResponse = b.toString();
            arguments = tokenResponse.split("&");
            String[] tokenPart = arguments[0].split("=");
            String accessToken = tokenPart[1];
            Log.i("Auth:", "Access token: " + accessToken);

            return accessToken;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    protected void onPostExecute(String s) {
       handler.onTokenResponseAcquired(s);
    }
}

