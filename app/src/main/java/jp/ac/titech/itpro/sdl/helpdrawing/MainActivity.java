package jp.ac.titech.itpro.sdl.helpdrawing;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.TextureView;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends Activity implements TextureView.SurfaceTextureListener {
    private Camera camera;
    private TextureView cameraTextureView;
    private boolean cameraOk = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraTextureView = (TextureView)findViewById(R.id.cameraTextureView);
        cameraTextureView.setSurfaceTextureListener(this);
        camera = Camera.open();
        camera.setDisplayOrientation(90);
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
