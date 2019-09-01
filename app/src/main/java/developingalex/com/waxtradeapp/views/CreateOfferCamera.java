package developingalex.com.waxtradeapp.views;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.util.Objects;

import developingalex.com.waxtradeapp.R;

public class CreateOfferCamera extends AppCompatActivity {

    private CameraSource cameraSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_offer_camera);

        final Toolbar toolbar = findViewById(R.id.offer_create_toolbar_camera);
        // Adds Back-Arrow to Toolbar
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        setTitle("Scan a QR-Code");

        final BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(CreateOfferCamera.this)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();

        final Display display = getWindowManager().getDefaultDisplay();
        final Point size = new Point();
        display.getSize(size);

        cameraSource = new CameraSource.Builder(CreateOfferCamera.this, barcodeDetector)
                .setRequestedPreviewSize(size.y , size.x)
                .setAutoFocusEnabled(true)
                .build();

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() { }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {

                final SparseArray<Barcode> qrCodes = detections.getDetectedItems();

                if (qrCodes.size() != 0) {

                    final String regex = "^(http://|https://)?(www.)?([a-zA-Z0-9]+).[a-zA-Z0-9]*.[a-z].?([a-z]+)?.(t).?([0-9])?.([0-9]){6}.([a-zA-Z0-9]){8}$";

                    if (qrCodes.valueAt(0).displayValue.matches((regex))) {
                        CreateOfferCamera.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Intent returnIntent = new Intent();
                                returnIntent.putExtra("tradeURL", qrCodes.valueAt(0).displayValue);
                                setResult(Activity.RESULT_OK, returnIntent);
                                finish();
                            }
                        });
                    } else {
                        CreateOfferCamera.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(CreateOfferCamera.this, "Please provide a valid URL", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

            }
        });

        final SurfaceView surfaceView = findViewById(R.id.camera_preview);

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (ActivityCompat.checkSelfPermission(CreateOfferCamera.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }

                try {
                    cameraSource.start(holder);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) { }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
