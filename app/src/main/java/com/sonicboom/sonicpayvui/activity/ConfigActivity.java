package com.sonicboom.sonicpayvui.activity;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.DropDownPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.google.gson.Gson;
import com.sankuai.waimai.router.Router;
import com.sankuai.waimai.router.annotation.RouterUri;
import com.sbs.aidl.Class.FareBasedEntryResult;
import com.sbs.aidl.Class.FareBasedExitResult;
import com.sbs.aidl.Class.MaxChargedEntryResult;
import com.sbs.aidl.Class.MaxChargedExitResult;
import com.sbs.aidl.Class.ParkingEntryResult;
import com.sbs.aidl.Class.ParkingExitResult;
import com.sbs.aidl.Class.QRTransactionResult;
import com.sbs.aidl.Class.ReadCardResult;
import com.sbs.aidl.Class.RefundResult;
import com.sbs.aidl.Class.SalesCompletionResult;
import com.sbs.aidl.Class.SalesResult;
import com.sbs.aidl.Class.SettlementResult;
import com.sbs.aidl.Class.VoidResult;
import com.sbs.aidl.Class.eTerminalState;
import com.sbs.aidl.IAIDLCardCallbackInterface;
import com.sbs.aidl.IAIDLSonicpayInterface;
import com.sonicboom.sonicpayvui.R;
import com.sonicboom.sonicpayvui.RouterConst;
import com.sonicboom.sonicpayvui.SharedPrefUI;
import com.sonicboom.sonicpayvui.config.fragment.MenuFragment;
import com.sonicboom.sonicpayvui.utils.LogUtils;
import com.sonicboom.sonicpayvui.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Objects;

@RouterUri(path= {RouterConst.CONFIG_MAIN})
public class ConfigActivity extends AppCompatActivity {

    private static final String TAG = "ConfigActivity";

    public ProgressDialog pd;
    public IAIDLSonicpayInterface sonicInterface;

    private MenuItem pendingTxn;

    public final IAIDLCardCallbackInterface callbackInterface = new IAIDLCardCallbackInterface.Stub() {

        @Override
        public void ReadCardCallback(ReadCardResult result) throws RemoteException {

        }

        @Override
        public void ParkingEntryCallback(ParkingEntryResult result) throws RemoteException {

        }

        @Override
        public void ParkingExitCallback(ParkingExitResult result) throws RemoteException {

        }

        @Override
        public void SalesCallback(SalesResult result) throws RemoteException {

        }

        @Override
        public void StatusCallback(eTerminalState state, boolean CardPresent) throws RemoteException {
            LogUtils.i(TAG, "StatusCallback:" + state.getValue());
        }

        @Override
        public void SettlementCallback(SettlementResult[] result) throws RemoteException {
            LogUtils.i(TAG, "SettlementCallback:" + new Gson().toJson(result));

            //Toast.makeText(getApplicationContext(), result.StatusCode.equals(eTngStatusCode.No_Error.getCode()) || result.StatusCode.equals(eStatusCode.Approved.getValue()) ? "Settlement success" : "Settlement failed", Toast.LENGTH_LONG).show();
//            new AlertDialog.Builder(getApplicationContext())
//                    .setTitle("Result")
//                    .setMessage(result.StatusCode.equals(eTngStatusCode.No_Error.getCode()) || result.StatusCode.equals(eStatusCode.Approved.getValue()) ? "Settlement success" : "Settlement failed")
//                    .setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss())
//                    .create()
//                    .show();
        }

        @Override
        public void QRTransactionCallback(QRTransactionResult result) throws RemoteException {

        }

        @Override
        public void SalesP1Callback(SalesResult result) throws RemoteException {

        }

        @Override
        public void SalesP3Callback(SalesResult result) throws RemoteException {

        }

        @Override
        public void SalesP3DebtRecoveryCallback(SalesResult result, int orinSystemID) throws RemoteException {

        }

        @Override
        public void PreAuthCallback(SalesResult result) throws RemoteException {

        }

        @Override
        public void SalesCompletionCallback(SalesCompletionResult result) throws RemoteException {

        }

        @Override
        public void VoidCallback(VoidResult result) throws RemoteException {

        }

        @Override
        public void MaintenanceCallback(String result) throws RemoteException {
            LogUtils.i(TAG, "Maintenance Callback: " + result);
            try {
                Thread.sleep(2000);
                JSONObject resultObj = new JSONObject(result);
                int remainingCount = resultObj.getInt("PendingTransactionCount");
                LogUtils.i(TAG, "Maintenance Callback Remaining Txn: " + remainingCount);
                if(pendingTxn != null){
                    runOnUiThread(() -> {
                        pendingTxn.setTitle("PENDING TXN: \n" + remainingCount);
                    });
                }
            } catch (Exception e) {
                LogUtils.e(TAG, "MaintenanceCallback Exception: " + Log.getStackTraceString(e));
            }
        }

        @Override
        public void ExecuteQueryCallback(String result) throws RemoteException {

        }

        @Override
        public void RefundCallback(SalesResult result) throws RemoteException {

        }

        @Override
        public void FareBasedEntryCallback(FareBasedEntryResult result) throws RemoteException {

        }

        @Override
        public void FareBasedExitCallback(FareBasedExitResult result) throws RemoteException {

        }

        @Override
        public void MaxChargedEntryCallback(MaxChargedEntryResult result) throws RemoteException {

        }

        @Override
        public void MaxChargedExitCallback(MaxChargedExitResult result) throws RemoteException {

        }
    };

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtils.i(TAG, "onServiceConnected");
            sonicInterface = IAIDLSonicpayInterface.Stub.asInterface(service);
            try {
                boolean enterMaintenanceMode = sonicInterface.SetMaintenanceMode(true, callbackInterface);
                if(!enterMaintenanceMode){
                    onKeyBackDown();
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogUtils.i(TAG, "onServiceConnected");
            sonicInterface = null;
            //Toast.makeText(getApplicationContext(), "SonicPay5 service disconnected", Toast.LENGTH_LONG).show();
            unbindService(serviceConnection);
            ConnectService();

            int i = 2;
            while(i > 0) {
                try {
                    Thread.sleep(1000);
                    if(!isServiceRunning())
                        ConnectService();
                    i--;
                } catch (Exception e) {
                    LogUtils.e(TAG, "onServiceDisconnected Exception: " + Log.getStackTraceString(e));
                }
            }
            Router.startUri(getApplicationContext(), RouterConst.CONFIG_LOGIN);
            finish();
        }
    };

    private boolean isServiceRunning() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServices = activityManager.getRunningServices(Integer.MAX_VALUE);

        for (ActivityManager.RunningServiceInfo serviceInfo : runningServices) {
            if ((serviceInfo.service.getClassName()).equals(Utils.getServiceClassName())) {
                LogUtils.i(TAG, "IsServiceRunning: true" );
                return true;
            }
        }
        LogUtils.i(TAG, "IsServiceRunning: false" );
        return false;
    }

    public void bindPreferenceValues(PreferenceScreen preferenceScreen) {
        try {
            int preferenceCount = preferenceScreen.getPreferenceCount();

            for (int i = 0; i < preferenceCount; i++) {
                Preference preference = preferenceScreen.getPreference(i);
                if ((preference instanceof PreferenceCategory)) {
                    for(int j=0; j < ((PreferenceCategory) preference).getPreferenceCount(); j++){
                        Preference preferenceItem = ((PreferenceCategory) preference).getPreference(j);
                        String preferenceKey = preferenceItem.getKey();

                        if(preferenceItem instanceof EditTextPreference){
                            String preferenceValue = sonicInterface.ReadSharedPref(preferenceKey);

                            //if(!preferenceValue.equals(""))
                                new SharedPrefUI(this).WriteSharedPrefStr(preferenceKey, preferenceValue);
                        }
                        else if (preferenceItem instanceof DropDownPreference){
                            String preferenceValue = sonicInterface.ReadSharedPref(preferenceKey);
                            if(!preferenceValue.equals(""))
                                new SharedPrefUI(this).WriteSharedPrefStr(preferenceKey, preferenceValue);
                            else
                                sonicInterface.WriteSharedPref(preferenceKey, Objects.requireNonNull(preferenceItem.getSharedPreferences()).getString(preferenceKey, ""));
                        }
                        else{
                            boolean preferenceValue = sonicInterface.ReadSharedPrefBoolean(preferenceKey);
                            new SharedPrefUI(this).WriteSharedPrefBoolean(preferenceKey, (boolean) preferenceValue);
                        }
                    }
                }
                else{
                    String preferenceKey = preference.getKey();
                    if(preference instanceof EditTextPreference){
                        String preferenceValue = sonicInterface.ReadSharedPref(preferenceKey);
                        //if(!preferenceValue.equals(""))
                        new SharedPrefUI(this).WriteSharedPrefStr(preferenceKey, preferenceValue);
                    }
                    else if (preference instanceof DropDownPreference){
                        String preferenceValue = sonicInterface.ReadSharedPref(preferenceKey);
                        if(!preferenceValue.equals(""))
                            new SharedPrefUI(this).WriteSharedPrefStr(preferenceKey, preferenceValue);
                        else
                            sonicInterface.WriteSharedPref(preferenceKey, Objects.requireNonNull(preference.getSharedPreferences()).getString(preferenceKey, ""));
                    }
                    else{
                        boolean preferenceValue = sonicInterface.ReadSharedPrefBoolean(preferenceKey);
                        new SharedPrefUI(this).WriteSharedPrefBoolean(preferenceKey, preferenceValue);
                    }
                }
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "Exception: " + Log.getStackTraceString(e));
        }
    }

    public void setPreferenceListener(PreferenceScreen preferenceScreen) {
        try {
            int preferenceCount = preferenceScreen.getPreferenceCount();

            for (int i = 0; i < preferenceCount; i++) {
                Preference preference = preferenceScreen.getPreference(i);
                if ((preference instanceof PreferenceCategory)) {
                    for(int j=0; j < ((PreferenceCategory) preference).getPreferenceCount(); j++){

                        ((PreferenceCategory) preference).getPreference(j).setOnPreferenceChangeListener((preference1, newValue) -> {
                            try {
                                if(preference1 instanceof EditTextPreference || preference1 instanceof DropDownPreference)
                                    sonicInterface.WriteSharedPref(preference1.getKey(), newValue.toString());
                                if(preference1 instanceof SwitchPreference)
                                    sonicInterface.WriteSharedPrefBoolean(preference1.getKey(), (Boolean) newValue);
                            } catch (RemoteException e) {
                                LogUtils.e(TAG, "Exception: " + Log.getStackTraceString(e));
                            }
                            return true;
                        });

                    }
                }
                else{
                    preference.setOnPreferenceChangeListener((preference1, newValue) -> {
                        try {
                            if(preference1 instanceof EditTextPreference || preference1 instanceof DropDownPreference)
                                sonicInterface.WriteSharedPref(preference1.getKey(), newValue.toString());
                            if(preference1 instanceof SwitchPreference)
                                sonicInterface.WriteSharedPrefBoolean(preference1.getKey(), (Boolean) newValue);
                        } catch (RemoteException e) {
                            LogUtils.e(TAG, "Exception: " + Log.getStackTraceString(e));
                        }
                        return true;
                    });
                }
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "Exception: " + Log.getStackTraceString(e));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogUtils.d(TAG, "onCreate started...");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        Toolbar toolbar = findViewById(R.id.config_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.config_container, new MenuFragment())
                .commit();

        ConnectService();

        pd = new ProgressDialog(this);
        LogUtils.d(TAG, "onCreate ended.");
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        unbindService(serviceConnection);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onKeyBackDown();
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.maintenance_actionbar, menu);
        pendingTxn = menu.findItem(R.id.pending_txn_count);
        return true;
    }


    public void UpdateTitle(String title){
        Toolbar toolbar = (Toolbar) findViewById(R.id.config_toolbar);
        toolbar.setTitle(title);
    }

    protected void onKeyBackDown() {
        super.onBackPressed();
    }

    private void ConnectService(){
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(Utils.getServiceAppCode(), Utils.getServiceClassName()));
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }
}