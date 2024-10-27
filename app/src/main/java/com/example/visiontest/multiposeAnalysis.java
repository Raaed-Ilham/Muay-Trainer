package com.example.visiontest;



import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;


import com.example.visiontest.ml.AutoModel1;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

public class multiposeAnalysis extends AppCompatActivity {

    private Paint paint = new Paint();
    private ImageProcessor imageProcessor;
    private AutoModel1 model;

    //private LiteModelMovenetSingleposeLightningTfliteFloat164 model;


    private Bitmap bitmap;

    private ImageView imageView;
    private Handler handler;
    private HandlerThread handlerThread;
    private TextureView textureView;
    private CameraManager cameraManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getPermissions();

        try {
            model = AutoModel1.newInstance(this);
        }
        catch (IOException e) {

        }


        imageProcessor = new ImageProcessor.Builder().add(new ResizeOp(160,256, ResizeOp.ResizeMethod.BILINEAR)).build();



        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        handlerThread = new HandlerThread("videoThread");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());

        paint.setColor(Color.YELLOW);

        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture p0, int p1, int p2) {
                openCamera();
            }

            @Override
            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture p0, int p1, int p2) {}

            @Override
            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture p0) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture p0) {
                bitmap = textureView.getBitmap();
                TensorImage tensorImage = new TensorImage(DataType.UINT8);
                tensorImage.load(bitmap);
                tensorImage = imageProcessor.process(tensorImage);


                int[] shape = new int[] {1,160,256,3};


                //int[] shape = tensorImage.getTensorBuffer().getShape();




                TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(shape,DataType.UINT8);
                // TensorBuffer inputFeature0 = TensorBuffer.createDynamic(DataType.UINT8);


                Log.d("size of input buffer", String.valueOf(inputFeature0.getFlatSize()));

                // Interpreter interpreter= new Interpreter();




                inputFeature0.loadBuffer(tensorImage.getBuffer());

                Log.d("Buffer created", "true");

                AutoModel1.Outputs outputs = model.process(inputFeature0);

                Log.d("Model processed", "outputs created");


                TensorBuffer temp =  outputs.getOutputFeature0AsTensorBuffer();

                float[] outputFeature0 = outputs.getOutputFeature0AsTensorBuffer().getFloatArray();




                Bitmap mutable = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                Canvas canvas = new Canvas(mutable);
                int h = bitmap.getHeight();
                int w = bitmap.getWidth();
                int x = 0;

                //see line198 in tflie sample app for ref on looping through coordinates

                Log.d("output__", String.valueOf(outputFeature0.length));
                while (x <= 51) {
                    float score_val =  outputFeature0[x+2];

                    Log.d("score values", String.valueOf(Math.abs(score_val)));
                    //if (outputFeature0[x+2] > 0.3f) {
                    canvas.drawCircle(Math.abs(outputFeature0[x+1] * w), Math.abs(outputFeature0[x] * h), 10f, paint);

                    //}
                    x += 3;
                }

                imageView.setImageBitmap(mutable);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        model.close();
    }

    @SuppressLint("MissingPermission")
    private void openCamera() {
        try {
            CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            if (cameraManager != null) {
                for (String cameraId : cameraManager.getCameraIdList()) {
                    CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
                    Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                    if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                        cameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
                            @Override
                            public void onOpened(@NonNull CameraDevice cameraDevice) {
                                try {
                                    CaptureRequest.Builder captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                                    SurfaceTexture texture = textureView.getSurfaceTexture();
                                    if (texture != null) {
                                        texture.setDefaultBufferSize(textureView.getWidth(), textureView.getHeight());
                                        Surface surface = new Surface(texture);
                                        captureRequestBuilder.addTarget(surface);
                                        cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                                            @Override
                                            public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                                                try {
                                                    CaptureRequest captureRequest = captureRequestBuilder.build();
                                                    cameraCaptureSession.setRepeatingRequest(captureRequest, null, null);
                                                } catch (CameraAccessException e) {
                                                    e.printStackTrace();
                                                }
                                            }

                                            @Override
                                            public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                                                // Handle configuration failure
                                            }
                                        }, null);
                                    }
                                } catch (CameraAccessException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onDisconnected(@NonNull CameraDevice cameraDevice) {
                                // Handle camera disconnect
                            }

                            @Override
                            public void onError(@NonNull CameraDevice cameraDevice, int i) {
                                // Handle camera error
                            }
                        }, null);
                        break;
                    }
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void getPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 101);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] != PERMISSION_GRANTED) {
            getPermissions();
        }
    }
}
