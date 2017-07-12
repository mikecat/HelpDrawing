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
    private Camera.CameraInfo cameraInfo;
    private Camera camera;
    private TextureView cameraTextureView;
    private boolean cameraOk = false;

    Camera openCamera() {
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
        Camera camera = Camera.open(cameraId);
        // 画面の向きとカメラの向きを合わせる
        setCameraOrientation(camera);
        return camera;
    }

    void setCameraOrientation(Camera camera) {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraTextureView = (TextureView)findViewById(R.id.cameraTextureView);
        cameraTextureView.setSurfaceTextureListener(this);
        camera = openCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (cameraOk) camera.startPreview();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cameraOk) camera.stopPreview();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setCameraOrientation(camera);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        try {
            camera.setPreviewTexture(surface);
            camera.startPreview();
            cameraOk = true;
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), R.string.camera_open_fail, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        camera.stopPreview();
        camera.release();
        cameraOk = false;
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }
}
