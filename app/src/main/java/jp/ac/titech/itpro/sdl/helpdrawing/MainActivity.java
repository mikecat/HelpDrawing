package jp.ac.titech.itpro.sdl.helpdrawing;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.IOException;

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
    private FigureView mainFigureView;
    private int layoutPrevX = -1, layoutPrevY = -1;
    private int prevRotation = 0;

    private boolean isDragging = false;
    private float prevDragX = -1, prevDragY = -1;
    private int dragTarget = -1;

    private int[] figureX = null;
    private int[] figureY = null;
    private int figureRadius = -1;

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

    private int getRotationDegrees() {
        int rotationDegrees = 0;
        switch (getWindowManager().getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_0: rotationDegrees = 0; break;
            case Surface.ROTATION_90: rotationDegrees = 90; break;
            case Surface.ROTATION_180: rotationDegrees = 180; break;
            case Surface.ROTATION_270: rotationDegrees = 270; break;
        }
        return rotationDegrees;
    }

    private void setCameraOrientation() {
        if (camera == null) return;
        // 画面の向きとカメラの向きを合わせる
        int rotationDegrees = getRotationDegrees();
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
        mainFigureView = (FigureView)findViewById(R.id.mainFigureView);

        cameraTextureView.setSurfaceTextureListener(this);
        cameraTextureView.getViewTreeObserver().addOnGlobalLayoutListener(this);

        prevRotation = getRotationDegrees();
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
    public boolean onTouchEvent(MotionEvent event) {
        Rect rect = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        int action = event.getAction();
        float x = event.getX() - rect.left;
        float y = event.getY() - rect.top;
        if (action == MotionEvent.ACTION_DOWN) {
            for (int i = 0; i < 4; i++) {
                if ((figureX[i] - x) * (figureX[i] - x) + (figureY[i] - y) * (figureY[i] - y) <= figureRadius * figureRadius) {
                    isDragging = true;
                    dragTarget = i;
                    prevDragX = x;
                    prevDragY = y;
                    break;
                }
            }
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
            if (isDragging) {
                figureX[dragTarget] += (int)(x - prevDragX);
                figureY[dragTarget] += (int)(y - prevDragY);
                mainFigureView.setCoord(figureX[dragTarget], figureY[dragTarget], dragTarget);
                mainFigureView.invalidate();
            }
            isDragging = false;
        } else if (action == MotionEvent.ACTION_MOVE) {
            if (isDragging) {
                mainFigureView.setCoord(figureX[dragTarget] + (int) (x - prevDragX),
                        figureY[dragTarget] + (int) (y - prevDragY), dragTarget);
                mainFigureView.invalidate();
            }
        }
        return true;
    }

    @Override
    public void onGlobalLayout() {
        int currentX = mainLayout.getWidth();
        int currentY = mainLayout.getHeight();
        int currentRotation = getRotationDegrees();
        if (currentX != layoutPrevX || currentY != layoutPrevY || currentRotation != prevRotation) {
            if (figureX == null) {
                if (figureRadius < 0) {
                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                    figureRadius = (int)(1.5 / 25.4 * displayMetrics.densityDpi);
                    mainFigureView.setRadius(figureRadius);
                }
                figureX = new int[4];
                figureY = new int[4];
                for (int i = 0; i < 4; i++) {
                    figureX[i] = (int) ((long) currentX * (i + 1) / 5);
                    figureY[i] = (int) ((long) currentY * (i + 1) / 5);
                    mainFigureView.setCoord(figureX[i], figureY[i], i);
                }
                mainFigureView.invalidate();
            } else if (currentRotation != prevRotation) {
                int diff = (currentRotation - prevRotation + 360) % 360;
                isDragging = false;
                android.util.Log.d("touchaaaa", "diff = " + diff);
                if (diff == 180) {
                    for (int i = 0; i < 4; i++) {
                        figureX[i] = currentX - figureX[i];
                        figureY[i] = currentY - figureY[i];
                    }
                } else if (diff == 90) {
                    for (int i = 0; i < 4; i++) {
                        int xBackup = figureX[i];
                        figureX[i] = figureY[i];
                        figureY[i] = currentY - xBackup;
                    }
                } else if (diff == 270) {
                    for (int i = 0; i < 4; i++) {
                        int xBackup = figureX[i];
                        figureX[i] = layoutPrevY - figureY[i];
                        figureY[i] = xBackup;
                    }
                }
                for (int i = 0; i < 4; i++) {
                    mainFigureView.setCoord(figureX[i], figureY[i], i);
                }
                mainFigureView.invalidate();
                prevRotation = currentRotation;
            }
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
