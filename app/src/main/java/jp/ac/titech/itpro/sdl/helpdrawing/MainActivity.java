package jp.ac.titech.itpro.sdl.helpdrawing;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

@SuppressWarnings("deprecation")
public class MainActivity extends Activity implements TextureView.SurfaceTextureListener, ViewTreeObserver.OnGlobalLayoutListener {
    private static final int PERMISSION_REQUEST = 12345;

    private int cameraId = -1;
    private Camera.CameraInfo cameraInfo = null;
    private Camera camera = null;
    private SurfaceTexture cameraTexture = null;
    private boolean requestingPermission = false;

    private TextureView cameraTextureView;
    private RelativeLayout mainLayout;
    private int layoutPrevX = -1, layoutPrevY = -1;

    private void openCamera() {
        // 権限が必要で、かつ無い場合要求する
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getApplicationContext().checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                if (requestingPermission) {
                    Toast.makeText(getApplicationContext(), R.string.camera_no_permission, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    requestingPermission = true;
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST);
                }
                return;
            }
        }
        requestingPermission = false;
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
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST) openCamera();
    }

    private void closeCamera() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    private void setCameraOrientation() {
        if (camera == null) return;
        // 画面の向きとカメラの向きを合わせる
        int rotationDegrees = 0;
        switch (getWindowManager().getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_0: rotationDegrees = 0; break;
            case Surface.ROTATION_90: rotationDegrees = 90; break;
            case Surface.ROTATION_180: rotationDegrees = 180; break;
            case Surface.ROTATION_270: rotationDegrees = 270; break;
        }
        int cameraRotation = (cameraInfo.orientation - rotationDegrees + 360) % 360;
        camera.setDisplayOrientation(cameraRotation);
        // 画面の大きさを適切にセットする
        boolean isRotate = (cameraRotation == 90 || cameraRotation == 270);
        Camera.Size previewSize = camera.getParameters().getPreviewSize();
        int width = isRotate ? previewSize.height : previewSize.width;
        int height = isRotate ? previewSize.width : previewSize.height;
        int viewWidth = mainLayout.getWidth();
        int viewHeight = mainLayout.getHeight();
        ViewGroup.LayoutParams lp = cameraTextureView.getLayoutParams();
        // カメラの縦が画面の縦より大きい場合
        // height / width >= viewHeight / viewWidth
        if ((long)height * viewWidth >= (long)viewHeight * width) {
            lp.width = viewWidth;
            lp.height = (int)((long)viewWidth * height / width);
        } else {
            lp.width = (int)((long)viewHeight * width / height);
            lp.height = viewHeight;
        }
        cameraTextureView.requestLayout();
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
        mainLayout = (RelativeLayout) findViewById(R.id.mainLayout);

        cameraTextureView.setSurfaceTextureListener(this);
        cameraTextureView.getViewTreeObserver().addOnGlobalLayoutListener(this);
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
    public void onGlobalLayout() {
        int currentX = mainLayout.getWidth();
        int currentY = mainLayout.getHeight();
        if (currentX != layoutPrevX || currentY != layoutPrevY) {
            layoutPrevX = currentX;
            layoutPrevY = currentY;
            setCameraOrientation();
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        cameraTexture = surface;
        try {
            if (camera != null) {
                camera.setPreviewTexture(surface);
                setCameraOrientation();
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
