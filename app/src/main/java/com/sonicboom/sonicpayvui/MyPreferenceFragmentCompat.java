package com.sonicboom.sonicpayvui;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.XmlRes;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import com.sonicboom.sonicpayvui.activity.ConfigActivity;

public class MyPreferenceFragmentCompat extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {

    }

    @SuppressLint("RestrictedApi")
    @Override
    public void setPreferencesFromResource(@XmlRes int preferencesResId, @Nullable String key) {
        //requirePreferenceManager();

        ((ConfigActivity)requireActivity()).bindPreferenceValues(getPreferenceManager().inflateFromResource(requireContext(),
                preferencesResId, null));

        @SuppressLint("RestrictedApi")
        final PreferenceScreen xmlRoot = getPreferenceManager().inflateFromResource(requireContext(),
                preferencesResId, null);

        final Preference root;
        if (key != null) {
            root = xmlRoot.findPreference(key);
            if (!(root instanceof PreferenceScreen)) {
                throw new IllegalArgumentException("Preference object with key " + key
                        + " is not a PreferenceScreen");
            }
        } else {
            root = xmlRoot;
        }

        setPreferenceScreen((PreferenceScreen) root);

        ((ConfigActivity)requireActivity()).setPreferenceListener(this.getPreferenceScreen());
    }

}
