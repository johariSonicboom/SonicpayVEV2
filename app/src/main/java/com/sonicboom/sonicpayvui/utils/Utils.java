package com.sonicboom.sonicpayvui.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;

import com.google.gson.internal.LinkedTreeMap;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.sonicboom.sonicpayvui.R;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Utils {
    private static final String TAG = "Utils";

    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':')<0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogUtils.e(TAG,"Exception: " + Log.getStackTraceString(e));
        }
        return "";
    }

    public static String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest.getInstance(MD5);
            digest.update(s.getBytes());
            byte[] messageDigest = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                StringBuilder h = new StringBuilder(Integer.toHexString(0xFF & aMessageDigest));
                while (h.length() < 2)
                    h.insert(0, "0");
                hexString.append(h);
            }
            return hexString.toString();

        } catch (Exception e) {
            LogUtils.e(TAG,"Exception: " + Log.getStackTraceString(e));
        }
        return "";
    }

    public static void LinkedTreeCastDoubleToInt(LinkedTreeMap<String, Object> map){
        if(map != null){
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                Object value = entry.getValue();

                if (value instanceof Double) {
                    // The value is a double, cast it to int
                    int iValue = (int) (double) value;
                    entry.setValue(iValue);
                }
            }
        }
    }

    public static String getServiceAppCode() { return "com.sbs.SonicpayService";}

    public static String getServiceClassName() { return "com.sbs.SonicpayService.Service.SonicpayServiceHandler";}

    public static String getServiceMinVersion(){return "5.1.16";}

    public static Bitmap generateQRfromStr(Context context, String str, boolean isDuitNow) throws WriterException {
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix bitMatrix = writer.encode(str, BarcodeFormat.QR_CODE, 380, 350);

        int w = bitMatrix.getWidth();
        int h = bitMatrix.getHeight();
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                pixels[y * w + x] = bitMatrix.get(x, y) ? isDuitNow ? ContextCompat.getColor(context, R.color.duitnow_pink) : Color.BLACK : Color.WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
        return bitmap;
    }

    public static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
               LogUtils.e("DownloadImageTask", " Exception: " + Log.getStackTraceString(e));
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    public static int compareVersionStr(String required, String current) {
        String[] requiredParts = required.split("\\.");
        String[] currentParts = current.split("\\.");
        int length = Math.max(requiredParts.length, currentParts.length);
        for (int i = 0; i < length; i++) {
            int requiredPart = i < requiredParts.length ? Integer.parseInt(requiredParts[i]) : 0;
            int currentPart = i < currentParts.length ? Integer.parseInt(currentParts[i]) : 0;
            if (requiredPart < currentPart)
                return -1;
            if (requiredPart > currentPart)
                return 1;
        }
        return 0;
    }
}
