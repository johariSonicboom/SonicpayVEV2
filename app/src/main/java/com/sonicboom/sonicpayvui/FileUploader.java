package com.sonicboom.sonicpayvui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.sonicboom.sonicpayvui.utils.LogUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FileUploader {
    public static void uploadFile(Context context, String baseURL, Uri fileUri, boolean showResult) {
        // Create a file request body
        RequestBody fileBody = RequestBody.create(MediaType.parse("multipart/form-data"), getFile(fileUri));

        // Create a multipart request body
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String date = dateFormat.format(Calendar.getInstance().getTime());
        String fileName = date + "-" + new SharedPrefUI(context).ReadSharedPrefStr(context.getString(R.string.serial_number)) + "_SPUI"  +".zip";

        MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", fileName, fileBody);

        // Upload the file using Retrofit service
        FileUploadService service = RetrofitClient.getService(baseURL);
        Call<Void> call = service.uploadFile(filePart);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // File uploaded successfully
                    LogUtils.i("File Uploader", "Uploaded successfully");
                    File file = getFile(fileUri);
                    file.delete();
                    if(showResult)
                        new AlertDialog.Builder(context)
                            .setTitle("File upload")
                            .setMessage("File uploaded successfully")
                            .setIcon(R.drawable.check_leaf_shadow )
                            .setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss())
                            .create()
                            .show();
                } else {
                    if(showResult)
                    // Handle upload failure
                        new AlertDialog.Builder(context)
                            .setTitle("File upload")
                            .setMessage("Failed: " + response.code() + "- " + response.message())
                            .setIcon(R.drawable.cross_fat )
                            .setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss())
                            .create()
                            .show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // Handle upload failure
                LogUtils.i("File Uploader", "Uploaded failed");
                if(showResult)
                    new AlertDialog.Builder(context)
                        .setTitle("File upload")
                        .setMessage("File upload failed")
                        .setIcon(R.drawable.cross_fat )
                        .setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss())
                        .create()
                        .show();
            }
        });
    }
    private static File getFile(Uri uri) {
        return new File(uri.getPath());
    }
}
