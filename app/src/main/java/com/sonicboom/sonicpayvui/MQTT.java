package com.sonicboom.sonicpayvui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;
import com.sankuai.waimai.router.Router;
import com.sbs.aidl.Class.GetStatusResult;
import com.sbs.aidl.Class.eTerminalState;
import com.sbs.aidl.IAIDLCardCallbackInterface;
import com.sbs.aidl.IAIDLSonicpayInterface;
import com.sonicboom.sonicpayvui.models.AppUpdate;
import com.sonicboom.sonicpayvui.models.TCPGeneralMessage;
import com.sonicboom.sonicpayvui.models.eHostNo;
import com.sonicboom.sonicpayvui.utils.FileUtils;
import com.sonicboom.sonicpayvui.utils.LogUtils;
import com.sonicboom.sonicpayvui.utils.Utils;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MQTT {

    private final String TAG = "MQTT";

    private MqttClient client;
    public String LastTran = "";
    public Payload queryPayload;
    private Context context;
    private Handler handler;
    private IAIDLSonicpayInterface sonicInterface;
    private IAIDLCardCallbackInterface callbackInterface;

    public void Init(Context context, Handler handler){
        LogUtils.d(TAG, "Init started...");

        this.context = context;
        this.handler = handler;

        while (true) {
            try {
                String serverUrl = new SharedPrefUI(context).ReadSharedPrefStr(context.getString(R.string.monitoring_url));
                if (serverUrl == null || serverUrl.isEmpty()) {
                    Thread.sleep(3000);
                    continue;
                }

                client = new MqttClient("tcp://" + serverUrl,
                        new SharedPrefUI(context).ReadSharedPrefStr(context.getString(R.string.serial_number)), new MemoryPersistence());

                MqttConnectOptions connOpts = new MqttConnectOptions();
                connOpts.setCleanSession(true);
                connOpts.setConnectionTimeout(30);
                connOpts.setKeepAliveInterval(30);
                connOpts.setAutomaticReconnect(true);

                client.setCallback(new MqttCallbackExtended() {
                    @Override
                    public void connectComplete(boolean reconnect, String serverURI) {
                        if (reconnect) {
                            LogUtils.d(TAG, "Reconnected to broker!");
                        } else {
                            LogUtils.d(TAG, "Connected to broker for the first time!");
                        }

                        TopicToSubscribe();
                    }

                    @Override
                    public void connectionLost(Throwable cause) {
                        //Log.e(TAG, "connectionLost: ", cause);
                        try {
                            client.reconnect();

                            if (client.isConnected()) {
                                Subscribe("Command/" + new SharedPrefUI(context).ReadSharedPrefStr(context.getString(R.string.serial_number)));
                            }
                        } catch (Exception ex) {
                            LogUtils.e(TAG, "Exception: " + ex);
                        }
                    }

                    @Override
                    public void messageArrived(String topic, MqttMessage message) {
                        try {

                            if (topic.equals("Command/" + new SharedPrefUI(context).ReadSharedPrefStr(context.getString(R.string.serial_number)))) {
                                Payload payload = new Gson().fromJson(message.toString(), Payload.class);
                                if(!payload.Command.equalsIgnoreCase("getstatus"))
                                    Log.i(TAG, "messageArrived: " + String.format("%s - %s", topic, message));

                                LogUtils.i(TAG, payload.Command + " triggered!");
                                switch (payload.Command.toLowerCase()){
                                    case "getstatus":
                                        SendStatus(payload.ClientId, payload.MessageId);
                                        break;
                                    case "fileupload":
                                        UploadUIFile(payload);
                                        break;
                                    case "appupdate":
                                        AppUpdate(context, BuildConfig.APPLICATION_ID, String.valueOf(BuildConfig.VERSION_CODE));
                                        break;
                                    case "maintenancescreen":
                                        Router.startUri(context, RouterConst.CONFIG_LOGIN);
                                        break;
                                    case "apprestart":
                                        AppRestart(payload);
                                        break;
                                    case "systemreboot":
                                        App.dal.getSys().reboot();
                                        break;
                                    case "setmaintenance":
                                        SetMaintenanceMode(payload,true);
                                        break;
                                    case "clearmaintenance":
                                        SetMaintenanceMode(payload,false);
                                        break;
                                    case "spdownloadconfig":
                                        int result = sonicInterface.DownloadConfig();
                                        Payload newPayload = new Payload();
                                        newPayload.MessageId = payload.MessageId;
                                        newPayload.ClientId = payload.ClientId;
                                        newPayload.Command = payload.Command;
                                        newPayload.SerialNo = new SharedPrefUI(context).ReadSharedPrefStr(context.getString(R.string.serial_number));
                                        newPayload.IsSuccess = result == 1;
                                        newPayload.Value = result == 0 ? "Download config failed" : result == 1 ? "Download config success" : "No update";

                                        Publish("Response/" + payload.ClientId, new Gson().toJson(newPayload));
                                        break;
                                    case "spfileupload":
                                        UploadServiceFile(payload);
                                        break;
                                    case "spappupdate":
                                        PackageManager pm = context.getPackageManager();
                                        PackageInfo pInfo = pm.getPackageInfo(Utils.getServiceAppCode(), 0);
                                        AppUpdate(context, Utils.getServiceAppCode(), String.valueOf(pInfo.versionCode));
                                        break;
                                    case "spemvsettlement":
                                        EMVSettlement(payload);
                                        break;
                                    case "sptngclosebatch":
                                        Settlement(eHostNo.TouchNGo.getValue(), payload, true);
                                        break;
                                    case "sprunquery":
                                        SPRunQuery(payload);
                                        break;
                                    case "spreadpref":
                                        SPReadSharedPref(payload);
                                        break;
                                    case "spwritepref":
                                        SPWriteSharedPref(payload);
                                        break;
                                    case "spresetemvconfigfile":
                                        SPResetEMVConfigFile(payload);
                                        break;
                                    default:
                                        break;
                                }
                            }
                        } catch (Exception e) {
                            LogUtils.e(TAG, "messageArrived Exception: " + Log.getStackTraceString(e));
                        }
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {
                        //Log.d(TAG, "deliveryComplete: " + Arrays.toString(token.getTopics()));
                    }
                });

                client.connect(connOpts);

                break;
            } catch (Exception ex) {
                LogUtils.e(TAG, "Exception: " + Log.getStackTraceString(ex));
            }

            try {
                Thread.sleep(3000);
            } catch (Exception e) {
                LogUtils.e(TAG, "Exception: " + Log.getStackTraceString(e));
            }
        }

        LogUtils.d(TAG, "Init ended.");
    }

    public void SetServiceCallback(IAIDLSonicpayInterface sonicInterface, IAIDLCardCallbackInterface callbackInterface){
        this.sonicInterface = sonicInterface;
        this.callbackInterface = callbackInterface;
    }

    public void Publish(String topic, String payload) {
        //Log.d(TAG, "Publish started...");
        //Log.i(TAG, "Publish to " + topic + ": " + payload);
        try {
            if (client != null && !client.isConnected()) {
                client.connect();
            }

            byte[] encodedPayload = payload.getBytes(StandardCharsets.UTF_8);
            MqttMessage message = new MqttMessage(encodedPayload);
            client.publish(topic, message);

        } catch (Exception e) {
            LogUtils.e(TAG, "Publish Exception: " + Log.getStackTraceString(e));
        }
        //Log.d(TAG, "Publish ended.");
    }

    private void Subscribe(String topic) {
        //Log.d(TAG, "Subscribe started...");
        //Log.i(TAG, "Subscribe to " + topic);
        try {
            client.subscribe(topic);
        } catch (Exception e) {
            LogUtils.e(TAG, "Subscribe Exception: " + Log.getStackTraceString(e));
        }
        //Log.d(TAG, "Subscribe ended.");
    }

    private void TopicToSubscribe() {
        Subscribe("Command/" + new SharedPrefUI(context).ReadSharedPrefStr(context.getString(R.string.serial_number)));
    }

    @SuppressLint("SimpleDateFormat")
    private void SendStatus(String senderClientId, String messageId) {
        Calendar cal = Calendar.getInstance();
        Date date = cal.getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        Status status = new Status();
        status.MessageId = messageId;
        status.SerialNo = new SharedPrefUI(context).ReadSharedPrefStr(context.getString(R.string.serial_number));
        status.Command = "GetStatus";
        status.Severity = 1;
        status.Status = "Online";
        status.SysTime = dateFormat.format(date);
        status.IPAddress = Utils.getIPAddress(true);
        status.AppVersion = GetListAppVersion();

        status.LastTran = LastTran;
        status.QueueCount = GetQueueCount();

        Gson gson = new Gson();
        String payload = gson.toJson(status);

        Publish("Response/" + senderClientId, payload);
    }

    private List<AppVersion> GetListAppVersion() {
        List<AppVersion> listAppVersion = new ArrayList<>();

        try {
            AppVersion uiApp = new AppVersion();
            uiApp.Name = "SonicpayVUI";
            uiApp.Version = new SharedPrefUI(context).ReadSharedPrefStr(context.getString(R.string.ui_app_version));
            listAppVersion.add(uiApp);

            AppVersion spApp = new AppVersion();
            spApp.Name = "SonicpayVS";
            spApp.Version = new SharedPrefUI(context).ReadSharedPrefStr(context.getString(R.string.service_app_version));
            listAppVersion.add(spApp);
        }
        catch (Exception ex) {
            LogUtils.e(TAG, "GetListAppVer Exception: " + Log.getStackTraceString(ex));
        }

        return listAppVersion;
    }

    private void UploadUIFile(Payload oriPayload) {
        Payload payload = new Payload();

        try {
            payload.MessageId = oriPayload.MessageId;
            payload.ClientId = oriPayload.ClientId;
            payload.Command = oriPayload.Command;
            payload.SerialNo = new SharedPrefUI(context).ReadSharedPrefStr(context.getString(R.string.serial_number));

            if(oriPayload.DateFrom == null || oriPayload.DateTo == null){
                throw new Exception("Invalid DateFrom/DateTo");
            }

            SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            Date dateFrom = format.parse(oriPayload.DateFrom);
            Date dateTo = format.parse(oriPayload.DateTo);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
            String startDate = dateFormat.format(dateFrom);
            String endDate = dateFormat.format(dateTo);

            String sourceFolderPath = context.getFilesDir().getPath() + "/appLog";

            String date = dateFormat.format(Calendar.getInstance().getTime());
            String zipFilePath = Environment.getExternalStorageDirectory() + "/SPUI _" + date + ".zip";

            FileUtils.zipFolder(sourceFolderPath, zipFilePath, startDate, endDate);
            File zipFile = new File(zipFilePath);
            FileUploader.uploadFile(context, sonicInterface.ReadSharedPref(context.getString(R.string.TMSWSURL)), Uri.fromFile(zipFile), false);

            payload.IsSuccess = true;
        } catch (Exception e) {
            LogUtils.e(TAG, "UploadUIFile Exception: " + e);
            payload.Value = e.getMessage();
            payload.IsSuccess = false;
        }

        Publish("Response/" + payload.ClientId, new Gson().toJson(payload));
    }

    private void UploadServiceFile(Payload oriPayload) {
        Payload payload = new Payload();
        try {
            payload.MessageId = oriPayload.MessageId;
            payload.ClientId = oriPayload.ClientId;
            payload.Command = oriPayload.Command;
            payload.SerialNo = new SharedPrefUI(context).ReadSharedPrefStr(context.getString(R.string.serial_number));

            SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            Date dateFrom = format.parse(oriPayload.DateFrom);
            Date dateTo = format.parse(oriPayload.DateTo);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String startDate = dateFormat.format(dateFrom);
            String endDate = dateFormat.format(dateTo);

            boolean logFileOnly = oriPayload.LogFileOnly;

            payload.IsSuccess = sonicInterface.FileUpload(startDate, endDate, logFileOnly);

        } catch (Exception e) {
            LogUtils.e(TAG, "UploadServiceFile Exception: " + e);
            payload.Value = e.getMessage();
            payload.IsSuccess = false;
        }

        Publish("Response/" + payload.ClientId, new Gson().toJson(payload));
    }

    private void AppRestart(Payload oriPayload) {
        Payload payload = new Payload();

        try {
            payload.MessageId = oriPayload.MessageId;
            payload.ClientId = oriPayload.ClientId;
            payload.Command = oriPayload.Command;
            payload.SerialNo = new SharedPrefUI(context).ReadSharedPrefStr(context.getString(R.string.serial_number));

            Thread.sleep(500);
            payload.IsSuccess = true;
        } catch (Exception ex) {
            LogUtils.e(TAG, "AppRestart Exception: " + Log.getStackTraceString(ex));
            payload.Value = ex.getMessage();
            payload.IsSuccess = false;
        }

        Publish("Response/" + oriPayload.ClientId, new Gson().toJson(payload));

        if (context instanceof Activity) {
            ((Activity) context).finish();
        }

        System.exit(0);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    private void EMVSettlement(Payload oriPayload) {
        Payload payload = new Payload();

        try {
            payload.MessageId = oriPayload.MessageId;
            payload.ClientId = oriPayload.ClientId;
            payload.Command = oriPayload.Command;
            payload.SerialNo = new SharedPrefUI(context).ReadSharedPrefStr(context.getString(R.string.serial_number));

            List<Integer> hostNos;

            boolean isPBB = sonicInterface.ReadSharedPref(context.getString(R.string.AcquirerBank)).equals("PBB");

            if (isPBB) {
                hostNos = Arrays.asList(
                        eHostNo.PBB_Visa.getValue(),
                        eHostNo.PBB_Master.getValue(),
                        eHostNo.PBB_MCCS.getValue(),
                        eHostNo.PBB_Visa_DR.getValue(),
                        eHostNo.PBB_Master_DR.getValue()
                );
            } else {
                hostNos = Arrays.asList(
                        eHostNo.Visa_Master.getValue(),
                        eHostNo.Amex.getValue(),
                        eHostNo.MyDebit.getValue(),
                        eHostNo.China_UnionPay.getValue()
                );
            }

            for (Integer hostNo : hostNos) {
                Settlement(hostNo, oriPayload, false);
            }

            payload.IsSuccess = true;
        } catch (Exception e) {
            LogUtils.e(TAG, "Exception: " + e);
            payload.Value = e.getMessage();
            payload.IsSuccess = false;
        }

        Publish("Response/" + oriPayload.ClientId, new Gson().toJson(payload));
    }

    private void Settlement(int hostNo, Payload oriPayload, boolean publishResult) {
        Payload payload = new Payload();
        try {
            sonicInterface.Settlement(hostNo, callbackInterface);

            payload.IsSuccess = true;
        } catch (Exception e) {
            LogUtils.e(TAG, "Settlement " + + hostNo + " Exception: " + Log.getStackTraceString(e));
            payload.Value = e.getMessage();
            payload.IsSuccess = false;
        }

        if(publishResult) {
            payload.MessageId = oriPayload.MessageId;
            payload.ClientId = oriPayload.ClientId;
            payload.Command = oriPayload.Command;
            payload.SerialNo = new SharedPrefUI(context).ReadSharedPrefStr(context.getString(R.string.serial_number));

            Publish("Response/" + oriPayload.ClientId, new Gson().toJson(payload));
        }
    }

    private void SetMaintenanceMode(Payload oriPayload, boolean isEnable) {
        Payload payload = new Payload();
        try {
            payload.MessageId = oriPayload.MessageId;
            payload.ClientId = oriPayload.ClientId;
            payload.Command = oriPayload.Command;
            payload.SerialNo = new SharedPrefUI(context).ReadSharedPrefStr(context.getString(R.string.serial_number));

            boolean result = sonicInterface.SetMaintenanceMode(isEnable, callbackInterface);
            if(result){
                TCPGeneralMessage eventMsg = new TCPGeneralMessage();
                eventMsg.Command = isEnable ? "TriggerMaintenance" : "ClearMaintenance";

                Message message = handler.obtainMessage();
                message.obj = new Gson().toJson(eventMsg);
                handler.sendMessage(message);
            }
            payload.IsSuccess = result;
        } catch (Exception ex) {
            LogUtils.e(TAG, "SetMaintenanceMode Exception: " + ex);
            payload.Value = ex.getMessage();
            payload.IsSuccess = false;
        }

        Publish("Response/" + oriPayload.ClientId, new Gson().toJson(payload));
    }

    private void SPRunQuery(Payload oriPayload) {
        Payload payload = new Payload();
        try {
            queryPayload = oriPayload;
            boolean result = sonicInterface.ExecuteQuery(oriPayload.Value, callbackInterface);
            payload.MessageId = queryPayload.MessageId;
            payload.ClientId = queryPayload.ClientId;
            payload.Command = queryPayload.Command;
            payload.SerialNo = new SharedPrefUI(context).ReadSharedPrefStr(context.getString(R.string.serial_number));
            payload.Value = result ? "Query run successfully" : "Query failed to run";
            payload.IsSuccess = result;
        }
        catch (Exception ex) {
            LogUtils.e(TAG, "SPRunQuery Exception: " + ex);
            payload.Value = ex.getMessage();
            payload.IsSuccess = false;
        }

        if(!payload.IsSuccess)
            Publish("Response/" + queryPayload.ClientId, new Gson().toJson(payload));
    }

    private void SPReadSharedPref(Payload oriPayload) {
        Payload payload = new Payload();

        try {
            String value = sonicInterface.ReadSharedPref(oriPayload.Key);
            LogUtils.i(TAG, "SPReadSharePref Key=" + oriPayload.Key + ", Value=" + value);

            payload.MessageId = oriPayload.MessageId;
            payload.ClientId = oriPayload.ClientId;
            payload.Command = oriPayload.Command;
            payload.SerialNo = new SharedPrefUI(context).ReadSharedPrefStr(context.getString(R.string.serial_number));
            payload.Key = oriPayload.Key;
            payload.Value = !Objects.equals(value, "") ? value : "Invalid Shared Pref Key!";
            payload.IsSuccess = !Objects.equals(value, "");
        }
        catch (Exception ex) {
            LogUtils.e(TAG, "SPReadSharedPref Exception: " + Log.getStackTraceString(ex));
            payload.Value = ex.getMessage();
            payload.IsSuccess = false;
        }

        Publish("Response/" + oriPayload.ClientId, new Gson().toJson(payload));
    }

    private void SPWriteSharedPref(Payload oriPayload) {
        Payload payload = new Payload();

        try {
            payload.MessageId = oriPayload.MessageId;
            payload.ClientId = oriPayload.ClientId;
            payload.Command = oriPayload.Command;
            payload.SerialNo = new SharedPrefUI(context).ReadSharedPrefStr(context.getString(R.string.serial_number));
            payload.Key = oriPayload.Key;

            LogUtils.i(TAG, "SPWriteSharedPref Key=" + oriPayload.Key + ", Value=" + oriPayload.Value);
            sonicInterface.WriteSharedPref(oriPayload.Key, oriPayload.Value);

            String result = sonicInterface.ReadSharedPref(oriPayload.Key);

            payload.Value = !Objects.equals(result, "") ? result : "Invalid Shared Pref Key!";
            payload.IsSuccess = !Objects.equals(oriPayload.Value, "");;
        }
        catch (Exception ex) {
            LogUtils.e(TAG, "SPWriteSharedPref Exception: " + Log.getStackTraceString(ex));
            payload.Value = ex.getMessage();
            payload.IsSuccess = false;
        }

        Publish("Response/" + oriPayload.ClientId, new Gson().toJson(payload));
    }


    private void AppUpdate(Context context, String appId, String versionCode){
        Callback<List<AppUpdate.ApkInfo>> callback = new Callback<List<AppUpdate.ApkInfo>>() {
            @Override
            public void onResponse(Call<List<AppUpdate.ApkInfo>> call, Response<List<AppUpdate.ApkInfo>> response) {
                LogUtils.i(TAG, "GetAppUpdate Response: " + new Gson().toJson(response.body()));
                if(response.body() != null && response.body().size() > 0) {
                    //Get latest update
                    AppUpdate.ApkInfo apkInfo = response.body().get(response.body().size() - 1);

                    FileUtils.DownloadFiles(context, apkInfo.file);
                    FileUtils.InstallApk(apkInfo.file);
                }
            }

            @Override
            public void onFailure(Call<List<AppUpdate.ApkInfo>> call, Throwable t) {
                LogUtils.e(TAG, "GetAppUpdate onFailure: " + t.getMessage());
            }
        };
        FileUtils.GetAppUpdate(context, appId, versionCode, callback);
    }

    private int GetQueueCount() {
        try {
            if (sonicInterface != null)
                return sonicInterface.GetPendingTransactionCount();
        } catch (Exception ex) {
            LogUtils.e(TAG, "Exception: " + ex);
        }

        return -1;
    }

    private String GetTerminalStage() {
        try {
            if (sonicInterface != null) {
                GetStatusResult result = sonicInterface.getStatus();
                return result.TerminalState == eTerminalState.Idle ? "Online" : result.TerminalState.name();
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "Exception: " + e);
        }

        return "Online";
    }

    private void SPResetEMVConfigFile(Payload oriPayload) {
        Payload payload = new Payload();
        try {
            int result = sonicInterface.PerformAction("ResetEMVConfigFile", null);
            payload.MessageId = queryPayload.MessageId;
            payload.ClientId = queryPayload.ClientId;
            payload.Command = queryPayload.Command;
            payload.SerialNo = new SharedPrefUI(context).ReadSharedPrefStr(context.getString(R.string.serial_number));
            payload.Value = result == 1 ? "Reset successfully" : "Reset failed";
            payload.IsSuccess = result == 1;
        }
        catch (Exception ex) {
            LogUtils.e(TAG, "SPResetEMVConfigFile Exception: " + ex);
            payload.Value = ex.getMessage();
            payload.IsSuccess = false;
        }

        Publish("Response/" + oriPayload.ClientId, new Gson().toJson(payload));
    }

    public class Payload {
        public String MessageId;
        public String Command;
        public String ClientId;
        public String SerialNo;
        public String Key;
        public String Value;
        public boolean IsSuccess;
        public String DateFrom;
        public String DateTo;
        public boolean LogFileOnly;
    }

    private class Status {
        String MessageId;
        String SerialNo;
        String Command;
        int Severity;
        String Status;
        String SysTime;
        String IPAddress;
        List<AppVersion> AppVersion;
        String LastTran;
        int QueueCount;
    }

    private class AppVersion{
        String Name;
        String Version;
    }

    private class Result{
        String MessageId;
        String SerialNo;
        String Command;
        boolean IsSuccess;
        String Result;
    }
}
