package com.sonicboom.sonicpayvui;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.pax.dal.IDAL;
import com.pax.dal.entity.ETermInfoKey;
import com.pax.neptunelite.api.NeptuneLiteUser;
import com.sonicboom.sonicpayvui.utils.AppLogger;
import com.sonicboom.sonicpayvui.utils.LogUtils;
import com.sonicboom.sonicpayvui.utils.Utils;

import java.util.Map;

public class App extends BaseApplication{
    private static final String TAG = "App";
    public static IDAL dal = null;

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init() throws Exception {
        dal = NeptuneLiteUser.getInstance().getDal(getApplicationContext());

        // init file logging
        String path = getFilesDir().getPath();
        LogUtils.init(path);

        SharedPrefUI sp = new SharedPrefUI(this);
        if (!sp.Contains(getString(R.string.LogFileRetentionDay)))
            new SharedPrefUI(this).WriteSharedPrefInt(getString(R.string.LogFileRetentionDay), 31);

        new Thread(() -> {
            try {
                int retentionDay = sp.ReadSharedPrefInt(getString(R.string.LogFileRetentionDay));
                AppLogger.PurgeLogFile(getApplicationContext().getFilesDir().getPath(), retentionDay);
            } catch (Exception e) {
                LogUtils.e(TAG, "Purge LogFile Error: " + e.getMessage());
            }
        }).start();

        //Serial Number
        Map<ETermInfoKey, String> termInfo = dal.getSys().getTermInfo();
        for (ETermInfoKey key : ETermInfoKey.values()) {
            if(key.name().equals("SN")) {
                LogUtils.i(TAG,key.name() + ": " + termInfo.get(key));
                new SharedPrefUI(this).WriteSharedPrefStr(getString(R.string.serial_number),termInfo.get(key));
            }
            if(key.name().equals("AP_VER")){
                LogUtils.i(TAG,key.name() + ": " + termInfo.get(key));
                new SharedPrefUI(this).WriteSharedPrefStr(getString(R.string.firmware_version),termInfo.get(key));
            }
        }

        //Device local IP
        String ip = Utils.getIPAddress(true);
        LogUtils.i(TAG, "IP: " + ip);
        new SharedPrefUI(this).WriteSharedPrefStr(getString(R.string.ip_address),ip);

        //UI App Version
        String version = getVersion();
        LogUtils.i(TAG, "UI App Version: " + version);
        new SharedPrefUI(this).WriteSharedPrefStr(getString(R.string.ui_app_version), version);

        //Navigation Bar
        dal.getSys().enableNavigationBar(new SharedPrefUI(this).ReadSharedPrefBoolean(getString(R.string.enable_nav_bar)));

        //Status Bar
        dal.getSys().enableStatusBar(new SharedPrefUI(this).ReadSharedPrefBoolean(getString(R.string.enable_status_bar)));
    }

    private String getVersion() {
        try {
            PackageManager manager = getPackageManager();
            PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
            return info.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "1.0.0";
    }
}
