package com.sonicboom.sonicpayvui.config.fragment;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;

import com.sonicboom.sonicpayvui.R;
import com.sonicboom.sonicpayvui.SharedPrefUI;
import com.sonicboom.sonicpayvui.activity.ConfigActivity;
import com.sonicboom.sonicpayvui.utils.LogUtils;

public class MenuFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.config_main_menu, rootKey);
    }

    @Override
    public void onResume(){
        super.onResume();
        ((ConfigActivity)requireActivity()).UpdateTitle("Maintenance");
    }

    public static class AboutFragment extends PreferenceFragmentCompat {

        private final static String TAG = "About";
        @Override
        public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
            setPreferencesFromResource(R.xml.sp_about, rootKey);
            ((ConfigActivity)requireActivity()).UpdateTitle("About");

        }
    }
}