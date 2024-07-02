package com.sonicboom.sonicpayvui;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

public class SharedPrefUI {
    private final SharedPreferences sharedPreference;

    public SharedPrefUI(Context context){
        String fileKey = BuildConfig.APPLICATION_ID + "_preferences";
        this.sharedPreference = context.getSharedPreferences(fileKey, Context.MODE_PRIVATE);
    }

    public boolean Contains(String key) {
        return this.sharedPreference.contains(key);
    }

    public String ReadSharedPrefStr(String key){
        return sharedPreference.getString(key, "");
    }

    public void WriteSharedPrefStr(String key, String value){
        SharedPreferences.Editor editor = sharedPreference.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public int ReadSharedPrefInt(String key){
        return sharedPreference.getInt(key, 0);
    }

    public void WriteSharedPrefInt(String key, int value){
        SharedPreferences.Editor editor = sharedPreference.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public boolean ReadSharedPrefBoolean(String key){
        return sharedPreference.getBoolean(key, false);
    }

    public void WriteSharedPrefBoolean(String key, boolean value){
        SharedPreferences.Editor editor = sharedPreference.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public Map<String, ?> getAll() {
        return sharedPreference.getAll();
    }

    // Ensure these methods are added to your SharedPrefUI class
    public void WriteSharedPrefFloat(String key, float value) {
        SharedPreferences.Editor editor = sharedPreference.edit();
        editor.putFloat(key, value);
        editor.apply();
    }

    public float ReadSharedPrefFloat(String key) {
        return sharedPreference.getFloat(key, 0.0f);
    }

    public void WriteSharedPrefLong(String key, long value) {
        SharedPreferences.Editor editor = sharedPreference.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    public long ReadSharedPrefLong(String key) {
        return sharedPreference.getLong(key, 0L);
    }
}
