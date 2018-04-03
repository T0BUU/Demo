package com.finnair.gamifiedpartnermap;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import java.util.Arrays;

public class CameraActivity extends AppCompatActivity {

    private TextureView previewTextureView;
    private TextureView.SurfaceTextureListener listener;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // Setting up the listener for the texture view
        listener = new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
                // When the surface is available, try opening the camera
                try{
                    //Find a rear-facing camera and try to open it.
                   findCamera();
                   if(cameraID != null){
                       openCamera(cameraID);
                   }
                }catch(CameraAccessException e){
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

            }
        };
        previewTextureView = findViewById(R.id.previewTextureView);
        previewTextureView.setSurfaceTextureListener(listener);


    }

    private void findCamera() throws CameraAccessException {
        manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        for(String camID : manager.getCameraIdList()){
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(camID);
            // Find if the rear-facing camera
            if(characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK){
                cameraID = camID;
                break;
            }
        }
    }

    private void openCamera(String cameraID) {
        try{
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

        }catch (CameraAccessException e){
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Couldn't open the camera", Toast.LENGTH_SHORT).show();

        } catch(SecurityException e){
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "No permission to open the camera", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Creates a preview session that outputs the camera image continuously
     */
    private void createCameraPreviewSession() {
        try{
            // Setting up the surface and the surface texture.
            SurfaceTexture surfaceTexture = previewTextureView.getSurfaceTexture();
            Surface previewSurface = new Surface(surfaceTexture);

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

        }catch (CameraAccessException e){
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Camera not accessible", Toast.LENGTH_SHORT).show();
        }

    }
}
