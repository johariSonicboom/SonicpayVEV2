package com.sonicboom.sonicpayvui.utils;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.pax.dal.IScanCodec;
import com.pax.dal.entity.DecodeResult;
import com.pax.dal.entity.DecodeResultRaw;
import com.pax.dal.entity.EBeepMode;
import com.sbs.aidl.IAIDLSonicpayInterface;
import com.sonicboom.sonicpayvui.App;
import com.sonicboom.sonicpayvui.MainActivity;
import com.sonicboom.sonicpayvui.models.TCPGeneralMessage;

import java.io.IOException;

public class ScannerUtils implements PreviewCallback, SurfaceHolder.Callback {

    private boolean isOpen = false;

    private byte[] data = null;
    private int WIDTH = 640, HEIGHT = 480;
    private byte[] previewBuff;

    private Camera camera;
    private IScanCodec scanCodec;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Context context;
    private Handler handler;
    private int amount;

    public ScannerUtils(Context context, SurfaceView view, Handler handler){
        this.surfaceView = view;
        this.context = context;
        this.handler = handler;
    }

    public void disableFormat(int format) {
        scanCodec.disableFormat(format);
    }

    private void enableFormat() {
        for (int i = 1; i <= 24; i++) {
            scanCodec.enableFormat(i);
        }
    }

    public void initScanCodec() {
        scanCodec= App.dal.getScanCodec();
        scanCodec.init(this.context, WIDTH,HEIGHT);
    }

    public DecodeResult decode(byte[] data) {
        DecodeResult result = scanCodec.decode(data);
        if(result.getContent()!=null){
            App.dal.getSys().beep(EBeepMode.FREQUENCE_LEVEL_5, 100);
        }
        return result;
    }

    public DecodeResultRaw decodeRaw(byte[] data) {
        return scanCodec.decodeRaw(data);
    }

    public void StartScan(int amount) throws IOException {
        this.amount = amount;
        if (!isOpen) {
            if (!initCamera())
                return;

            initScanCodec();
            enableFormat();

            surfaceHolder = surfaceView.getHolder();
            surfaceHolder.addCallback(this);
            camera.addCallbackBuffer(data);
            camera.setPreviewCallbackWithBuffer(this);
            camera.setPreviewDisplay(surfaceHolder);

        } else
            releaseRes();

        isOpen = !isOpen;
    }
    public boolean initCamera() {

        try {
            camera = Camera.open(0);
            // camera.setDisplayOrientation(90);
            Camera.Parameters parameters = camera.getParameters();

            // String pictureSize = Arrays.toString( parameters.getSupportedPictureSizes().toArray());
            // String previewSize = Arrays.toString(parameters.getSupportedPreviewSizes().toArray());
            // Log.i("Test", previewSize +" \n");

            parameters.setPreviewSize(WIDTH, HEIGHT);
            parameters.setPictureSize(WIDTH, HEIGHT);
            parameters.setExposureCompensation(-3);
            //parameters.setZoom(parameters.getZoom() + 15);
            // parameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            // parameters.setAutoWhiteBalanceLock(true);
            camera.setParameters(parameters);

            camera.cancelAutoFocus();
            camera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {

                }
            });

            previewBuff = new byte[10 * WIDTH * HEIGHT];

            // For formats besides YV12, the size of the buffer is determined by multiplying the preview image width,
            // height, and bytes per pixel. The width and height can be read from Camera.Parameters.getPreviewSize(). Bytes
            // per pixel can be computed from android.graphics.ImageFormat.getBitsPerPixel(int) / 8, using the image format
            // from Camera.Parameters.getPreviewFormat().
            float bytesPerPixel = ImageFormat.getBitsPerPixel(parameters.getPreviewFormat()) / (float) 8;
            data = new byte[(int) (bytesPerPixel * WIDTH * HEIGHT)];

            Log.i("Test", "previewFormat:" + parameters.getPreviewFormat() + " bytesPerPixel:" + bytesPerPixel
                    + " prewidth:" + parameters.getPreviewSize().width + " preheight:" + parameters.getPreviewSize().height);

            camera.addCallbackBuffer(data);
        } catch (Exception e) {
            LogUtils.e("ScannerUtils", "initCamera Exception: " + Log.getStackTraceString(e));
        }
        return true;
    }

    private void startPreview() throws IOException {
        if (camera != null) {
            camera.setDisplayOrientation(90);
            camera.setPreviewDisplay(surfaceHolder);
            camera.setPreviewCallbackWithBuffer(this);
            camera.addCallbackBuffer(previewBuff);
            camera.startPreview();
        }
    }

    private void stopPreview() {
        if (camera != null) {
            camera.setPreviewCallbackWithBuffer(null);
            camera.stopPreview();
        }
    }

    public void releaseRes() {
        scanCodec.release();
        stopPreview();
        camera.release();
        camera = null;
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        if (data != null) {
            DecodeResult decodeResult = decode(data);
            if(decodeResult.getContent()!=null){
                LogUtils.i("ScannerUtils","QR Scanned:"+ decodeResult.getContent());
                releaseRes();
                isOpen = !isOpen;

                TCPGeneralMessage qrResponseMsg = new TCPGeneralMessage();
                qrResponseMsg.Command = "QRMerchantScan";
                qrResponseMsg.Data = decodeResult.getContent();

                //Notify UI to change to Processing screen
                Message message = handler.obtainMessage();
                message.obj = new Gson().toJson(qrResponseMsg);
                handler.sendMessage(message);
            }
            camera.addCallbackBuffer(data);
        }
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {}

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        synchronized (this) {
            try {
               startPreview();
            } catch (IOException e) {
                LogUtils.e("ScannerUtils", "surfaceChanged Exception: " + Log.getStackTraceString(e));
            }
        }
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {}
}