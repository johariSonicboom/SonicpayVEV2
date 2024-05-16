package com.sonicboom.sonicpayvui.config.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.gson.Gson;
import com.pax.dal.IDAL;
import com.pax.neptunelite.api.NeptuneLiteUser;
import com.sonicboom.sonicpayvui.AppUpdateService;
import com.sonicboom.sonicpayvui.BuildConfig;
import com.sonicboom.sonicpayvui.CheckboxListAdapter;
import com.sonicboom.sonicpayvui.FileUploader;
import com.sonicboom.sonicpayvui.R;
import com.sonicboom.sonicpayvui.RetrofitClient;
import com.sonicboom.sonicpayvui.SharedPrefUI;
import com.sonicboom.sonicpayvui.activity.ConfigActivity;
import com.sonicboom.sonicpayvui.models.AppUpdate;
import com.sonicboom.sonicpayvui.utils.FileUtils;
import com.sonicboom.sonicpayvui.utils.LogUtils;
import com.sonicboom.sonicpayvui.utils.Utils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SPUIConfigFragment extends PreferenceFragmentCompat {

    private static final String TAG = "SPUIConfig";
    private MaterialDatePicker<Pair<Long, Long>> datePicker;

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.sp_ui_menu, rootKey);
    }

    @Override
    public void onResume(){
        super.onResume();
        ((ConfigActivity)requireActivity()).UpdateTitle("SonicPay UI");
    }

    @SuppressLint("RestrictedApi")
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

            if(preference.getKey() != null && preference.getKey().equals("cFileUpload")) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
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
                    if (datePicker.getSelection() != null) {
                        // Get the selected date range
                        String startDate = dateFormat.format(datePicker.getSelection().first);
                        String endDate = dateFormat.format(datePicker.getSelection().second);
                        String date = dateFormat.format(Calendar.getInstance().getTime());

                        String sourceFolderPath = requireActivity().getFilesDir().getPath() + "/appLog";
                        String zipFilePath = Environment.getExternalStorageDirectory() + "/SPUI_"+ date +".zip";

                        try {
                            FileUtils.zipFolder(sourceFolderPath, zipFilePath, startDate, endDate);
                            File zipFile = new File(zipFilePath);
                            FileUploader.uploadFile(requireActivity(), ((ConfigActivity) requireActivity()).sonicInterface.ReadSharedPref(getString(R.string.TMSWSURL)), Uri.fromFile(zipFile),true);
                        } catch (Exception e) {
                            LogUtils.e(TAG, "Exception: " + Log.getStackTraceString(e));
                        }
                    }
                    datePicker = null;
                });

                datePicker.show(requireActivity().getSupportFragmentManager(), "datePicker");

            }
        } catch (Exception e) {
            LogUtils.e(TAG, "Exception: " + Log.getStackTraceString(e));
        }
        return true;
    }

    public static class GeneralConfigurationFragment extends PreferenceFragmentCompat{

        private final static String TAG = "GeneralConfiguration";
        @Override
        public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
            setPreferencesFromResource(R.xml.sp_ui_general_config_preferences, rootKey);
            ((ConfigActivity)requireActivity()).UpdateTitle("General Configuration");
        }

        @Override
        public boolean onPreferenceTreeClick(@NonNull Preference preference){
            try {
                IDAL dal = NeptuneLiteUser.getInstance().getDal(requireActivity());

                if(preference.getKey().equals(getString(R.string.enable_nav_bar))){
                    dal.getSys().enableNavigationBar(new SharedPrefUI(requireActivity()).ReadSharedPrefBoolean(preference.getKey()));
                    dal.getSys().showNavigationBar(new SharedPrefUI(requireActivity()).ReadSharedPrefBoolean(preference.getKey()));
                }

                if(preference.getKey().equals(getString(R.string.enable_status_bar))){
                    dal.getSys().enableStatusBar(new SharedPrefUI(requireActivity()).ReadSharedPrefBoolean(preference.getKey()));
                }

            } catch (Exception e) {
                LogUtils.e("SPUIConfig", "Exception: " + e.getMessage());
            }
            return true;
        }
    }

    public static class TCPConfigurationFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
            setPreferencesFromResource(R.xml.sp_ui_tcp_preferences, rootKey);
            ((ConfigActivity) requireActivity()).UpdateTitle("TCP Configuration");
        }
    }

    public static class SoftwareUpdateFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
            setPreferencesFromResource(R.xml.sp_ui_software_update, rootKey);
            ((ConfigActivity) requireActivity()).UpdateTitle("Software Update");
        }

        @SuppressLint("RestrictedApi")
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

                if(preference.getKey().equals("cSPUIUpdate")) {
                    ((ConfigActivity)requireActivity()).pd.setMessage("Checking update...");
                    ((ConfigActivity)requireActivity()).pd.show();
                    GetAppUpdate(BuildConfig.APPLICATION_ID, String.valueOf(BuildConfig.VERSION_CODE));
                }

                if(preference.getKey().equals("cSPServiceUpdate")){
                    ((ConfigActivity)requireActivity()).pd.setMessage("Checking update...");
                    ((ConfigActivity)requireActivity()).pd.show();

                    PackageManager pm = requireActivity().getPackageManager();
                    PackageInfo pInfo = pm.getPackageInfo(Utils.getServiceAppCode(), 0);

                    GetAppUpdate(Utils.getServiceAppCode(), String.valueOf(pInfo.versionCode));
                }
            } catch (Exception e) {
                LogUtils.e(TAG, "onPreferenceTreeClick Exception: " + Log.getStackTraceString(e));
            }
            return true;
        }

        private void showDynamicCheckboxListDialog(Context context, List<AppUpdate.ApkInfo> items) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Select version to update");

            // Create a ListView to hold the checkbox list
            ListView listView = new ListView(context);
            CheckboxListAdapter adapter = new CheckboxListAdapter(context, items);
            listView.setAdapter(adapter);

            builder.setView(listView);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ((ConfigActivity)requireActivity()).pd.setMessage("Updating");
                    ((ConfigActivity)requireActivity()).pd.show();

                    // Handle OK button click
                    int index = adapter.getCheckedPosition();
                    String fileName = items.get(index).file;
                    new InstallApp(((ConfigActivity)context), ((ConfigActivity)context).pd).execute(new SharedPrefUI(requireActivity()).ReadSharedPrefStr(getString(R.string.app_update_server_url)), fileName);
                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Handle Cancel button click
                    dialog.dismiss();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }

        public void GetAppUpdate(String appId, String versionCode){
//            String url = new SharedPrefUI(requireActivity()).ReadSharedPrefStr(getString(R.string.app_update_server_url));
//            if(url.isEmpty())
//                url = "http://codev.southeastasia.cloudapp.azure.com/AppUpdate";
//            if(!url.endsWith("/"))
//                url += "/";
//            AppUpdateService appUpdateService = RetrofitClient.getAppUpdateService(url);
//            AppUpdate.GetAppUpdateRequest appUpdateServiceReq = new AppUpdate().new GetAppUpdateRequest();
//            appUpdateServiceReq.packageName = appId;
//            appUpdateServiceReq.versionCode = versionCode;
//
//            Call<List<AppUpdate.ApkInfo>> call = appUpdateService.GetAppUpdate(appUpdateServiceReq);
//            call.enqueue(new Callback<List<AppUpdate.ApkInfo>>() {
//                @Override
//                public void onResponse(Call<List<AppUpdate.ApkInfo>> call, Response<List<AppUpdate.ApkInfo>> response) {
//                    if (response.isSuccessful()) {
//                        LogUtils.i(TAG, "GetAppUpdate Response: " + new Gson().toJson(response.body()));
//                        if(response.body() != null ){
//                            ((ConfigActivity)requireActivity()).pd.dismiss();
//                            if(response.body().size() == 0){
//                                new AlertDialog.Builder(requireActivity())
//                                        .setTitle("Software Update")
//                                        .setMessage("Current version is the latest")
//                                        .setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss())
//                                        .create()
//                                        .show();
//                            }
//                            else
//                                showDynamicCheckboxListDialog(requireActivity(), response.body());
//                        }
//                    }
//                }
//
//                @Override
//                public void onFailure(Call<List<AppUpdate.ApkInfo>> call, Throwable t) {
//                    LogUtils.e(TAG, "GetAppUpdate onFailure: " + t.getMessage());
//                    new AlertDialog.Builder(requireActivity())
//                            .setTitle("Software Update")
//                            .setMessage("App failed to update")
//                            .setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss())
//                            .create()
//                            .show();
//                }
//            });
            Callback<List<AppUpdate.ApkInfo>> callback = new Callback<List<AppUpdate.ApkInfo>>() {
                @Override
                public void onResponse(Call<List<AppUpdate.ApkInfo>> call, Response<List<AppUpdate.ApkInfo>> response) {
                    if (response.isSuccessful()) {
                        LogUtils.i(TAG, "GetAppUpdate Response: " + new Gson().toJson(response.body()));
                        if(response.body() != null ){
                            ((ConfigActivity)requireActivity()).pd.dismiss();
                            if(response.body().size() == 0){
                                new AlertDialog.Builder(requireActivity())
                                        .setTitle("Software Update")
                                        .setMessage("Current version is the latest")
                                        .setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss())
                                        .create()
                                        .show();
                            }
                            else
                                showDynamicCheckboxListDialog(requireActivity(), response.body());
                        }
                    }
                }

                @Override
                public void onFailure(Call<List<AppUpdate.ApkInfo>> call, Throwable t) {
                    LogUtils.e(TAG, "GetAppUpdate onFailure: " + t.getMessage());
                    new AlertDialog.Builder(requireActivity())
                            .setTitle("Software Update")
                            .setMessage("App failed to update")
                            .setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss())
                            .create()
                            .show();
                }
            };
            FileUtils.GetAppUpdate(requireActivity(), appId, versionCode, callback);
        }
    }

    //DEPRECATED: first version app update
    private static class SoftwareUpdate extends AsyncTask<String, Void, Integer> {
        private final WeakReference<ProgressDialog> mProgressDialog;
        private final WeakReference<ConfigActivity> mActivity;
        private final static String TAG = "SoftwareUpdate";

        SoftwareUpdate(ConfigActivity context, ProgressDialog progressDialog) {
            mProgressDialog = new WeakReference<ProgressDialog>(progressDialog);
            mActivity = new WeakReference<ConfigActivity>(context);
        }

        @Override
        protected Integer doInBackground(String... strings) {
            int result = -1;
            try {
                result = FileUtils.UpdateSoftware(mActivity.get(), strings[0]);
            }
            catch(Exception e){
                LogUtils.e(TAG, "SoftwareUpdate exception: " + Log.getStackTraceString(e));
            }
            return result;
        }

        protected void onPostExecute(Integer result) {
            mProgressDialog.get().dismiss();
            if(result != 0){
                new AlertDialog.Builder(mProgressDialog.get().getContext())
                        .setTitle("Software Update")
                        .setMessage("App failed to update")
                        .setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss())
                        .create()
                        .show();
            }
        }
    }

    private static class InstallApp extends AsyncTask<String, Void, Integer> {
        private final WeakReference<ProgressDialog> mProgressDialog;
        private final WeakReference<ConfigActivity> mActivity;
        private final static String TAG = "InstallApp";

        InstallApp(ConfigActivity context, ProgressDialog progressDialog) {
            mProgressDialog = new WeakReference<>(progressDialog);
            mActivity = new WeakReference<>(context);
        }

        @Override
        protected Integer doInBackground(String... strings) {
            int result = -1;
            try {
                FileUtils.DownloadFiles(mActivity.get(), strings[1]);
                result = FileUtils.InstallApk(strings[1]);
            }
            catch(Exception e){
                LogUtils.e(TAG, "InstallApp exception: " + Log.getStackTraceString(e));
            }
            return result;
        }

        protected void onPostExecute(Integer result) {
            mProgressDialog.get().dismiss();
            if(result != 0){
                new AlertDialog.Builder(mProgressDialog.get().getContext())
                        .setTitle("Software Update")
                        .setMessage("App failed to update")
                        .setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss())
                        .create()
                        .show();
            }
        }
    }

}
