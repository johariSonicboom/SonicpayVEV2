package com.sonicboom.sonicpayvui.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;

import com.google.gson.Gson;
import com.pax.dal.entity.EBeepMode;
import com.sonicboom.sonicpayvui.App;
import com.sonicboom.sonicpayvui.AppUpdateService;
import com.sonicboom.sonicpayvui.R;
import com.sonicboom.sonicpayvui.RetrofitClient;
import com.sonicboom.sonicpayvui.SharedPrefUI;
import com.sonicboom.sonicpayvui.activity.ConfigActivity;
import com.sonicboom.sonicpayvui.models.AppUpdate;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FileUtils {
    public static void zipFolder(String sourceFolderPath, String zipFilePath, String startDate, String endDate) throws IOException {
        File sourceFolder = new File(sourceFolderPath);
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFilePath)))) {
            zipFile(sourceFolder, sourceFolder.getName(), zipOutputStream, startDate, endDate);
        }
    }

    private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOutputStream, String startDate, String endDate) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
//            if (fileName.endsWith("/")) {
//                zipOutputStream.putNextEntry(new ZipEntry(fileName));
//                zipOutputStream.closeEntry();
//            } else {
//                zipOutputStream.putNextEntry(new ZipEntry(fileName + "/"));
//                zipOutputStream.closeEntry();
//            }
//            zipFile(childFile, fileName + "/" + childFile.getName(), zipOutputStream, startDate, endDate);
            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
                if(childFile.getName().endsWith(".txt")) {
                    int i = Integer.parseInt(childFile.getName().replace(".txt", ""));
                    if (Integer.parseInt(startDate) <= i && i <= Integer.parseInt(endDate))
                        zipFile(childFile, childFile.getName(), zipOutputStream, startDate, endDate);
                }
                if(childFile.getName().endsWith(".db"))
                    zipFile(childFile, childFile.getName(), zipOutputStream, startDate, endDate);
            }
            return;
        }
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileToZip))) {
            ZipEntry zipEntry = new ZipEntry(fileName);
            zipOutputStream.putNextEntry(zipEntry);
            byte[] bytes = new byte[1024];
            int bytesRead;
            while ((bytesRead = bis.read(bytes)) != -1) {
                zipOutputStream.write(bytes, 0, bytesRead);
            }
            zipOutputStream.closeEntry();
        }
    }

    public static int UpdateSoftware(Context context, String filename) {
            String filenamezip = filename + ".zip";

            String file_url = new SharedPrefUI(context).ReadSharedPrefStr(context.getString(R.string.server_url)) + "/" + new SharedPrefUI(context).ReadSharedPrefStr(context.getString(R.string.client_code)) + "/Files/" + filenamezip;

            // Beep Sound
            App.dal.getSys().beep(EBeepMode.FREQUENCE_LEVEL_6, 100);

            // File exist checking
            CheckLocalFiles(filename);

            // Download file from Server
            DownloadFiles(context, filenamezip);

            // Unzip file
            UnzipFile(filenamezip);

            // App Update
            String apkFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
            String apkName = filename+".apk";
            int value = App.dal.getSys().installApp( apkFolder + "/" + apkName);
            return value;

    }

    public static int InstallApk(String filename) {
        // Beep Sound
        App.dal.getSys().beep(EBeepMode.FREQUENCE_LEVEL_6, 100);

        String apkFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();

        int value = App.dal.getSys().installApp( apkFolder + "/" + filename);
        return value;
    }

    private static void CheckLocalFiles(String filename){

        try {

            String downloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
            File directory = new File(downloadsDirectory);
            // Check if the directory exists
            if (directory.exists() && directory.isDirectory()) {
                File[] files = directory.listFiles();

                // Iterate through the files and print their names
                if (files != null) {
                    for (File file : files) {
                        if (file.isFile()) {

                            String filenames = file.getName();
                            if (filenames.contains(filename))
                            {
                                LogUtils.i("CheckLocalFiles", "File exist in local");

                                // Check if the file exists
                                if (file.exists()) {
                                    // Delete the file
                                    boolean deleted = file.delete();

                                    if (deleted) {
                                        LogUtils.d("CheckLocalFiles", "File deleted successfully.");
                                    } else {
                                        LogUtils.d("CheckLocalFiles", "File deleted successfully.");
                                    }
                                } else {
                                    LogUtils.d("CheckLocalFiles", "File does not exist.");
                                }
                            }
                        }
                    }
                }
                else
                {
                    LogUtils.d("CheckLocalFiles", "No list of file in local");
                }
            }
            else
                LogUtils.d("CheckLocalFiles", "Directory not exist in local");

        }catch (Exception ex) {
            LogUtils.e("CheckLocalFiles", "Exception: " + ex.getMessage());
        }
    }

    public static void GetAppUpdate(Context context, String appId, String versionCode, Callback<List<AppUpdate.ApkInfo>> callback){
        String url = new SharedPrefUI(context).ReadSharedPrefStr(context.getString(R.string.app_update_server_url));
        if(url.isEmpty())
            url = "http://haste.my/AppUpdate";
        if(!url.endsWith("/"))
            url += "/";
        AppUpdateService appUpdateService = RetrofitClient.getAppUpdateService(url);
        AppUpdate.GetAppUpdateRequest appUpdateServiceReq = new AppUpdate().new GetAppUpdateRequest();
        appUpdateServiceReq.packageName = appId;
        appUpdateServiceReq.versionCode = versionCode;

        Call<List<AppUpdate.ApkInfo>> call = appUpdateService.GetAppUpdate(appUpdateServiceReq);
        call.enqueue(callback);
    }

    public static void DownloadFiles(Context context, String filename){
        String url = new SharedPrefUI(context).ReadSharedPrefStr(context.getString(R.string.app_update_server_url));
        if(url.isEmpty())
            url = "http://haste.my/AppUpdate";
        if(!url.endsWith("/"))
            url += "/";

        url += "Apks/" + filename;

        LogUtils.i("DownloadFiles", "URL: " + url);
        try {
            int SDK_INT = android.os.Build.VERSION.SDK_INT;
            if (SDK_INT > 8)
            {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                        .permitAll().build();
                StrictMode.setThreadPolicy(policy);
                //your codes here

            }

            URL u = new URL(url);
            InputStream is = u.openStream();

            DataInputStream dis = new DataInputStream(is);

            byte[] buffer = new byte[1024];
            int length;

            FileOutputStream fos = new FileOutputStream(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/" + filename));
            while ((length = dis.read(buffer))>0) {
                fos.write(buffer, 0, length);
            }

            LogUtils.i("DownloadFiles", "Download completed!");

        } catch (MalformedURLException ex) {
            LogUtils.e("DownloadFiles", "malformed url error: " + ex.getMessage());
        } catch (IOException ex) {
            LogUtils.e("DownloadFiles", "io error: " + ex.getMessage());
        } catch (SecurityException ex) {
            LogUtils.e("DownloadFiles", "security error: " + ex.getMessage());
        }
    }

    public static List<String> GetTngParamFiles(){
        String directoryPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/TNGParam";

        List<String> fileNames = new ArrayList<>();

        File directory = new File(directoryPath);
        if(!directory.exists())
            return fileNames;

        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".dat")) {
                    fileNames.add(file.getName().replace(".dat", ""));
                }
            }
        }

        return fileNames;
    }

    private static void UnzipFile(String filename){
        try {
            String FilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();

            // Open the zip file
            FileInputStream fis = new FileInputStream(FilePath + File.separator + filename);
            ZipInputStream zipInputStream = new ZipInputStream(fis);

            // Read each entry in the zip file
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                String entryName = zipEntry.getName();
                String filePath = FilePath + File.separator + entryName;

                // Create directories if any
                if (zipEntry.isDirectory()) {
                    new File(filePath).mkdirs();
                } else {
                    // Extract the file
                    ExtractFile(zipInputStream, filePath);
                }

                zipInputStream.closeEntry();
            }

            zipInputStream.close();
            fis.close();
            LogUtils.i("UnzipFile", "Unzip file completed!");

            //// Delete ZIP file
            //File file = new File(FilePath + File.separator + filename);

            //// Check if the file exists
            //if (file.exists()) {
            //    // Delete the file
            //    boolean deleted = file.delete();

            //    if (deleted) {
            //        Log.d("UpzipFile", "ZIP file deleted successfully.");
            //    } else {
            //        Log.d("UpzipFile", "ZIP file deleted successfully.");
            //    }
            //} else {
            //    Log.d("UpzipFile", "ZIP file does not exist.");
            //}
        } catch (Exception ex) {
            LogUtils.e("UnzipFile", "Exception: " + ex.getMessage());
        }
    }

    private static void ExtractFile(ZipInputStream zipInputStream, String filePath) throws IOException {
        byte[] buffer = new byte[1024];
        FileOutputStream fos = new FileOutputStream(filePath);
        int length;
        while ((length = zipInputStream.read(buffer)) > 0) {
            fos.write(buffer, 0, length);
        }
        fos.close();
    }


}
