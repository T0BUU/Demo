package com.finnair.gamifiedpartnermap;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by huzla on 11.1.2018.
 */

public class GeofenceTransitionsIntentService extends IntentService {

    private static final String TAG = GeofenceTransitionsIntentService.class.getSimpleName();


    public GeofenceTransitionsIntentService() {
        super(TAG);
        Log.d(TAG, "Created");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);

        if (event.hasError()) {

        } else {

            int transition = event.getGeofenceTransition();
            Geofence g = event.getTriggeringGeofences().get(0);
            String requestId = g.getRequestId();

            if (transition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                Log.d(TAG, "Entered!");
            }
            else {
                Log.d(TAG, "Left!");
            }
        }
    }
}
