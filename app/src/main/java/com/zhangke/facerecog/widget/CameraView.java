package com.zhangke.facerecog.widget;

import android.content.Context;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.zhangke.facerecog.util.ByteArrayPool;
import com.zldlib.ZldLog.ZldLog;
import com.zldlib.util.CameraUtil;


/**
 * 自定义 CameraView 可以实现实时人脸框
 * <p>
 * Created by ZhangKe on 2017/12/5.
 */

public class CameraView extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {

    private static final String TAG = "CameraView";

    private Camera mCamera;
    private SurfaceHolder mSurfaceHolder;

    private int mWidth, mHeight, mFormat;
    private int mCameraID = Camera.CameraInfo.CAMERA_FACING_BACK;

    private OnCameraCallback onCameraCallback;

    private ByteArrayPool byteArrayPool;
    private int dataSize = 0;

    private Camera.Parameters mCameraParameters;

    private boolean previewing = false;
    private boolean surfaceCreated = false;

    private boolean createdAndPreview = false;

    public CameraView(Context context) {
        super(context);
        init();
    }

    public CameraView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        mWidth = 1080;
        mHeight = 1920;
        mFormat = ImageFormat.NV21;
        byteArrayPool = new ByteArrayPool(3);
        mSurfaceHolder.setKeepScreenOn(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        initCamera();
        Log.e(TAG, "surfaceCreated inited");
        surfaceCreated = true;
        if (createdAndPreview) {
            startPreview();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.e(TAG, "surfaceDestroyed");
        surfaceCreated = false;
        CameraUtil.releaseCamera(mCamera);
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (onCameraCallback != null) {
            byte[] buffer = byteArrayPool.getBuf(dataSize);
            System.arraycopy(data, 0, buffer, 0, buffer.length);
            onCameraCallback.onPreview(buffer);
            byteArrayPool.returnBuf(buffer);
        }
        if (this.mCamera != null) {
            this.mCamera.addCallbackBuffer(data);
        }
    }

    private void initCamera() {
        try {
            mCamera = Camera.open(mCameraID);
            mCamera.setPreviewDisplay(this.getHolder());

            mCameraParameters = mCamera.getParameters();

            CameraUtil.setCameraPreviewSize(mCameraParameters, mHeight);
            CameraUtil.setCameraPictureSize(mCameraParameters, mHeight);

            mCameraParameters.setPreviewFormat(mFormat);
            mCameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);//自动对焦
            mCameraParameters.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
            CameraUtil.setCameraDisplayOrientation(getContext(), mCameraID, mCamera);

            mCamera.setParameters(mCameraParameters);
        } catch (Exception e) {
            ZldLog.e(TAG, "initCamera", e);
        }
        if (mCamera != null) {
            setCameraCallback();
        }
    }

    public void startPreview() {
        if (!previewing) {
            if (surfaceCreated && mCamera != null) {
                mCamera.startPreview();
                previewing = true;
                setCameraCallback();
            } else {
                createdAndPreview = true;
            }
        }
    }

    public void stopPreview() {
        if (previewing && surfaceCreated && mCamera != null) {
            mCamera.stopPreview();
            previewing = false;
        }
        createdAndPreview = false;
    }

    public Camera getCamera() {
        return mCamera;
    }

    public void releaseCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    public void openFlash() {
        if (supportFlashLight(getContext())) {
            if (null != mCamera) {
                mCameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                mCamera.setParameters(mCameraParameters);
            }
        }
    }

    public void closeFlash() {
        if (supportFlashLight(getContext()) && null != mCamera) {
            mCameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(mCameraParameters);
        }
    }

    public int getImageHeight() {
        return mHeight;
    }

    public int getImageWidth() {
        return mWidth;
    }

    public void setOnCameraCallback(OnCameraCallback onCameraCallback) {
        this.onCameraCallback = onCameraCallback;
    }

    public interface OnCameraCallback {
        void onPreview(byte[] data);
    }

    @Override
    protected void onDetachedFromWindow() {
        surfaceCreated = false;
        previewing = false;
        super.onDetachedFromWindow();
    }

    /**
     * 该设备是否支持闪光灯
     */
    public static boolean supportFlashLight(Context context) {
        final PackageManager pm = context.getPackageManager();
        final FeatureInfo[] features = pm.getSystemAvailableFeatures();
        boolean canOpenFlash = false;
        for (final FeatureInfo f : features) {
            if (PackageManager.FEATURE_CAMERA_FLASH.equals(f.name)) {
                canOpenFlash = true;
            }
        }
        return canOpenFlash;
    }

    private void setCameraCallback() {
        mWidth = mCamera.getParameters().getPreviewSize().width;
        mHeight = mCamera.getParameters().getPreviewSize().height;

        Camera.Size imageSize = this.mCamera.getParameters().getPreviewSize();
        this.mWidth = imageSize.width;
        this.mHeight = imageSize.height;
        int lineBytes = imageSize.width * ImageFormat.getBitsPerPixel(this.mFormat) / 8;
        this.mCamera.addCallbackBuffer(new byte[lineBytes * this.mHeight]);
        this.mCamera.addCallbackBuffer(new byte[lineBytes * this.mHeight]);
        this.mCamera.addCallbackBuffer(new byte[lineBytes * this.mHeight]);
        this.mCamera.setPreviewCallbackWithBuffer(this);
        dataSize = lineBytes * this.mHeight;
    }
}
