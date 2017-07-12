package jp.ac.titech.itpro.sdl.helpdrawing;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import java.io.IOException;

@SuppressWarnings("deprecation")
public class MainActivity extends Activity implements TextureView.SurfaceTextureListener {
    private int cameraId = -1;
    private Camera.CameraInfo cameraInfo = null;
    private Camera camera = null;
    private TextureView cameraTextureView;
    private SurfaceTexture cameraTexture = null;

    private void openCamera() {
        // カメラが選択されていない場合、選択する
        if (cameraId < 0) {
            int cameraMax = Camera.getNumberOfCameras();
            cameraInfo = new Camera.CameraInfo();
            for (int i = 0; i < cameraMax; i++) {
                Camera.getCameraInfo(i, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    // backを向いている最初のカメラ
                    cameraId = i;
                    break;
                }
            }
            if (cameraId < 0) {
                // 有効なカメラが見つからない
                Toast.makeText(getApplicationContext(), R.string.camera_not_found, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        // カメラを開く
        camera = Camera.open(cameraId);
        if (cameraTexture != null) {
            try {
                camera.setPreviewTexture(cameraTexture);
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), R.string.camera_initialize_fail, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        setCameraOrientation();
        camera.startPreview();
        if (cameraTexture != null) startFocus();
    }

    private void closeCamera() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    private void setCameraOrientation() {
        // 画面の向きとカメラの向きを合わせる
        int rotationDegrees = 0;
        switch (getWindowManager().getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_0: rotationDegrees = 0; break;
            case Surface.ROTATION_90: rotationDegrees = 90; break;
            case Surface.ROTATION_180: rotationDegrees = 180; break;
            case Surface.ROTATION_270: rotationDegrees = 270; break;
        }
        camera.setDisplayOrientation((cameraInfo.orientation - rotationDegrees + 360) % 360);
    }

    private void startFocus() {
        camera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraTextureView = (TextureView)findViewById(R.id.cameraTextureView);
        cameraTextureView.setSurfaceTextureListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        openCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeCamera();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setCameraOrientation();
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        cameraTexture = surface;
        try {
            if (camera != null) {
                camera.setPreviewTexture(surface);
                startFocus();
            }
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), R.string.camera_initialize_fail, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        closeCamera();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }
}
