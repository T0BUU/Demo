package com.finnair.gamifiedpartnermap;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;

/**
 * Created by noctuaPC on 19.2.2018.
 */

public class SensorActivity extends CardSelectionActivity implements SensorEventListener {
    /**
     TODO:
     WARNING! Always remember to disable sensors when finished.
     They will eat the battery withing hours.

     TODO:
     Additional note. You must lock screen rotation because rotation and sensor data can
     interact in unexpected ways. Use:
     setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

     TODO:
     onSensorChanged() runs on main thread and gets called frequently
     Rewrite onSensorChanged() to use an AsyncTask object for all the calculations and updates to views.

     Good source: https://github.com/google-developer-training/android-advanced/blob/master/TiltSpot/app/src/main/java/com/example/android/tiltspot/MainActivity.java
     https://google-developer-training.gitbooks.io/android-developer-advanced-course-practicals/unit-1-expand-the-user-experience/lesson-3-sensors/3-2-p-working-with-sensor-based-orientation/3-2-p-working-with-sensor-based-orientation.html

     TODO:
     Just for the future, an easy way to show which way user is facing
     googleMap.addMarker(new MarkerOptions()
     .position(myLatLng)
     .title(myTitle)
     .snippet(mySnippet)
     .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_arrow_up))
     .rotation(myAngle));



     // Add this to MainActivity.onCreate() ///////////////////////////////////////////////////////
     SensorManager sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

     double abraLat = 60.165095;
     double abraLng = 24.928022;

     double torniLat = 60.169353;
     double torniLng = 24.938749;

     Location abraLoc = new Location("");
     abraLoc.setLatitude(abraLat);
     abraLoc.setLongitude(abraLng);

     Location torniLoc = new Location("");
     torniLoc.setLatitude(torniLat);
     torniLoc.setLongitude(torniLng);

     double angle = abraLoc.bearingTo(torniLoc);

     testTextView = findViewById(R.id.testTextView);
     sensorActivity = new SensorActivity(sensorManager, testTextView, angle);
     sensorActivity.registerListeners();


     Add these to activity_main.xml under <!-- Content -->
     <TextView
     android:id="@+id/testTextView"
     android:layout_width="match_parent"
     android:layout_height="wrap_content"
     android:text="TextView" />

     <TextView
     android:id="@+id/testTextView2"
     android:layout_width="match_parent"
     android:layout_height="wrap_content"
     android:text="TextView" />



     // New shit ends ///////////////////////////////////////////////////////////////////////////


     */

    public static SensorManager sensorManager;
    public static SensorEvent sensorEvent;
    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];

    private final float[] rotationMatrix = new float[9];
    public final float[] orientationAngles = new float[3];
    public static float azimuth;
    public float pitch;
    public float roll;
    private double azimuthToTarget;
    private double pitchToTarget;
    private double rollToTarget;


    public SensorActivity(SensorManager sensorManager, double azimuthToTarget, double pitchToTarget, double rollToTarget){
        this.sensorManager = sensorManager;
        this.azimuthToTarget = azimuthToTarget;
        this.pitchToTarget = pitchToTarget;
        this.rollToTarget = rollToTarget;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accurarcy){

    }

    public void registerListeners(){
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL);
    }

    public float getAzimuth(){return this.azimuth;}
    public float getPitch(){return this.pitch;}
    public float getRoll(){return this.roll;}

    @Override
    public void onSensorChanged(SensorEvent event){
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.length);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading,0, magnetometerReading.length);
        }

        updateOrientationAngles();


    }



    // Compute the three orientation angles based on the most recent readings from
    // the device's accelerometer and magnetometer.
    public void updateOrientationAngles() {
        // Update rotation matrix, which is needed to update orientation angles.
        sensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading);
        sensorManager.getOrientation(rotationMatrix, orientationAngles);
        this.azimuth = orientationAngles[0];
        this.pitch = orientationAngles[1];
        this.roll = orientationAngles[2];

        final Calculations calculations = new Calculations();
        calculations.deviceAzimuth = Math.toDegrees(getAzimuth()); // Update angles to calculations
        calculations.devicePitch = Math.toDegrees(getPitch());
        calculations.deviceRoll = Math.toDegrees(getRoll());
        calculations.azimuthAngle();
        calculations.rollAngle();


    }



}
