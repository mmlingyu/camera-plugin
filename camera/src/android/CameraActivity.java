package com.inspace.plugin;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.opengl.GLES11Ext;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.common.collect.Lists;
import com.google.mediapipe.framework.image.BitmapImageBuilder;
import com.google.mediapipe.framework.image.MPImage;
import com.google.mediapipe.tasks.core.BaseOptions;
import com.google.mediapipe.tasks.core.Delegate;
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetector;
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetectorResult;
import com.inspace.plugin.ResultBundle;
import java.nio.ByteBuffer;
import java.util.Arrays;
import android.view.Gravity;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
/**
 * TODO add keyguard when recording. (Cannot leave app when it is recording)
 */
public class CameraActivity extends Activity {
    private static final String TAG = CameraActivity.class.getSimpleName();
    /*权限请求Code*/
    private final static int PERMISSION_REQUEST_CODE = 1234;
    /*我们需要使用的权限*/
    private String[] permissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    };
    private int pos, i;
    private int times_nobody, time_body = 3;
    private int default_camera = 0;
    private boolean istalking = false;
    public static final String RESULT_RECEIVER = "resultReceiver";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*SDK>6.0 权限申请*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(permissions[0]) != PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(permissions[1]) != PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(permissions[2]) != PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(permissions[3]) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(permissions, PERMISSION_REQUEST_CODE);
        }
        Window window = getWindow();

        window.setGravity(Gravity.START | Gravity.TOP);

//设置为1像素大小

        WindowManager.LayoutParams params = window.getAttributes();

        params.x = 0;

        params.y = 0;

        params.width = 1;

        params.height = 1;

        window.setAttributes(params);
        handleStartRecordingCommand();
    }

    private ObjectDetector objectDetector;

    ResultBundle detectImage(Bitmap image) {
        long startTime = SystemClock.uptimeMillis();
        MPImage mpImage = new BitmapImageBuilder(image).build();
        ObjectDetectorResult detectionResult = objectDetector.detect(mpImage);
        long inferenceTimeMs = SystemClock.uptimeMillis() - startTime;
        if (inferenceTimeMs >= 2000) {
            times_nobody = 3;
            time_body = 2;
        } else if (inferenceTimeMs > 600 && inferenceTimeMs < 2000) {
            times_nobody = 6;
        } else {
            time_body = 3;
            times_nobody = 16;
        }
        if (detectionResult.detections().size() > 0) {
            String ca = detectionResult.detections().get(0).categories().get(0).categoryName();
            Log.e(TAG, " ---- --" + detectionResult.detections().get(0).categories().get(0).categoryName());
            Log.e(TAG, " ---- --" + detectionResult.detections().get(0).categories().get(0).score());
            DeteckManager.detect.onDetect(ca, detectionResult.detections().get(0).categories().get(0).score());
        }
        return new ResultBundle(
                Lists.newArrayList(detectionResult),
                inferenceTimeMs,
                image.getHeight(),
                image.getWidth(), 0);
    }


    private ImageReader mImageReader;
    private CaptureRequest.Builder mCaptureRequestBuilder;
    private CaptureRequest mCaptureRequest;

    private CameraCaptureSession mCameraCaptureSession;
    ImageReader imageReader = ImageReader.newInstance(1080, 720,
            ImageFormat.JPEG, /*maxImages*/2);
    boolean mRecording = false;

    private void handleStartRecordingCommand() {
        if (!Util.isCameraExist(this)) {
            throw new IllegalStateException("There is no device, not possible to start recording");
        }

        if (mRecording) {
            // Already recording
            return;
        }
        BaseOptions baseOptions = BaseOptions.builder().setDelegate(Delegate.CPU).setModelAssetPath("efficientdet-lite0.tflite").build();
        ObjectDetector.ObjectDetectorOptions objectDetectorOptions = ObjectDetector.ObjectDetectorOptions.builder().setBaseOptions(baseOptions).setMaxResults(2).build();
        objectDetector = ObjectDetector.createFromOptions(this.getApplication(), objectDetectorOptions);
        mRecording = true;
        HandlerThread handlerThread = new HandlerThread("Camera2");
        handlerThread.start();
        Handler childHandler = new Handler(handlerThread.getLooper());
        Util.getCameraInstance(default_camera, this, new CameraDevice.StateCallback() {
            @Override
            public void onOpened(CameraDevice cameraDevice) {
                Log.d(TAG, "onOpened is finished.");

                SurfaceTexture surfaceTexture = new SurfaceTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
                surfaceTexture.setDefaultBufferSize(1080, 720);

                Surface previewSurface = new Surface(surfaceTexture);
// 设置监听器以便于获取图片
                imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                    @Override
                    public void onImageAvailable(ImageReader reader) {
                        Image image = null;
                        Log.d(TAG, "onImageAvailable --> ");
                        try {
                            image = reader.acquireLatestImage();
                            if (image != null) {
                                Image.Plane[] planes = image.getPlanes();
                                ByteBuffer buffer = planes[0].getBuffer();
                                byte[] data = new byte[buffer.capacity()];
                                buffer.get(data);

                                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                                detectImage(bitmap);
                                bitmap.recycle();
                                bitmap = null;
                                // 处理数据...
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            if (image != null) {
                                image.close();
                            }
                        }
                    }
                }, childHandler); // 可以使用后台线程的Handler

// 创建并配置相机的输出目标
                try {

                    mCaptureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                    // mCaptureRequestBuilder.addTarget(previewSurface);
                    //设置实时帧数据接收
                    mCaptureRequestBuilder.addTarget(imageReader.getSurface());
                    cameraDevice.createCaptureSession(Arrays.asList(imageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {

                            try {
                                mCameraCaptureSession = session;
                                mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                //开始预览
                                mCameraCaptureSession.setRepeatingRequest(mCaptureRequestBuilder.build(), null, childHandler);
                            } catch (
                                    CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                        }
                    }, childHandler);

                    /* cameraDevice.createCaptureSession(Arrays.asList(imageReader.getSurface()), *//*callback*//* null, childHandler);
                   CaptureRequest.Builder captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                   captureRequestBuilder.addTarget(imageReader.getSurface());
                   // 创建请求并添加目标Surface


// 开始预览
                   cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, null);*/
                } catch (CameraAccessException e) {
                    throw new RuntimeException(e);
                }


            }

            @Override
            public void onDisconnected(CameraDevice mCameraDevice) {
                Log.d(TAG, "onDisconnected is finished.");
                if (null != mCameraDevice) {
                    mCameraDevice.close();
                    mCameraDevice = null;
                }
            }

            @Override
            public void onError(CameraDevice mCameraDevice, int error) {
                Log.d(TAG, "onError is finished.");
                Log.d(TAG, "onDisconnected is finished.");
                if (null != mCameraDevice) {
                    mCameraDevice.close();
                    mCameraDevice = null;
                }
            }
        },childHandler);
    }
}