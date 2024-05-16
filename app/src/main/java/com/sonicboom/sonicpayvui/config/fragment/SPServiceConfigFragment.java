package com.sonicboom.sonicpayvui.config.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.sonicboom.sonicpayvui.CheckboxListAdapter;
import com.sonicboom.sonicpayvui.MainActivity;
import com.sonicboom.sonicpayvui.MyPreferenceFragmentCompat;
import com.sonicboom.sonicpayvui.R;
import com.sonicboom.sonicpayvui.SharedPrefUI;
import com.sonicboom.sonicpayvui.activity.ConfigActivity;
import com.sonicboom.sonicpayvui.models.eHostNo;
import com.sonicboom.sonicpayvui.utils.FileUtils;
import com.sonicboom.sonicpayvui.utils.LogUtils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SPServiceConfigFragment extends PreferenceFragmentCompat {
    private static final String TAG = "SPServiceConfigFragment";

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.sp_service_menu, rootKey);
    }

    @Override
    public void onResume(){
        super.onResume();
        ((ConfigActivity)requireActivity()).UpdateTitle("SonicPay Service");
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    public static class TerminalFragment extends MyPreferenceFragmentCompat{

        @Override
        public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
            setPreferencesFromResource(R.xml.sp_service_terminal_preferences, rootKey);
            ((ConfigActivity)requireActivity()).UpdateTitle("Terminal");
        }
    }

    public static class NetworkFragment extends MyPreferenceFragmentCompat{
        private static String TAG = "Network";
        @Override
        public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
            setPreferencesFromResource(R.xml.sp_service_network_preferences, rootKey);
            ((ConfigActivity)requireActivity()).UpdateTitle("Network");

            try{
                String acquirerBank = ((ConfigActivity) requireActivity()).sonicInterface.ReadSharedPref(getString(R.string.AcquirerBank));
                PreferenceCategory cBankHost = findPreference("cBankHost");
                PreferenceCategory cMasterTerminal = findPreference("cMasterTerminal");

                if(!acquirerBank.equals("PBB")) {
                    assert cMasterTerminal != null;
                    getPreferenceScreen().removePreference(cMasterTerminal);
                }
                if(!acquirerBank.equals("MGATE")){
                    EditTextPreference SecondaryBankHostIP = findPreference(getString(R.string.SecondaryBankHostIP));
                    assert SecondaryBankHostIP != null;
                    cBankHost.removePreference(SecondaryBankHostIP);

                    EditTextPreference BankHostPort2 = findPreference(getString(R.string.BankHostPort2));
                    assert BankHostPort2 != null;
                    cBankHost.removePreference(BankHostPort2);
                }
                else{
                    EditTextPreference BankHostSSLCertname = findPreference(getString(R.string.BankHostSSLCertname));
                    assert BankHostSSLCertname != null;
                    cBankHost.removePreference(BankHostSSLCertname);
                }

            }
            catch(Exception ex){
                LogUtils.e(TAG, "Exception: " + Log.getStackTraceString(ex));
            }
        }


    }

    public static class ConfigurationFragment extends MyPreferenceFragmentCompat{

        @Override
        public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
            setPreferencesFromResource(R.xml.sp_service_configuration_preferences, rootKey);
            ((ConfigActivity)requireActivity()).UpdateTitle("Configuration");
        }
    }

    public static class TNGSettingFragment extends MyPreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
            setPreferencesFromResource(R.xml.sp_service_tng_setting_preference, rootKey);
            ((ConfigActivity)requireActivity()).UpdateTitle("TouchNGo Setting");
            List<String> list = FileUtils.GetTngParamFiles();

            // Get the preference screen
            PreferenceScreen preferenceScreen = getPreferenceScreen();

            // Create a preference for each item in the list
            for (String item : list) {
                Preference preference = new Preference(requireActivity());
                preference.setTitle(item);

                // Add the preference to the screen
                preferenceScreen.addPreference(preference);
            }
        }
    }

    public static class QRSettingFragment extends MyPreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
            setPreferencesFromResource(R.xml.sp_service_qr_setting_preferences, rootKey);
            ((ConfigActivity)requireActivity()).UpdateTitle("QR Setting");
        }
    }

    public static class OperationFragment extends PreferenceFragmentCompat{

        private static final String TAG = "Operation";
        private MaterialDatePicker<Pair<Long, Long>> datePicker;

        @Override
        public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
            setPreferencesFromResource(R.xml.sp_service_operation_preferences, rootKey);
            try {
                PreferenceCategory categorySettlement = findPreference("cSettlement");
                assert categorySettlement != null;

                String acquirerBank = ((ConfigActivity) requireActivity()).sonicInterface.ReadSharedPref(getString(R.string.AcquirerBank));
                if(acquirerBank != null && (acquirerBank.equals("MBB") || acquirerBank.equals("HLB"))){
                    PreferenceCategory categoryKeyLoading = findPreference("cKeyLoading");
                    assert categoryKeyLoading != null;
                    getPreferenceScreen().removePreference(categoryKeyLoading);

                    PreferenceCategory categoryRemoteKey = findPreference("cRemoteKey");
                    assert categoryRemoteKey != null;

                    Preference mGateInitialKey = findPreference(getString(R.string.RKLMGATE));
                    assert mGateInitialKey != null;
                    categoryRemoteKey.removePreference(mGateInitialKey);

                    if(acquirerBank.equals("HLB")){
                        Preference SettlementAmex = findPreference(getString(R.string.SettlementAmex));
                        assert SettlementAmex != null;
                        categorySettlement.removePreference(SettlementAmex);
                    }
                    Preference SettlementVisa = findPreference(getString(R.string.SettlementVisa));
                    assert SettlementVisa != null;
                    categorySettlement.removePreference(SettlementVisa);
                    Preference SettlementMaster = findPreference(getString(R.string.SettlementMaster));
                    assert SettlementMaster != null;
                    categorySettlement.removePreference(SettlementMaster);
                    Preference SettlementVisaDR = findPreference(getString(R.string.SettlementVisaDR));
                    assert SettlementVisaDR != null;
                    categorySettlement.removePreference(SettlementVisaDR);
                    Preference SettlementMasterDR = findPreference(getString(R.string.SettlementMasterDR));
                    assert SettlementMasterDR != null;
                    categorySettlement.removePreference(SettlementMasterDR);
                }
                if(acquirerBank != null && acquirerBank.equals("PBB")){
                    PreferenceCategory categoryRemoteKey = findPreference("cRemoteKey");
                    assert categoryRemoteKey != null;
                    getPreferenceScreen().removePreference(categoryRemoteKey);

                    Preference SettlementVisaMaster = findPreference(getString(R.string.SettlementVisaMaster));
                    assert SettlementVisaMaster != null;
                    categorySettlement.removePreference(SettlementVisaMaster);
                    Preference SettlementAmex = findPreference(getString(R.string.SettlementAmex));
                    assert SettlementAmex != null;
                    categorySettlement.removePreference(SettlementAmex);
                    Preference SettlementUPI = findPreference(getString(R.string.SettlementUPI));
                    assert SettlementUPI != null;
                    categorySettlement.removePreference(SettlementUPI);
                }
                if(acquirerBank != null && acquirerBank.equals("MGATE")){
                    PreferenceCategory categoryKeyLoading = findPreference("cKeyLoading");
                    assert categoryKeyLoading != null;
                    getPreferenceScreen().removePreference(categoryKeyLoading);

                    PreferenceCategory categoryRemoteKey = findPreference("cRemoteKey");
                    assert categoryRemoteKey != null;

                    Preference RKLVisaMaster = findPreference(getString(R.string.RKLVisaMaster));
                    assert RKLVisaMaster != null;
                    categoryRemoteKey.removePreference(RKLVisaMaster);

                    Preference RKLAmex = findPreference(getString(R.string.RKLAmex));
                    assert RKLAmex != null;
                    categoryRemoteKey.removePreference(RKLAmex);

                    Preference RKLMCCS = findPreference(getString(R.string.RKLMCCS));
                    assert RKLMCCS != null;
                    categoryRemoteKey.removePreference(RKLMCCS);

                    Preference RKLUP = findPreference(getString(R.string.RKLUP));
                    assert RKLUP != null;
                    categoryRemoteKey.removePreference(RKLUP);

                    getPreferenceScreen().removePreference(categorySettlement);
                }

            } catch (Exception e) {
                LogUtils.e(TAG, "Exception: " + Log.getStackTraceString(e));
            }
        }

        @Override
        public void onResume(){
            super.onResume();
            ((ConfigActivity)requireActivity()).UpdateTitle("Operation");
        }

        @SuppressLint("RestrictedApi")
        @Override
        public boolean onPreferenceTreeClick(@NonNull Preference preference) {
            try {
                if (preference.getFragment() != null) {
                    boolean handled = false;
                    if (getCallbackFragment() instanceof OnPreferenceStartFragmentCallback) {
                        handled = ((OnPreferenceStartFragmentCallback) getCallbackFragment())
                                .onPreferenceStartFragment(this, preference);
                    }
                    //  If the callback fragment doesn't handle OnPreferenceStartFragmentCallback, looks up
                    //  its parent fragment in the hierarchy that implements the callback until the first
                    //  one that returns true
                    Fragment callbackFragment = this;
                    while (!handled && callbackFragment != null) {
                        if (callbackFragment instanceof OnPreferenceStartFragmentCallback) {
                            handled = ((OnPreferenceStartFragmentCallback) callbackFragment)
                                    .onPreferenceStartFragment(this, preference);
                        }
                        callbackFragment = callbackFragment.getParentFragment();
                    }
                    if (!handled && getContext() instanceof OnPreferenceStartFragmentCallback) {
                        handled = ((OnPreferenceStartFragmentCallback) getContext())
                                .onPreferenceStartFragment(this, preference);
                    }
                    // Check the Activity as well in case getContext was overridden to return something
                    // other than the Activity.
                    if (!handled && getActivity() instanceof OnPreferenceStartFragmentCallback) {
                        handled = ((OnPreferenceStartFragmentCallback) getActivity())
                                .onPreferenceStartFragment(this, preference);
                    }
                    if (!handled) {
                        Log.w(TAG,
                                "onPreferenceStartFragment is not implemented in the parent activity - "
                                        + "attempting to use a fallback implementation. You should "
                                        + "implement this method so that you can configure the new "
                                        + "fragment that will be displayed, and set a transition between "
                                        + "the fragments.");
                        final FragmentManager fragmentManager = getParentFragmentManager();
                        final Bundle args = preference.getExtras();
                        final Fragment fragment = fragmentManager.getFragmentFactory().instantiate(
                                requireActivity().getClassLoader(), preference.getFragment());
                        fragment.setArguments(args);
                        fragment.setTargetFragment(this, 0);
                        fragmentManager.beginTransaction()
                                // Attempt to replace this fragment in its root view - developers should
                                // implement onPreferenceStartFragment in their activity so that they can
                                // customize this behaviour and handle any transitions between fragments
                                .replace(((View) requireView().getParent()).getId(), fragment)
                                .addToBackStack(null)
                                .commit();
                    }
                }

                if(preference.getKey().equals(getResources().getString(R.string.DownloadSettings))){
                    ((ConfigActivity)requireActivity()).pd.setMessage("Downloading...");
                    ((ConfigActivity)requireActivity()).pd.show();
                    new DownloadSettings(((ConfigActivity)requireActivity()), ((ConfigActivity)requireActivity()).pd).execute();
                }

                if(preference.getKey().equals(getResources().getString(R.string.DownloadCAPK))){
                    ((ConfigActivity)requireActivity()).pd.setMessage("Downloading...");
                    ((ConfigActivity)requireActivity()).pd.show();
                    new DownloadCAPK(((ConfigActivity)requireActivity()), ((ConfigActivity)requireActivity()).pd).execute();
                    //new DownloadSettings(((ConfigActivity)requireActivity()), ((ConfigActivity)requireActivity()).pd).execute();
                }

                if(preference.getKey().equals(getResources().getString(R.string.UploadData))){
                    ((ConfigActivity)requireActivity()).pd.setMessage("Uploading...");
                    ((ConfigActivity)requireActivity()).pd.show();
                    new UploadData(((ConfigActivity)requireActivity()), ((ConfigActivity)requireActivity()).pd).execute();
                }

                if(preference.getKey().equals(getString(R.string.UploadFile))){
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    Calendar calendar = Calendar.getInstance();

                    // Set the range of dates you want to allow selection
                    CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
                    constraintsBuilder.setEnd(calendar.getTimeInMillis());
                    calendar.add(Calendar.DAY_OF_YEAR, -21);
                    constraintsBuilder.setStart(calendar.getTimeInMillis());

                    datePicker = MaterialDatePicker.Builder.dateRangePicker()
                            .setCalendarConstraints(constraintsBuilder.build())
                            .build();

                    datePicker.addOnPositiveButtonClickListener(dialog -> {
                        try {
                            if (datePicker.getSelection() != null) {
                                // Get the selected date range
                                String startDate = dateFormat.format(datePicker.getSelection().first);
                                String endDate = dateFormat.format(datePicker.getSelection().second);

                                // option for upload Log File Only or with Database File
                                AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
                                builder.setTitle("Upload Option");

                                final boolean[] logFileOnly = {false};
                                final CharSequence[] items = { " All ", " Log File Only "};
                                builder.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        logFileOnly[0] = i == 1;
                                    }
                                });

                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        try {
                                            boolean result = ((ConfigActivity) requireActivity()).sonicInterface.FileUpload(startDate, endDate, logFileOnly[0]);
                                            new AlertDialog.Builder(requireActivity())
                                                    .setTitle("File upload")
                                                    .setMessage(result ? "File uploaded successfully" : "File upload failed")
                                                    .setIcon(result ? R.drawable.check_leaf_shadow : R.drawable.cross_fat)
                                                    .setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss())
                                                    .create()
                                                    .show();

                                        } catch (RemoteException e) {
                                            LogUtils.e(TAG, "File Upload exception: " + Log.getStackTraceString(e));
                                        }
                                    }
                                });

//                                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        // Handle Cancel button click
//                                        dialog.dismiss();
//                                    }
//                                });

                                AlertDialog fileUploadOptionDialog = builder.create();
                                fileUploadOptionDialog.show();
                            }
                        } catch (Exception e) {
                            LogUtils.e(TAG, "File Upload exception: " + Log.getStackTraceString(e));
                        }
                        datePicker = null;
                    });

                    datePicker.show(requireActivity().getSupportFragmentManager(), "datePicker");
                }

                if(preference.getKey().equals(getResources().getString(R.string.PBBKL)))
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .replace(R.id.config_container, KeyLoadingFragment.class, null)
                            .addToBackStack(null)
                            .commit();

                if(preference.getKey().equals(getString(R.string.RKLVisaMaster))){
                    ((ConfigActivity)requireActivity()).pd.setMessage("Loading...");
                    ((ConfigActivity)requireActivity()).pd.show();
                    new RemoteKeyLoading(((ConfigActivity)requireActivity()), ((ConfigActivity)requireActivity()).pd).execute(eHostNo.Visa_Master.getValue());
                }

                if(preference.getKey().equals(getString(R.string.RKLAmex))){
                    ((ConfigActivity)requireActivity()).pd.setMessage("Loading...");
                    ((ConfigActivity)requireActivity()).pd.show();
                    new RemoteKeyLoading(((ConfigActivity)requireActivity()), ((ConfigActivity)requireActivity()).pd).execute(eHostNo.Amex.getValue());
                }

                if(preference.getKey().equals(getString(R.string.RKLMCCS))){
                    ((ConfigActivity)requireActivity()).pd.setMessage("Loading...");
                    ((ConfigActivity)requireActivity()).pd.show();
                    new RemoteKeyLoading(((ConfigActivity)requireActivity()), ((ConfigActivity)requireActivity()).pd).execute(eHostNo.MyDebit.getValue());
                }

                if(preference.getKey().equals(getString(R.string.RKLUP))){
                    ((ConfigActivity)requireActivity()).pd.setMessage("Loading...");
                    ((ConfigActivity)requireActivity()).pd.show();
                    new RemoteKeyLoading(((ConfigActivity)requireActivity()), ((ConfigActivity)requireActivity()).pd).execute(eHostNo.China_UnionPay.getValue());
                }

                if(preference.getKey().equals(getString(R.string.RKLMGATE))){
                    ((ConfigActivity)requireActivity()).pd.setMessage("Loading...");
                    ((ConfigActivity)requireActivity()).pd.show();
                    new RemoteKeyLoading(((ConfigActivity)requireActivity()), ((ConfigActivity)requireActivity()).pd).execute(eHostNo.Visa_Master.getValue());
                }

                if(preference.getKey().equals(getString(R.string.SettlementVisaMaster))){
                    ((ConfigActivity)requireActivity()).pd.setMessage("Uploading...");
                    ((ConfigActivity)requireActivity()).pd.show();
                    new Settlement(((ConfigActivity)requireActivity()), ((ConfigActivity)requireActivity()).pd).execute(eHostNo.Visa_Master.getValue());
                }

                if(preference.getKey().equals(getResources().getString(R.string.SettlementVisa))){
                    ((ConfigActivity)requireActivity()).pd.setMessage("Uploading...");
                    ((ConfigActivity)requireActivity()).pd.show();
                    new Settlement(((ConfigActivity)requireActivity()), ((ConfigActivity)requireActivity()).pd).execute(eHostNo.PBB_Visa.getValue());
//                    new Thread(()->{
//                        try {
//                            ((ConfigActivity)requireActivity()).sonicInterface.Settlement(eHostNo.TouchNGo.getValue(), ((ConfigActivity) requireActivity()).callbackInterface);
//                        } catch (RemoteException e) {
//                            e.printStackTrace();
//                        }
//                    }).start();
//                    //((ConfigActivity)requireActivity()).sonicInterface.Settlement(eHostNo.TouchNGo.getValue(), ((ConfigActivity) requireActivity()).callbackInterface);
//                    ((ConfigActivity) requireActivity()).pd.setMessage("Settlement uploading...");
//                    ((ConfigActivity) requireActivity()).pd.show();
                }

                if(preference.getKey().equals(getString(R.string.SettlementMaster))){
                    ((ConfigActivity)requireActivity()).pd.setMessage("Uploading...");
                    ((ConfigActivity)requireActivity()).pd.show();
                    new Settlement(((ConfigActivity)requireActivity()), ((ConfigActivity)requireActivity()).pd).execute(eHostNo.PBB_Master.getValue());
                }

                if(preference.getKey().equals(getString(R.string.SettlementAmex))){
                    ((ConfigActivity)requireActivity()).pd.setMessage("Uploading...");
                    ((ConfigActivity)requireActivity()).pd.show();
                    new Settlement(((ConfigActivity)requireActivity()), ((ConfigActivity)requireActivity()).pd).execute(eHostNo.Amex.getValue());
                }

                if(preference.getKey().equals(getString(R.string.SettlementUPI))){
                    ((ConfigActivity)requireActivity()).pd.setMessage("Uploading...");
                    ((ConfigActivity)requireActivity()).pd.show();
                    new Settlement(((ConfigActivity)requireActivity()), ((ConfigActivity)requireActivity()).pd).execute(eHostNo.China_UnionPay.getValue());
                }

                if(preference.getKey().equals(getString(R.string.SettlementMCCS))){
                    ((ConfigActivity)requireActivity()).pd.setMessage("Uploading...");
                    ((ConfigActivity)requireActivity()).pd.show();
                    boolean isPBB = ((ConfigActivity) requireActivity()).sonicInterface.ReadSharedPref(getString(R.string.AcquirerBank)).equals("PBB");
                    new Settlement(((ConfigActivity)requireActivity()), ((ConfigActivity)requireActivity()).pd).execute(isPBB ? eHostNo.PBB_MCCS.getValue(): eHostNo.MyDebit.getValue());
                }

                if(preference.getKey().equals(getString(R.string.SettlementVisaDR))){
                    ((ConfigActivity)requireActivity()).pd.setMessage("Uploading...");
                    ((ConfigActivity)requireActivity()).pd.show();
                    new Settlement(((ConfigActivity)requireActivity()), ((ConfigActivity)requireActivity()).pd).execute(eHostNo.PBB_Visa_DR.getValue());
                }

                if(preference.getKey().equals(getString(R.string.SettlementMasterDR))){
                    ((ConfigActivity)requireActivity()).pd.setMessage("Uploading...");
                    ((ConfigActivity)requireActivity()).pd.show();
                    new Settlement(((ConfigActivity)requireActivity()), ((ConfigActivity)requireActivity()).pd).execute(eHostNo.PBB_Master_DR.getValue());
                }

                if(preference.getKey().equals(getString(R.string.SettlementTNG))){
                    ((ConfigActivity)requireActivity()).pd.setMessage("Uploading...");
                    ((ConfigActivity)requireActivity()).pd.show();
                    new Settlement(((ConfigActivity)requireActivity()), ((ConfigActivity)requireActivity()).pd).execute(eHostNo.TouchNGo.getValue());
                }

                if(preference.getKey().equals(getString(R.string.TNGDownloadParam))){
                    ((ConfigActivity)requireActivity()).pd.setMessage("Downloading...");
                    ((ConfigActivity)requireActivity()).pd.show();
                    new DownloadParam(((ConfigActivity)requireActivity()), ((ConfigActivity)requireActivity()).pd).execute();
                }

                if(preference.getKey().equals(getString(R.string.TNGRemoveParam))){
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                    builder.setTitle("Delete TNG Param");
                    builder.setMessage("Confirm to proceed deletion?");

                    // Positive button (OK action)
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Log.i(TAG, "Delete TNG Param");
                            // Perform the desired action when "Yes" is clicked
                            // For example, you can call a method or execute some code
                            getActivity().runOnUiThread(()-> {
                                boolean result= false;
                                File fileToDelete = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/TNGParam");
                                Log.i(TAG, "Delete TNG Param " + Environment.getExternalStorageDirectory().getAbsolutePath() + "/TNGParam");
                                if(fileToDelete.exists()){
                                    if (fileToDelete.isDirectory()) {
                                        for (File child : fileToDelete.listFiles()) {
                                            // Recursively delete the contents of the directory
                                            Log.i(TAG, "Delete TNG Param: " + child.getName());
                                            child.delete();
                                        }
                                    }
                                    result =fileToDelete.delete();
                                }
                                Log.i(TAG, "Delete TNG Param " + result);
                                new AlertDialog.Builder(getContext())
                                        .setTitle("Delete Param File")
                                        .setMessage(result ? "success" : "failed")
                                        .setIcon(result ? R.drawable.check_leaf_shadow : R.drawable.cross_fat)
                                        .setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss())
                                        .create()
                                        .show();
                            });
                        }
                    });

                    // Negative button (Cancel action)
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Perform any action you want when "No" is clicked, or leave it empty
                            dialog.dismiss(); // Dismiss the dialog
                        }
                    });

                    getActivity().runOnUiThread(()->{
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    });
                    // Create and show the dialog
	            }

            } catch (Exception e) {
                LogUtils.e(TAG, "Exception: " + Log.getStackTraceString(e));
            }
            return true;
        }
    }

    public static class AdvancedOperationFragment extends PreferenceFragmentCompat{

        private static final String TAG = "AdvancedOperation";
        @Override
        public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
            setPreferencesFromResource(R.xml.sp_service_advanced_operation_preferences, rootKey);
            ((ConfigActivity)requireActivity()).UpdateTitle("Advanced Operation");
        }

        @SuppressLint("RestrictedApi")
        @Override
        public boolean onPreferenceTreeClick(@NonNull Preference preference) {
            try {
                if (preference.getFragment() != null) {
                    boolean handled = false;
                    if (getCallbackFragment() instanceof OnPreferenceStartFragmentCallback) {
                        handled = ((OnPreferenceStartFragmentCallback) getCallbackFragment())
                                .onPreferenceStartFragment(this, preference);
                    }
                    //  If the callback fragment doesn't handle OnPreferenceStartFragmentCallback, looks up
                    //  its parent fragment in the hierarchy that implements the callback until the first
                    //  one that returns true
                    Fragment callbackFragment = this;
                    while (!handled && callbackFragment != null) {
                        if (callbackFragment instanceof OnPreferenceStartFragmentCallback) {
                            handled = ((OnPreferenceStartFragmentCallback) callbackFragment)
                                    .onPreferenceStartFragment(this, preference);
                        }
                        callbackFragment = callbackFragment.getParentFragment();
                    }
                    if (!handled && getContext() instanceof OnPreferenceStartFragmentCallback) {
                        handled = ((OnPreferenceStartFragmentCallback) getContext())
                                .onPreferenceStartFragment(this, preference);
                    }
                    // Check the Activity as well in case getContext was overridden to return something
                    // other than the Activity.
                    if (!handled && getActivity() instanceof OnPreferenceStartFragmentCallback) {
                        handled = ((OnPreferenceStartFragmentCallback) getActivity())
                                .onPreferenceStartFragment(this, preference);
                    }
                    if (!handled) {
                        Log.w(TAG,
                                "onPreferenceStartFragment is not implemented in the parent activity - "
                                        + "attempting to use a fallback implementation. You should "
                                        + "implement this method so that you can configure the new "
                                        + "fragment that will be displayed, and set a transition between "
                                        + "the fragments.");
                        final FragmentManager fragmentManager = getParentFragmentManager();
                        final Bundle args = preference.getExtras();
                        final Fragment fragment = fragmentManager.getFragmentFactory().instantiate(
                                requireActivity().getClassLoader(), preference.getFragment());
                        fragment.setArguments(args);
                        fragment.setTargetFragment(this, 0);
                        fragmentManager.beginTransaction()
                                // Attempt to replace this fragment in its root view - developers should
                                // implement onPreferenceStartFragment in their activity so that they can
                                // customize this behaviour and handle any transitions between fragments
                                .replace(((View) requireView().getParent()).getId(), fragment)
                                .addToBackStack(null)
                                .commit();
                    }
                }

                if(preference.getKey().equals(getResources().getString(R.string.RenewTEPin))) {

                    LayoutInflater layoutInflater = LayoutInflater.from(this.getContext());
                    View promptView = layoutInflater.inflate(R.layout.input_dialog, null);
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this.getContext());
                    alertDialogBuilder.setView(promptView);

                    TextView titleText = (TextView) promptView.findViewById(R.id.input_dialog_title);
                    final EditText editText = (EditText) promptView.findViewById(R.id.input_dialog_text);
                    TextView messageText = (TextView) promptView.findViewById(R.id.input_dialog_message);
                    titleText.setText("New TE PIN");
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                    editText.setHint("Enter new PIN here...");
                    messageText.setText("");
                    // setup a dialog window
                    alertDialogBuilder.setCancelable(false)
                            .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
//                                    try {
//                                        String newPIN = editText.getText().toString();
//
//                                        if (newPIN.equalsIgnoreCase("") || newPIN.length() != 8) {
//                                            messageText.setTextColor(Color.RED);
//                                            messageText.setText("PIN must be 8 digit");
//
//                                        } else {
//                                            dialog.dismiss();
//                                            String result = ((ConfigActivity) requireActivity()).sonicInterface.RenewTEPIN(newPIN);
//                                            new AlertDialog.Builder(requireActivity())
//                                                    .setTitle("Renew TE PIN")
//                                                    .setMessage(result)
//                                                    .setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss())
//                                                    .create()
//                                                    .show();
//                                        }
//
//                                    } catch (Exception e) {
//                                        LogUtils.e(TAG, "Renew TE PIN Exception: " + Log.getStackTraceString(e));
//                                    }
                                }
                            })
                            .setNegativeButton("Cancel",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });

                    // create an alert dialog
                    AlertDialog dialog = alertDialogBuilder.create();
                    dialog.show();
                    //Overriding the handler immediately after show is probably a better approach than OnShowListener as described below
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            try {
                                String newPIN = editText.getText().toString();

                                if (newPIN.equalsIgnoreCase("") || newPIN.length() != 8) {
                                    messageText.setTextColor(Color.RED);
                                    messageText.setText("PIN must be 8 digit");

                                } else {
                                    dialog.dismiss();
                                    String result = ((ConfigActivity) requireActivity()).sonicInterface.RenewTEPIN(newPIN);
                                    new AlertDialog.Builder(requireActivity())
                                            .setTitle("Renew TE PIN")
                                            .setMessage(result)
                                            .setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss())
                                            .create()
                                            .show();
                                }

                            } catch (Exception e) {
                                LogUtils.e(TAG, "Renew TE PIN Exception: " + Log.getStackTraceString(e));
                            }
                        }
                    });
                }

                if(preference.getKey().equals(getResources().getString(R.string.ClearReversal))) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                    builder.setTitle("Clear Reversal");
                    builder.setMessage("Confirm to proceed?");

                    // Positive button (OK action)
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Log.i(TAG, "Clear Reversal");
                            boolean result = false;
                            try {
                                result = ((ConfigActivity)requireActivity()).sonicInterface.ClearReversal();
                            } catch (Exception e) {
                                LogUtils.e(TAG, "ResetEMVConfigFile Error: " + Log.getStackTraceString(e));
                            }
                            new AlertDialog.Builder(requireActivity())
                                    .setTitle("Clear Reversal")
                                    .setMessage(result ? "Reversal cleared successfully" : "Failed to clear reversal")
                                    .setIcon(result ? R.drawable.check_leaf_shadow : R.drawable.cross_fat)
                                    .setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss())
                                    .create()
                                    .show();
                        }
                    });

                    // Negative button (Cancel action)
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Perform any action you want when "No" is clicked, or leave it empty
                            dialog.dismiss(); // Dismiss the dialog
                        }
                    });

                    getActivity().runOnUiThread(()->{
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    });
                }

                if(preference.getKey().equals(getResources().getString(R.string.ClearSettlement))) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                    builder.setTitle("Clear Settlement");
                    builder.setMessage("Confirm to proceed?");

                    // Positive button (OK action)
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Log.i(TAG, "Clear Settlement");
                            boolean result = false;
                            try {
                                result = ((ConfigActivity)requireActivity()).sonicInterface.ClearSettlement();
                            } catch (Exception e) {
                                LogUtils.e(TAG, "ResetEMVConfigFile Error: " + Log.getStackTraceString(e));
                            }
                            new AlertDialog.Builder(requireActivity())
                                    .setTitle("Clear Settlement")
                                    .setMessage(result ? "Settlement cleared successfully" : "Failed to clear settlement")
                                    .setIcon(result ? R.drawable.check_leaf_shadow : R.drawable.cross_fat)
                                    .setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss())
                                    .create()
                                    .show();
                        }
                    });

                    // Negative button (Cancel action)
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Perform any action you want when "No" is clicked, or leave it empty
                            dialog.dismiss(); // Dismiss the dialog
                        }
                    });

                    getActivity().runOnUiThread(()->{
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    });
                }

                if(preference.getKey().equals(getResources().getString(R.string.ResetEMVConfigFile))) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                    builder.setTitle("Reset EMV Config File");
                    builder.setMessage("Confirm to proceed?");

                    // Positive button (OK action)
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Log.i(TAG, "Reset EMV Config File");
                            int result = 0;
                            try {
                                result = ((ConfigActivity) requireActivity()).sonicInterface.PerformAction("resetemvconfigfile", null);
                            } catch (Exception e) {
                                LogUtils.e(TAG, "ResetEMVConfigFile Error: " + Log.getStackTraceString(e));
                            }
                            new AlertDialog.Builder(requireActivity())
                                    .setTitle("Reset EMV Config File")
                                    .setMessage(result == 1 ? "Reset Successful" : "Reset Failed")
                                    .setIcon(result == 1 ? R.drawable.check_leaf_shadow : R.drawable.cross_fat)
                                    .setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss())
                                    .create()
                                    .show();
                        }
                    });

                    // Negative button (Cancel action)
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Perform any action you want when "No" is clicked, or leave it empty
                            dialog.dismiss(); // Dismiss the dialog
                        }
                    });

                    getActivity().runOnUiThread(()->{
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    });
                }

                if(preference.getKey().equals(getResources().getString(R.string.ResetConfig))) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                    builder.setTitle("Reset Config");
                    builder.setMessage("Application settings will be erased, confirm to proceed?");

                    // Positive button (OK action)
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Log.i(TAG, "Reset Config");
                            int result = 0;
                            try {
                                result = ((ConfigActivity) requireActivity()).sonicInterface.PerformAction("resetconfig", null);
                            } catch (Exception e) {
                                LogUtils.e(TAG, "ResetConfig Error: " + Log.getStackTraceString(e));
                            }
                            new AlertDialog.Builder(requireActivity())
                                    .setTitle("Reset Config")
                                    .setMessage(result == 1 ? "Reset Successful" : "Reset Failed")
                                    .setIcon(result == 1 ? R.drawable.check_leaf_shadow : R.drawable.cross_fat)
                                    .setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss())
                                    .create()
                                    .show();
                        }
                    });

                    // Negative button (Cancel action)
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Perform any action you want when "No" is clicked, or leave it empty
                            dialog.dismiss(); // Dismiss the dialog
                        }
                    });

                    getActivity().runOnUiThread(()->{
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    });
                }

                if(preference.getKey().equals(getResources().getString(R.string.ResetDB))) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                    builder.setTitle("Reset DB");
                    builder.setMessage("Local database will be erased, confirm to proceed?");

                    // Positive button (OK action)
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Log.i(TAG, "Reset DB");
                            int result = 0;
                            try {
                                result = ((ConfigActivity) requireActivity()).sonicInterface.PerformAction("resetdb", null);
                            } catch (Exception e) {
                                LogUtils.e(TAG, "ResetDatabase Error: " + Log.getStackTraceString(e));
                            }
                            new AlertDialog.Builder(requireActivity())
                                    .setTitle("Reset DB")
                                    .setMessage(result == 1 ? "Reset Successful" : "Reset Failed")
                                    .setIcon(result == 1 ? R.drawable.check_leaf_shadow : R.drawable.cross_fat)
                                    .setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss())
                                    .create()
                                    .show();
                        }
                    });

                    // Negative button (Cancel action)
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Perform any action you want when "No" is clicked, or leave it empty
                            dialog.dismiss(); // Dismiss the dialog
                        }
                    });

                    getActivity().runOnUiThread(()->{
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    });
                }

                if(preference.getKey().equals(getResources().getString(R.string.ResetNRT))) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                    builder.setTitle("Reset NRT");
                    builder.setMessage("Local NRT will be erased, confirm to proceed?");

                    // Positive button (OK action)
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Log.i(TAG, "Reset NRT");
                            int result = 0;
                            try {
                                result = ((ConfigActivity) requireActivity()).sonicInterface.PerformAction("resetnrt", null);
                            } catch (Exception e) {
                                LogUtils.e(TAG, "ResetNRT Error: " + Log.getStackTraceString(e));
                            }
                            new AlertDialog.Builder(requireActivity())
                                    .setTitle("Reset NRT")
                                    .setMessage(result == 1 ? "Reset Successful" : "Reset Failed")
                                    .setIcon(result == 1 ? R.drawable.check_leaf_shadow : R.drawable.cross_fat)
                                    .setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss())
                                    .create()
                                    .show();
                        }
                    });

                    // Negative button (Cancel action)
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Perform any action you want when "No" is clicked, or leave it empty
                            dialog.dismiss(); // Dismiss the dialog
                        }
                    });

                    getActivity().runOnUiThread(()->{
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    });
                }

                if(preference.getKey().equals(getResources().getString(R.string.DbDiagnostic))) {
                    int result = 0;
                    try {
                        result = ((ConfigActivity) requireActivity()).sonicInterface.PerformAction("DBDiagnostic", null);
                    } catch (Exception e) {
                        LogUtils.e(TAG, "DBDiagnostic Error: " + Log.getStackTraceString(e));
                    }
                    new AlertDialog.Builder(requireActivity())
                            .setTitle("Database Diagnostic")
                            .setMessage(result == 1 ? "Healthy" : "Corrupted")
                            .setIcon(result == 1 ? R.drawable.check_leaf_shadow : R.drawable.cross_fat)
                            .setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss())
                            .create()
                            .show();
                }

                if(preference.getKey().equals(getResources().getString(R.string.BackupDB))) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                    builder.setTitle("Backup DB");
                    builder.setMessage("**This will backup database file to internal storage");

                    // Positive button (OK action)
                    builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Log.i(TAG, "Backup DB");
                            int result = 0;
                            String backupPath = "/sdcard/";
                            String backupFilename = "sonicpay.db";
                            String backupFileSize = "";
                            String backupDate = "";
                            try {
                                result = ((ConfigActivity) requireActivity()).sonicInterface.PerformAction("backupdb", null);
                                if (result == 1) {
                                    File backupFile = new File(backupPath + backupFilename);
                                    LogUtils.i(TAG, "BackupPath: " + backupPath + backupFilename);
                                    if (backupFile != null) {
                                        if (backupFile.length() < 1024)
                                            backupFileSize = backupFile.length() + " bytes";
                                        else
                                            backupFileSize = (double)(backupFile.length() /1024) + " kb";

                                        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                        backupDate = dateFormat.format(new Date(backupFile.lastModified()));
                                    }
                                }
                            } catch (Exception e) {
                                LogUtils.e(TAG, "BackupDB Error: " + Log.getStackTraceString(e));
                            }
                            new AlertDialog.Builder(requireActivity())
                                    .setTitle("Backup DB")
                                    .setMessage(result == 1 ? ("Backup Successful\n" + backupFilename + "\n" + backupFileSize + "\n" + backupDate) : "Restore Failed")
                                    .setIcon(result == 1 ? R.drawable.check_leaf_shadow : R.drawable.cross_fat)
                                    .setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss())
                                    .create()
                                    .show();
                        }
                    });

                    // Negative button (Cancel action)
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Perform any action you want when "No" is clicked, or leave it empty
                            dialog.dismiss(); // Dismiss the dialog
                        }
                    });

                    getActivity().runOnUiThread(()->{
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    });
                }

                if(preference.getKey().equals(getResources().getString(R.string.RestoreDB))) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                    builder.setTitle("Restore DB");
                    builder.setMessage("**This will restore the file from internal storage, existing database will be overwritten!");

                    // Positive button (OK action)
                    builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Log.i(TAG, "Restore DB");
                            int result = 0;
                            try {
                                result = ((ConfigActivity) requireActivity()).sonicInterface.PerformAction("restoredb", null);
                            } catch (Exception e) {
                                LogUtils.e(TAG, "RestoreDB Error: " + Log.getStackTraceString(e));
                            }
                            new AlertDialog.Builder(requireActivity())
                                    .setTitle("Restore DB")
                                    .setMessage(result == 1 ? "Restore Successful" : "Restore Failed")
                                    .setIcon(result == 1 ? R.drawable.check_leaf_shadow : R.drawable.cross_fat)
                                    .setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss())
                                    .create()
                                    .show();
                        }
                    });

                    // Negative button (Cancel action)
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Perform any action you want when "No" is clicked, or leave it empty
                            dialog.dismiss(); // Dismiss the dialog
                        }
                    });

                    getActivity().runOnUiThread(()->{
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    });
                }

                if(preference.getKey().equals(getResources().getString(R.string.DownloadData))) {

                    LayoutInflater layoutInflater = LayoutInflater.from(this.getContext());
                    View promptView = layoutInflater.inflate(R.layout.input_dialog, null);
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this.getContext());
                    alertDialogBuilder.setView(promptView);

                    TextView titleText = (TextView) promptView.findViewById(R.id.input_dialog_title);
                    final EditText editText = (EditText) promptView.findViewById(R.id.input_dialog_text);
                    TextView messageText = (TextView) promptView.findViewById(R.id.input_dialog_message);
                    titleText.setText("Old Serial No");
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                    editText.setHint("Enter Serial No here...");
                    messageText.setText("");
                    // setup a dialog window
                    alertDialogBuilder.setCancelable(false)
                            .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {}
                            })
                            .setNegativeButton("Cancel",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });

                    // create an alert dialog
                    AlertDialog dialog = alertDialogBuilder.create();
                    dialog.show();
                    //Overriding the handler immediately after show is probably a better approach than OnShowListener as described below
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            try {
                                String oldSerialNo = editText.getText().toString();

                                if (oldSerialNo.equalsIgnoreCase("")) {
                                    messageText.setTextColor(Color.RED);
                                    messageText.setText("Serial No cannot be empty");

                                } else {
                                    dialog.dismiss();
                                    int result = ((ConfigActivity) requireActivity()).sonicInterface.PerformAction("DownloadData", oldSerialNo);
                                    String resultMsg = "Download data failed";
                                    if (result == 1)
                                        resultMsg = "Download data successful";
                                    else if (result == -1)
                                        resultMsg = "Abort, download data already performed";
                                    else if (result == -2)
                                        resultMsg = "Abort, database is not empty";

                                    new AlertDialog.Builder(requireActivity())
                                            .setTitle("Download Data")
                                            .setMessage(resultMsg)
                                            .setIcon(result == 1 ? R.drawable.check_leaf_shadow : R.drawable.cross_fat)
                                            .setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss())
                                            .create()
                                            .show();
                                }

                            } catch (Exception e) {
                                LogUtils.e(TAG, "DownloadData Exception: " + Log.getStackTraceString(e));
                            }
                        }
                    });
                }

            } catch (Exception e) {
                LogUtils.e(TAG, "Exception: " + Log.getStackTraceString(e));
            }
            return true;
        }
    }

    public static class EMVSettingFragment extends MyPreferenceFragmentCompat{

        private static final String TAG = "EMVSetting";
        @Override
        public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
            setPreferencesFromResource(R.xml.sp_service_emv_setting_preferences, rootKey);
            ((ConfigActivity)requireActivity()).UpdateTitle("EMV Setting");
            try {
                PreferenceCategory categoryEMVBatchNo = findPreference("cEMVBatchNo");
                assert categoryEMVBatchNo != null;
                PreferenceCategory categoryMIDTID = findPreference("cMIDTID");
                assert categoryMIDTID != null;
                PreferenceCategory categoryTPDUNII = findPreference("cTPDUNII");
                assert categoryTPDUNII != null;
                PreferenceCategory categoryNII = findPreference("cNII");
                assert categoryNII != null;

                String acquirerBank = ((ConfigActivity) requireActivity()).sonicInterface.ReadSharedPref(getString(R.string.AcquirerBank));
                if(acquirerBank != null && (acquirerBank.equals("MBB") || acquirerBank.equals("HLB"))){
                    if(acquirerBank.equals("HLB")){
                        EditTextPreference AmexBatchNo = findPreference(getString(R.string.AmexBatchNo));
                        assert AmexBatchNo != null;
                        categoryEMVBatchNo.removePreference(AmexBatchNo);

                        EditTextPreference AmexMID = findPreference(getString(R.string.AmexMID));
                        assert AmexMID != null;
                        categoryMIDTID.removePreference(AmexMID);

                        EditTextPreference AmexTID = findPreference(getString(R.string.AmexTID));
                        assert AmexTID != null;
                        categoryMIDTID.removePreference(AmexTID);

                        EditTextPreference AmexNII = findPreference(getString(R.string.AmexNII));
                        assert AmexNII != null;
                        categoryNII.removePreference(AmexNII);
                    }
                    EditTextPreference VisaBatchNo = findPreference(getString(R.string.VisaBatchNo));
                    assert VisaBatchNo != null;
                    categoryEMVBatchNo.removePreference(VisaBatchNo);
                    EditTextPreference MasterBatchNo = findPreference(getString(R.string.MasterBatchNo));
                    assert MasterBatchNo != null;
                    categoryEMVBatchNo.removePreference(MasterBatchNo);
                    EditTextPreference VisaDRBatchNo = findPreference(getString(R.string.VisaDRBatchNo));
                    assert VisaDRBatchNo != null;
                    categoryEMVBatchNo.removePreference(VisaDRBatchNo);
                    EditTextPreference MasterDRBatchNo = findPreference(getString(R.string.MasterDRBatchNo));
                    assert MasterDRBatchNo != null;
                    categoryEMVBatchNo.removePreference(MasterDRBatchNo);

                    EditTextPreference VisaMID = findPreference(getString(R.string.VisaMID));
                    assert VisaMID != null;
                    categoryMIDTID.removePreference(VisaMID);
                    EditTextPreference VisaTID = findPreference(getString(R.string.VisaTID));
                    assert VisaTID != null;
                    categoryMIDTID.removePreference(VisaTID);
                    EditTextPreference MasterMID = findPreference(getString(R.string.MasterMID));
                    assert MasterMID != null;
                    categoryMIDTID.removePreference(MasterMID);
                    EditTextPreference MasterTID = findPreference(getString(R.string.MasterTID));
                    assert MasterTID != null;
                    categoryMIDTID.removePreference(MasterTID);
                    EditTextPreference VisaMIDDR = findPreference(getString(R.string.VisaMIDDR));
                    assert VisaMIDDR != null;
                    categoryMIDTID.removePreference(VisaMIDDR);
                    EditTextPreference VisaTIDDR = findPreference(getString(R.string.VisaTIDDR));
                    assert VisaTIDDR != null;
                    categoryMIDTID.removePreference(VisaTIDDR);
                    EditTextPreference MasterMIDDR = findPreference(getString(R.string.MasterMIDDR));
                    assert MasterMIDDR != null;
                    categoryMIDTID.removePreference(MasterMIDDR);
                    EditTextPreference MasterTIDDR = findPreference(getString(R.string.MasterTIDDR));
                    assert MasterTIDDR != null;
                    categoryMIDTID.removePreference(MasterTIDDR);

                    EditTextPreference VisaTPDUNII = findPreference(getString(R.string.VisaTPDUNII));
                    assert VisaTPDUNII != null;
                    categoryTPDUNII.removePreference(VisaTPDUNII);
                    EditTextPreference MasterTPDUNII = findPreference(getString(R.string.MasterTPDUNII));
                    assert MasterTPDUNII != null;
                    categoryTPDUNII.removePreference(MasterTPDUNII);
                    EditTextPreference MCCSTPDUNII = findPreference(getString(R.string.MCCSTPDUNII));
                    assert MCCSTPDUNII != null;
                    categoryTPDUNII.removePreference(MCCSTPDUNII);

                    EditTextPreference VisaNII = findPreference(getString(R.string.VisaNII));
                    assert VisaNII != null;
                    categoryNII.removePreference(VisaNII);
                    EditTextPreference MasterNII = findPreference(getString(R.string.MasterNII));
                    assert MasterNII != null;
                    categoryNII.removePreference(MasterNII);

                }

                if(acquirerBank != null && acquirerBank.equals("PBB")){
                    EditTextPreference VisaMasterBatchNo = findPreference(getString(R.string.VisaMasterBatchNo));
                    assert VisaMasterBatchNo != null;
                    categoryEMVBatchNo.removePreference(VisaMasterBatchNo);
                    EditTextPreference AmexBatchNo = findPreference(getString(R.string.AmexBatchNo));
                    assert AmexBatchNo != null;
                    categoryEMVBatchNo.removePreference(AmexBatchNo);
                    EditTextPreference UPIBatchNo = findPreference(getString(R.string.UPIBatchNo));
                    assert UPIBatchNo != null;
                    categoryEMVBatchNo.removePreference(UPIBatchNo);

                    EditTextPreference VisaMasterMID = findPreference(getString(R.string.VisaMasterMID));
                    assert VisaMasterMID != null;
                    categoryMIDTID.removePreference(VisaMasterMID);
                    EditTextPreference VisaMasterTID = findPreference(getString(R.string.VisaMasterTID));
                    assert VisaMasterTID != null;
                    categoryMIDTID.removePreference(VisaMasterTID);
                    EditTextPreference AmexMID = findPreference(getString(R.string.AmexMID));
                    assert AmexMID != null;
                    categoryMIDTID.removePreference(AmexMID);
                    EditTextPreference AmexTID = findPreference(getString(R.string.AmexTID));
                    assert AmexTID != null;
                    categoryMIDTID.removePreference(AmexTID);
                    EditTextPreference UPIMID = findPreference(getString(R.string.UPIMID));
                    assert UPIMID != null;
                    categoryMIDTID.removePreference(UPIMID);
                    EditTextPreference UPITID = findPreference(getString(R.string.UPITID));
                    assert UPITID != null;
                    categoryMIDTID.removePreference(UPITID);

                    EditTextPreference TPDUNII = findPreference(getString(R.string.TPDUNII));
                    assert TPDUNII != null;
                    categoryTPDUNII.removePreference(TPDUNII);

                    EditTextPreference VisaMasterNII = findPreference(getString(R.string.VisaMasterNII));
                    assert VisaMasterNII != null;
                    categoryNII.removePreference(VisaMasterNII);
                    EditTextPreference AmexNII = findPreference(getString(R.string.AmexNII));
                    assert AmexNII != null;
                    categoryNII.removePreference(AmexNII);
                    EditTextPreference UPINII = findPreference(getString(R.string.UPINII));
                    assert UPINII != null;
                    categoryNII.removePreference(UPINII);

                }

                if(acquirerBank != null && acquirerBank.equals("MGATE")){
                    PreferenceCategory emvAppSelection = findPreference("cEMVAppSelection");
                    assert emvAppSelection != null;
                    getPreferenceScreen().removePreference(emvAppSelection);

                    getPreferenceScreen().removePreference(categoryEMVBatchNo);
                    getPreferenceScreen().removePreference(categoryTPDUNII);

                    PreferenceCategory emvMode = findPreference("cEMVMode");
                    assert emvMode != null;
                    getPreferenceScreen().removePreference(emvMode);

                    EditTextPreference VisaMID = findPreference(getString(R.string.VisaMID));
                    assert VisaMID != null;
                    categoryMIDTID.removePreference(VisaMID);
                    EditTextPreference VisaTID = findPreference(getString(R.string.VisaTID));
                    assert VisaTID != null;
                    categoryMIDTID.removePreference(VisaTID);
                    EditTextPreference MasterMID = findPreference(getString(R.string.MasterMID));
                    assert MasterMID != null;
                    categoryMIDTID.removePreference(MasterMID);
                    EditTextPreference MasterTID = findPreference(getString(R.string.MasterTID));
                    assert MasterTID != null;
                    categoryMIDTID.removePreference(MasterTID);
                    EditTextPreference VisaMIDDR = findPreference(getString(R.string.VisaMIDDR));
                    assert VisaMIDDR != null;
                    categoryMIDTID.removePreference(VisaMIDDR);
                    EditTextPreference VisaTIDDR = findPreference(getString(R.string.VisaTIDDR));
                    assert VisaTIDDR != null;
                    categoryMIDTID.removePreference(VisaTIDDR);
                    EditTextPreference MasterMIDDR = findPreference(getString(R.string.MasterMIDDR));
                    assert MasterMIDDR != null;
                    categoryMIDTID.removePreference(MasterMIDDR);
                    EditTextPreference MasterTIDDR = findPreference(getString(R.string.MasterTIDDR));
                    assert MasterTIDDR != null;
                    categoryMIDTID.removePreference(MasterTIDDR);
                    EditTextPreference MCCSMID = findPreference(getString(R.string.MCCSMID));
                    assert MCCSMID != null;
                    categoryMIDTID.removePreference(MCCSMID);
                    EditTextPreference MCCSTID = findPreference(getString(R.string.MCCSTID));
                    assert MCCSTID != null;
                    categoryMIDTID.removePreference(MCCSTID);
                    EditTextPreference UPIMID = findPreference(getString(R.string.UPIMID));
                    assert UPIMID != null;
                    categoryMIDTID.removePreference(UPIMID);
                    EditTextPreference UPITID = findPreference(getString(R.string.UPITID));
                    assert UPITID != null;
                    categoryMIDTID.removePreference(UPITID);

                    EditTextPreference VisaNII = findPreference(getString(R.string.VisaNII));
                    assert VisaNII != null;
                    categoryNII.removePreference(VisaNII);
                    EditTextPreference MasterNII = findPreference(getString(R.string.MasterNII));
                    assert MasterNII != null;
                    categoryNII.removePreference(MasterNII);
                    EditTextPreference MCCSNII = findPreference(getString(R.string.MCCSNII));
                    assert MCCSNII != null;
                    categoryNII.removePreference(MCCSNII);
                    EditTextPreference UPINII = findPreference(getString(R.string.UPINII));
                    assert UPINII != null;
                    categoryNII.removePreference(UPINII);
                }
            } catch (Exception e) {
                LogUtils.e(TAG, "Exception: " + Log.getStackTraceString(e));
            }
        }
    }

    private static class DownloadSettings extends AsyncTask<Void, Void, Integer> {
        private static final String TAG = "DownloadSettings";
        private WeakReference<ProgressDialog> mProgressDialog;
        private final WeakReference<ConfigActivity> mActivity;

        DownloadSettings(ConfigActivity context, ProgressDialog progressDialog) {
            mProgressDialog = new WeakReference<ProgressDialog>(progressDialog);
            mActivity = new WeakReference<ConfigActivity>(context);
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            int result = 0;
            try {
                result = mActivity.get().sonicInterface.DownloadConfig();
            } catch (Exception e) {
                LogUtils.e(TAG, "Exception: " + Log.getStackTraceString(e));
            }
            return result;
        }

        protected void onPostExecute(Integer result) {
            Log.d(TAG, "Result: " + result);

            try {
                String message = "";
                if(result == 0)
                    message = "Download settings failed";
                if(result == 1)
                    message = "Download settings successfully";
                if(result == 2)
                    message = "Settings up to date";
                if (result == 3)
                    message = "Download config files failed";
                mProgressDialog.get().dismiss();

                String clientCodeKey = mActivity.get().getString(R.string.client_code);

                new SharedPrefUI(mActivity.get()).WriteSharedPrefStr(clientCodeKey, mActivity.get().sonicInterface.ReadSharedPref(clientCodeKey));

                // v1.0.7 set Controller IP and Port
                String TCPControllerIPKey = mActivity.get().getString(R.string.tcp_controller_ip);
                String TCPControllerPortKey = mActivity.get().getString(R.string.tcp_controller_port);
                String TCPControllerIP = mActivity.get().sonicInterface.ReadSharedPref(TCPControllerIPKey);
                String TCPControllerPort = mActivity.get().sonicInterface.ReadSharedPref(TCPControllerPortKey);
                if (TCPControllerIP != null && !TCPControllerIP.equalsIgnoreCase(""))
                    new SharedPrefUI(mActivity.get()).WriteSharedPrefStr(TCPControllerIPKey, TCPControllerIP);
                if (TCPControllerPort != null && !TCPControllerPort.equalsIgnoreCase("") && !TCPControllerPort.equalsIgnoreCase("0"))
                    new SharedPrefUI(mActivity.get()).WriteSharedPrefStr(TCPControllerPortKey, TCPControllerPort);

                // v1.0.7 set Auto Request QR to follow service [User Scan] enablement
                String AutoRequestQRKey = mActivity.get().getString(R.string.enable_auto_request_qr);;
                boolean AutoRequestQR = mActivity.get().sonicInterface.ReadSharedPrefBoolean("IsQRUserScanEnabled");
                new SharedPrefUI(mActivity.get()).WriteSharedPrefBoolean(AutoRequestQRKey, AutoRequestQR);

                new AlertDialog.Builder(mActivity.get())
                        .setTitle("Download Settings")
                        .setMessage(message)
                        .setIcon(result == 0 ? R.drawable.cross_fat : R.drawable.check_leaf_shadow)
                        .setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss())
                        .create()
                        .show();
            } catch (RemoteException e) {
                LogUtils.e(TAG, "Exception: " + Log.getStackTraceString(e));
            }
        }
    }

    private static class DownloadCAPK extends AsyncTask<Void, Void, Integer> {
        private static final String TAG = "DownloadCAPK";
        private WeakReference<ProgressDialog> mProgressDialog;
        private final WeakReference<ConfigActivity> mActivity;

        DownloadCAPK(ConfigActivity context, ProgressDialog progressDialog) {
            mProgressDialog = new WeakReference<ProgressDialog>(progressDialog);
            mActivity = new WeakReference<ConfigActivity>(context);
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            int result = 0;
            try {
                result = mActivity.get().sonicInterface.PerformAction("DownloadCAPK", null);
            } catch (Exception e) {
                LogUtils.e(TAG, "Exception: " + Log.getStackTraceString(e));
            }
            return result;
        }

        protected void onPostExecute(Integer result) {
            Log.d(TAG, "Result: " + result);

            try {
                String message = "";
                if(result == 0)
                    message = "Download CAPK failed";
                if(result == 1)
                    message = "Download CAPK successfully";
                mProgressDialog.get().dismiss();

                new AlertDialog.Builder(mActivity.get())
                        .setTitle("Download CAPK")
                        .setMessage(message)
                        .setIcon(result == 0 ? R.drawable.cross_fat : R.drawable.check_leaf_shadow)
                        .setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss())
                        .create()
                        .show();
            } catch (Exception e) {
                LogUtils.e(TAG, "Exception: " + Log.getStackTraceString(e));
            }
        }
    }

    private static class UploadData extends AsyncTask<Void, Void, Integer> {
        private final WeakReference<ProgressDialog> mProgressDialog;
        private final WeakReference<ConfigActivity> mActivity;
        private final static String TAG = "UploadData";

        UploadData(ConfigActivity context, ProgressDialog progressDialog) {
            mProgressDialog = new WeakReference<ProgressDialog>(progressDialog);
            mActivity = new WeakReference<ConfigActivity>(context);
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            int result = 0;
            try {
                result = mActivity.get().sonicInterface.DataUpload();
            } catch (Exception e) {
                LogUtils.e(TAG, "Exception: " + Log.getStackTraceString(e));
            }
            return result;
        }

        protected void onPostExecute(Integer result) {
            Log.d(TAG, "Result: " + result);
            String message = "";
            if(result == 0)
                message = "Data upload failed";
            if(result == 1)
                message = "Data upload successfully";
            if(result == 2)
                message = "No data uploaded";
            mProgressDialog.get().dismiss();

            new AlertDialog.Builder(mProgressDialog.get().getContext())
                    .setTitle("Upload data")
                    .setMessage(message)
                    .setIcon(result == 0 ? R.drawable.cross_fat : R.drawable.check_leaf_shadow)
                    .setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss())
                    .create()
                    .show();
        }
    }

    private static class Settlement extends AsyncTask<Integer, Void, Boolean> {
        private final WeakReference<ProgressDialog> mProgressDialog;
        private final WeakReference<ConfigActivity> mActivity;
        private final static String TAG = "Settlement";

        Settlement(ConfigActivity context, ProgressDialog progressDialog) {
            mProgressDialog = new WeakReference<ProgressDialog>(progressDialog);
            mActivity = new WeakReference<ConfigActivity>(context);
        }

        @Override
        protected Boolean doInBackground(Integer... integers) {
            boolean result = false;
            try {
                result = mActivity.get().sonicInterface.Settlement(integers[0], mActivity.get().callbackInterface);
            } catch (RemoteException e) {
                LogUtils.e(TAG, "Exception: " + Log.getStackTraceString(e));
            }
            return result;
        }

        protected void onPostExecute(Boolean result) {
            LogUtils.d(TAG, "Result: " + result);
            mProgressDialog.get().dismiss();

            //TODO: update when callback trigger after result return
            new AlertDialog.Builder(mProgressDialog.get().getContext())
                    .setTitle("Settlement")
                    .setMessage(result ? "Settlement success": "Settlement failed")
                    .setIcon(result ? R.drawable.check_leaf_shadow : R.drawable.cross_fat)
                    .setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss())
                    .create()
                    .show();
        }
    }

    private static class RemoteKeyLoading extends AsyncTask<Integer, Void, String> {
        private final WeakReference<ProgressDialog> mProgressDialog;
        private final WeakReference<ConfigActivity> mActivity;
        private final static String TAG = "RemoteKeyLoading";

        RemoteKeyLoading(ConfigActivity context, ProgressDialog progressDialog) {
            mProgressDialog = new WeakReference<ProgressDialog>(progressDialog);
            mActivity = new WeakReference<ConfigActivity>(context);
        }

        @Override
        protected String doInBackground(Integer... integers) {
            String result;
            try {
                result = mActivity.get().sonicInterface.RemoteKeyLoading(integers[0]);
            } catch (RemoteException e) {
                LogUtils.e(TAG, "Exception: " + Log.getStackTraceString(e));
                result = "Error";
            }
            return result;
        }

        protected void onPostExecute(String result) {
            LogUtils.d(TAG, "Result: " + result);
            mProgressDialog.get().dismiss();

            new AlertDialog.Builder(mProgressDialog.get().getContext())
                    .setTitle("Remote Key Loading")
                    .setMessage(result)
                    .setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss())
                    .create()
                    .show();
        }
    }

    private static class DownloadParam extends AsyncTask<Void, Void, Integer> {
        private static final String TAG = "DownloadSettings";
        private WeakReference<ProgressDialog> mProgressDialog;
        private final WeakReference<ConfigActivity> mActivity;

        DownloadParam(ConfigActivity context, ProgressDialog progressDialog) {
            mProgressDialog = new WeakReference<ProgressDialog>(progressDialog);
            mActivity = new WeakReference<ConfigActivity>(context);
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            int result = 0;
            try {
                result = mActivity.get().sonicInterface.PerformAction("ParamDownload", "");
            } catch (Exception e) {
                LogUtils.e(TAG, "Exception: " + Log.getStackTraceString(e));
            }
            return result;
        }

        protected void onPostExecute(Integer result) {
            Log.d(TAG, "Result: " + result);

            try {
                String message = "";
                if (result == 0)
                    message = "Download Param failed";
                if (result == 1)
                    message = "Download Param successfully";
                if (result == 2)
                    message = "Param up to date";
                mProgressDialog.get().dismiss();

                String clientCodeKey = mActivity.get().getString(R.string.client_code);

                new SharedPrefUI(mActivity.get()).WriteSharedPrefStr(clientCodeKey, mActivity.get().sonicInterface.ReadSharedPref(clientCodeKey));

                new AlertDialog.Builder(mActivity.get())
                        .setTitle("Download Settings")
                        .setMessage(message)
                        .setIcon(result == 0 ? R.drawable.cross_fat : R.drawable.check_leaf_shadow)
                        .setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss())
                        .create()
                        .show();
            } catch (RemoteException e) {
                LogUtils.e(TAG, "Exception: " + Log.getStackTraceString(e));
            }
        }
    }
}
