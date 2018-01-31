package com.finnair.gamifiedpartnermap;

import android.app.Activity;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends Activity implements TokenResponseHandler, ProfileResponseHandler{
    private String CLIENT_ID = "aalto-0Cs";
    private String CLIENT_SECRET = "sbsiwXWiXjM";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Log.i("Authorization", "LoginActivity created");

        // Fetching the response and/or exception from intent redirected from RedirectUriReceiverActivity
        AuthorizationResponse resp = AuthorizationResponse.fromIntent(getIntent());
        AuthorizationException exp = AuthorizationException.fromIntent(getIntent());

        if (resp != null) {
            Toast.makeText(this, "Authentication successful", Toast.LENGTH_SHORT).show();
            TokenRequest tokenRequest = new TokenRequest();
            tokenRequest.handler = this;
           String url = String.format("https://preauth.finnair.com/cas/oauth2.0/accessToken?client_id=%s&redirect_uri=%s&code=%s&client_secret=%s", CLIENT_ID, "https%3A%2F%2Fdatademo-2a85f.firebaseapp.com%2Fauth%2Ffinnair%2Flogin",resp.authorizationCode,CLIENT_SECRET);
            try {
               tokenRequest.execute(new URL(url));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

        }
        else{
            //Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show();
            Intent i = new Intent("com.finnair.gamifiedpartnermap.AUTHORIZATION_FAILED");
            startActivity(i);
        }

    }


    @Override
    public void onTokenResponseAcquired(String tokenResponse) {
        Log.i("onTokenResponse: ", tokenResponse);
        ProfileRequest profileRequest = new ProfileRequest();
        profileRequest.handler = this;
        String profileUrl = String.format("https://preauth.finnair.com/cas/oauth2.0/profile?access_token=%s", tokenResponse);
        try {
            profileRequest.execute(new URL(profileUrl));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }

    /**
     * Gets called after ProfileRequest has finished
     * @param profileResponse
     * @
     */
    @Override
    public void onProfileResponseAcquired(String profileResponse) {
        Log.i("ProfileResponse", profileResponse);
        HashMap<String, String> profileInformation = new HashMap<String, String>();
        try {
            JSONObject profileJSON = new JSONObject(profileResponse);
            Log.i("Profile ID", profileJSON.getString("id"));

            profileInformation.put("id", profileJSON.getString("id"));

            // Go back to MainActivity and include the profile information in intent extras
            Intent intent = new Intent("com.finnair.gamifiedpartnermap.PROFILE_REQUEST_SUCCESSFUL");
            intent.putExtra("profileInformation", profileInformation);
            startActivity(intent);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
