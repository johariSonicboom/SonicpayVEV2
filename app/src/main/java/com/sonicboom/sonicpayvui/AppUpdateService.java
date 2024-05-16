package com.sonicboom.sonicpayvui;

import com.sonicboom.sonicpayvui.models.AppUpdate;

import java.io.File;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface AppUpdateService {
    @POST("AppUpdate/GetAppUpdate")
    Call<List<AppUpdate.ApkInfo>> GetAppUpdate(@Body AppUpdate.GetAppUpdateRequest request);
}

