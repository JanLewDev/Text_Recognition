package com.example.textrecognition;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;


import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private TextView textView;
    private SurfaceView cameraView;
    private CameraSource cameraSource;
    private EditText lookFor;
    public String toLookFor = "nic";
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int CAMERA_REQUEST = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }

        textView = findViewById(R.id.textView);
        cameraView = findViewById(R.id.CameraView);
        lookFor = findViewById(R.id.editText);



        startCameraFeed();

        // TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        lookFor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                toLookFor = lookFor.getText().toString();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Toast.makeText(MainActivity.this, lookFor.getText().toString(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void startCameraFeed(){
        final TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();

        if(!textRecognizer.isOperational()){
            Toast.makeText(this, "Cannot scan for some reason", Toast.LENGTH_SHORT).show();
        } else {
            cameraSource = new CameraSource.Builder(getApplicationContext(), textRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1920, 1080)
                    .setAutoFocusEnabled(true)
                    .setRequestedFps(5.0f).build();

            cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
                    try {
                        if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(new String[]{android.Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
                            return;
                        }

                        cameraSource.start(cameraView.getHolder());
                    } catch (IOException e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {

                }

                @Override
                public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
                    cameraSource.stop();
                }
            });

            textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
                @Override
                public void release() {

                }

                @Override
                public void receiveDetections(@NonNull Detector.Detections<TextBlock> detections) {
                    final SparseArray<TextBlock> items = detections.getDetectedItems();
                    if(items.size() > 0){
                        textView.post(new Runnable() {
                            @Override
                            public void run() {
                                StringBuilder stringBuilder = new StringBuilder();

                                for(int i=0;i<items.size();i++){
                                    TextBlock item = items.valueAt(i);
                                    stringBuilder.append(item.getValue());
                                    stringBuilder.append("\n");

                                }

                                textView.setText(stringBuilder.toString());
                                if(stringBuilder.toString().contains(toLookFor)){
                                    textView.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.red));
                                } else {
                                    textView.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.white));
                                }
                            }
                        });
                    }
                }
            });
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Camera permission granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }
}