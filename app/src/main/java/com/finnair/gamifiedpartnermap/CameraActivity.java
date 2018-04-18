package com.finnair.gamifiedpartnermap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.support.annotation.NonNull;

import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class CameraActivity extends Activity {

    public boolean plane_catched = false;
    public static int rotaatio;

    // Setting up UI elements
    private TextureView previewTextureView;
    private TextureView.SurfaceTextureListener listener;
    private ImageView topArrow;
    private ImageView rightArrow;
    private ImageView bottomArrow;
    private ImageView leftArrow;
    private ImageView[] arrows;

    // Setting up camera related variables
    private CameraManager manager;
    private String cameraID;
    private CameraDevice device;
    private CameraDevice.StateCallback stateCallback;
    private CaptureRequest captureRequest;
    private CaptureRequest.Builder captureRequestBuilder;
    CameraCaptureSession session;
    CameraCaptureSession.CaptureCallback sessionCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);
        }
    };
    private Size previewSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // Setting up the arrows in the UI
        topArrow    = findViewById(R.id.top_arrow);
        rightArrow  = findViewById(R.id.right_arrow);
        bottomArrow = findViewById(R.id.bottom_arrow);
        leftArrow   = findViewById(R.id.left_arrow);
        arrows =  new ImageView[]{topArrow, rightArrow, bottomArrow, leftArrow};



        // Setting up the listener for the texture view
        listener = new TextureView.SurfaceTextureListener() {
            //int dir = -5;
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
                // When the surface is available, try opening the camera
                try {
                    //Find a rear-facing camera and try to open it.
                    findCamera(i, i1);
                    rotateImage(i, i1);
                    if (cameraID != null) {
                        openCamera(cameraID);

                    }
                } catch (CameraAccessException e) {
                    Toast.makeText(getApplicationContext(), "Camera not accessible", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

               /* for(ImageView i : arrows){
                    int alpha = (int) i.getImageAlpha();
                    if(alpha < 100) dir = 5;
                    else if (alpha > 250) dir = -5;
                    i.setImageAlpha(alpha + dir);

                }*/

                }

        };
        previewTextureView = findViewById(R.id.previewTextureView);
        previewTextureView.setSurfaceTextureListener(listener);

        Button catchplane = findViewById(R.id.catch_plane);
        catchplane.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {

                Intent intent = new Intent(CameraActivity.this, MainActivity.class);
                setResult(Activity.RESULT_OK, intent);
                finish();

            }
        });





        final Calculations calculations = new Calculations();

        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {


                hideTopArrow();
                hideRightArrow();
                //hideBottomArrow();
                //hideLeftArrow();
                showCatchButton();

                if(Calculations.azimuthangle == true && Calculations.rollangle == true && plane_catched == true){
                    timer.cancel();
                    timer.purge();

                }

                calculations.azimuthAngle();
                calculations.rollAngle();

            }
        },0,1);


    }

    @Override
    public void onPause(){
        super.onPause();
        device.close();
        device = null;
    }

    @Override
    public void onResume(){
        super.onResume();
        previewTextureView.setSurfaceTextureListener(listener);
    }






    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void findCamera(int width, int height) throws CameraAccessException {
        manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        for (String camID : manager.getCameraIdList()) {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(camID);
            // Find if the rear-facing camera
            if (characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
                cameraID = camID;
                StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    previewSize = getPreferredSize(map.getOutputSizes(SurfaceTexture.class), height, width);
                }else previewSize = getPreferredSize(map.getOutputSizes(SurfaceTexture.class), width, height);
                break;
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void openCamera(String cameraID) {
        try {
            // Prepare the state callback for use
            stateCallback = new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice cameraDevice) {
                    device = cameraDevice;
                    createCameraPreviewSession();
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice cameraDevice) {
                    device = null;
                    cameraDevice.close();
                }

                @Override
                public void onError(@NonNull CameraDevice cameraDevice, int i) {
                    device = null;
                    cameraDevice.close();
                }
            };

            // Opening the camera.
            // This will lead to the stateCallbacks onOpened method getting called,
            // that is, no more work required here
            manager.openCamera(cameraID, stateCallback, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Couldn't open the camera", Toast.LENGTH_SHORT).show();

        } catch (SecurityException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "No permission to open the camera", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Creates a preview session that outputs the camera image continuously
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void createCameraPreviewSession() {
        try {
            // Setting up the surface and the surface texture.
            SurfaceTexture surfaceTexture = previewTextureView.getSurfaceTexture();
            Surface previewSurface = new Surface(surfaceTexture);
            surfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());

            // Creating a capture request builder
            captureRequestBuilder = device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(previewSurface);


            device.createCaptureSession(Arrays.asList(previewSurface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    if (device == null) return;
                    try {
                        captureRequest = captureRequestBuilder.build();
                        session = cameraCaptureSession;
                        session.setRepeatingRequest(captureRequest, sessionCallback, null);

                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(getApplicationContext(), "Configuration failed", Toast.LENGTH_SHORT).show();
                }
            }, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Camera not accessible", Toast.LENGTH_SHORT).show();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private Size getPreferredSize(Size[] sizes, int width, int height) {
        List<Size> collectorSizes = new ArrayList<>();
        for (Size size : sizes) {
            if (width < height) {
                if (size.getWidth() > width && size.getHeight() > height) collectorSizes.add(size);
            } else {
                if (size.getWidth() < height && size.getHeight() < width) {
                    collectorSizes.add(size);
                }
            }
        }
        if (collectorSizes.size() > 0) {
            return Collections.min(collectorSizes, new Comparator<Size>() {
                @Override
                public int compare(Size size, Size t1) {
                    return Long.signum(size.getWidth() * size.getHeight() - t1.getWidth() * t1.getHeight());
                }
            });
        } else return sizes[0];
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void rotateImage(int width, int height) {
        if (previewSize == null || previewTextureView == null) {
            return;
        }
        Matrix matrix = new Matrix();
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        rotaatio = rotation;
        RectF textureRectF = new RectF(0, 0, width, height);
        RectF previewRectF = new RectF(0, 0, previewSize.getHeight(), previewSize.getWidth());
        float centerX = textureRectF.centerX();
        float centery = textureRectF.centerY();


        if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) {
            previewRectF.offset(centerX - previewRectF.centerX(), centery - previewRectF.centerY());
            matrix.setRectToRect(textureRectF, previewRectF, Matrix.ScaleToFit.FILL);
            float scale = Math.max((float) width / previewSize.getWidth(), (float) height / previewSize.getHeight());

            matrix.postScale(scale, scale, centerX, centery);
            matrix.postRotate(90 * (rotation - 2), centerX, centery);
            previewTextureView.setTransform(matrix);

        }
    }


    void hideTopArrow(){

        final ImageView image = (findViewById(R.id.top_arrow));
        final ImageView image2 = (findViewById(R.id.bottom_arrow));

        if (Calculations.rollangle == true){


            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    image.setVisibility(View.INVISIBLE);
                    image2.setVisibility(View.INVISIBLE);

                }

            });

        }


        if(Calculations.showtoparrow == false && Calculations.showbottomrow == true && Calculations.rollangle == false) {

            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    image.setVisibility(View.INVISIBLE);
                    image2.setVisibility(View.VISIBLE);

                }

            });

        }

        if(Calculations.showtoparrow == true && Calculations.showbottomrow == false && Calculations.rollangle == false) {

            runOnUiThread(new Runnable() {

                @Override
                public void run() {

                    image.setVisibility(View.VISIBLE);
                    image2.setVisibility(View.INVISIBLE);

                }

            });

        }


    }


    void hideRightArrow(){

        final ImageView image = (findViewById(R.id.right_arrow));
        final ImageView image2 = (findViewById(R.id.left_arrow));

        if (Calculations.azimuthangle == true){

            runOnUiThread(new Runnable() {

                @Override
                public void run() {

                    image.setVisibility(View.INVISIBLE);
                    image2.setVisibility(View.INVISIBLE);

                }

            });

        }
        if(Calculations.showrightarrow == true && Calculations.showleftarrow == false && Calculations.azimuthangle == false){

            runOnUiThread(new Runnable() {

                @Override
                public void run() {

                    image.setVisibility(View.VISIBLE);
                    image2.setVisibility(View.INVISIBLE);

                }

            });

        }

        if(Calculations.showrightarrow == false && Calculations.showleftarrow == true && Calculations.azimuthangle == false){

            runOnUiThread(new Runnable() {

                @Override
                public void run() {

                    image.setVisibility(View.INVISIBLE);
                    image2.setVisibility(View.VISIBLE);

                }

            });

        }

    }




    void hideBottomArrow() {


        final ImageView image = (findViewById(R.id.bottom_arrow));
        final ImageView image2 = (findViewById(R.id.top_arrow));
        if (Calculations.rollangle == true) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    image.setVisibility(View.INVISIBLE);
                }
            });



        }

        if(image.getVisibility() == View.VISIBLE){

            runOnUiThread(new Runnable() {

                @Override
                public void run() {

                    image2.setVisibility(View.INVISIBLE);

                }

            });

        }
        if(image.getVisibility() == View.INVISIBLE && Calculations.rollangle != true){

            runOnUiThread(new Runnable() {

                @Override
                public void run() {

                    image2.setVisibility(View.VISIBLE);

                }

            });

        }

    }

    void hideLeftArrow() {


        final ImageView image2 = (findViewById(R.id.left_arrow));
        if (Calculations.azimuthangle == true && Calculations.showleftarrow == false) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    image2.setVisibility(View.INVISIBLE);
                }
            });



        }

        else{

            runOnUiThread(new Runnable() {

                @Override
                public void run() {

                    image2.setVisibility(View.VISIBLE);

                }

            });

        }

    }

    public void showCatchButton(){

        final Button catch_plane = (findViewById(R.id.catch_plane));

        if (Calculations.azimuthangle == true && Calculations.rollangle == true){

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    catch_plane.setVisibility(View.VISIBLE);
                }
            });



        }

        else{

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    catch_plane.setVisibility(View.GONE);

                }
            });
        }

    }

}
