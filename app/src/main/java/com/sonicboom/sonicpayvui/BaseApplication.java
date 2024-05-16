package com.sonicboom.sonicpayvui;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

import com.sankuai.waimai.router.Router;
import com.sankuai.waimai.router.common.DefaultRootUriHandler;
import com.sankuai.waimai.router.core.Debugger;

public class BaseApplication extends Application {
    public int a = 1;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        initWMRouter();
    }

    private void initWMRouter() {
        if (BuildConfig.DEBUG) {
            Debugger.setEnableDebug(true);
            Debugger.setEnableLog(true);
            Debugger.setLogger(new RouterDebugger());
        }
        DefaultRootUriHandler rootHandler = new DefaultRootUriHandler(this);
        Router.init(rootHandler);
    }

    private static class RouterDebugger implements Debugger.Logger {
        private static final String TAG = "WMRouter";


        @Override
        public void d(String msg, Object... args) {

        }

        @Override
        public void i(String msg, Object... args) {

        }

        @Override
        public void w(String msg, Object... args) {

        }

        @Override
        public void w(Throwable t) {

        }

        @Override
        public void e(String msg, Object... args) {

        }

        @Override
        public void e(Throwable t) {

        }

        @Override
        public void fatal(String msg, Object... args) {

        }

        @Override
        public void fatal(Throwable t) {

        }
    }
}
