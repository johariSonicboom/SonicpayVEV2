package com.sonicboom.sonicpayvui.models;

import java.util.List;

public class AppUpdate {
    public class GetAppUpdateRequest{
        public String packageName;
        public String versionCode;
    }

    public class ApkInfo{
        public String appName;
        public String appCode;
        public String versionCode;
        public String versionName;
        public String file;
    }
}
