package com.sonicboom.sonicpayvui.utils;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.sonicboom.sonicpayvui.BuildConfig;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class LogUtils {
    private static final boolean IS_DEBUG = BuildConfig.DEBUG;
    private static final String VERBOSE = "VERBOSE";
    private static final String INFO = "INFO";
    private static final String DEBUG = "DEBUG";
    private static final String ERROR = "ERROR";
    private static final String WARNING = "WARNING";

    private static String LogPath = null;
    private static String LogName = null;
    private static AppLogger logger = null;

    private static String getTag() {
        StackTraceElement[] trace = new Throwable().getStackTrace();
        if (trace == null || trace.length == 0) {
            return "";
        }
        return trace[2].getClassName() + "." + trace[2].getMethodName() + "(line:" + trace[2].getLineNumber() + ")";
    }

    /**
     * output ERROR level logs
     * @param tag tag
     * @param msg msg
     */
    public static void e(String tag, Object msg) {
        if (IS_DEBUG) {
            Log.e("[" + tag + "]", "" + msg);
            checkIsNewLogNeeded();
            if(logger!=null)
                logger.error(tag, "" + msg);
        }
    }

    /**
     * output WARN level logs
     * @param tag tag
     * @param msg msg
     */
    public static void w(String tag, Object msg) {
        if (IS_DEBUG) {
            Log.w("[" + tag + "]", "" + msg);
            checkIsNewLogNeeded();
            if(logger!=null)
                logger.warning(tag, "" + msg);
        }
    }

    /**
     *  output INFO level logs
     * @param tag tag
     * @param msg msg
     */
    public static void i(String tag, Object msg) {
        if (IS_DEBUG) {
            Log.i("[" + tag + "]", "" + msg);
            checkIsNewLogNeeded();
            if(logger!=null)
                logger.info(tag, "" + msg);
        }
    }

    /**
     * output DEBUG level logs, only captured in logcat, exclude from file
     * @param tag tag
     * @param msg msg
     */
    public static void d(String tag, Object msg) {
        if (IS_DEBUG) {
            Log.d("[" + tag + "]", "" + msg);
        }
    }

    /**
     * output Verbose level logs
     * @param tag tag
     * @param msg msg
     */
    public static void v(String tag, Object msg) {
        if (IS_DEBUG) {
            Log.v("[" + tag + "]", "" + msg);
            checkIsNewLogNeeded();
            if(logger!=null)
                logger.info(tag, "" + msg);
        }
    }

    /**
     * output ERROR level logs
     * @param tag tag
     * @param msg msg
     * @param th th
     */
    public static void e(String tag, String msg, Throwable th) {
        if (IS_DEBUG) {
            Log.e("[" + tag + "]", msg, th);
            checkIsNewLogNeeded();
            if(logger!=null)
                logger.error(tag, "" + msg);
        }
    }

    /**
     * output WARN level logs
     * @param tag tag
     * @param msg msg
     * @param th th
     */
    public static void w(String tag, String msg, Throwable th) {
        if (IS_DEBUG) {
            Log.w("[" + tag + "]", msg, th);
            checkIsNewLogNeeded();
            if(logger!=null)
                logger.warning(tag, "" + msg);
        }
    }
    /**
     *  output INFO level logs
     * @param tag tag
     * @param msg msg
     * @param th th
     */
    public static void i(String tag, String msg, Throwable th) {
        if (IS_DEBUG) {
            Log.i("[" + tag + "]", msg, th);
            checkIsNewLogNeeded();
            if(logger!=null)
                logger.info(tag, "" + msg);
        }
    }
    /**
     * output DEBUG level logs
     * @param tag tag
     * @param msg msg
     * @param th th
     */
    public static void d(String tag, String msg, Throwable th) {
        if (IS_DEBUG) {
            Log.d("[" + tag + "]", msg, th);
            checkIsNewLogNeeded();
            if(logger!=null)
                logger.info(tag, "" + msg);
        }
    }
    /**
     * output Verbose level logs
     * @param tag tag
     * @param msg msg
     * @param th th
     */
    public static void v(String tag, String msg, Throwable th) {
        if (IS_DEBUG) {
            Log.v("[" + tag + "]", msg, th);
            checkIsNewLogNeeded();
            if(logger!=null)
                logger.info(tag, "" + msg);
        }
    }

    /**
     * output DEBUG level logs
     * @param content msg
     */
    public static void d(Object content) {
        d(getTag(), content);
    }

    /**
     * output ERROR level logs
     * @param e error
     */
    public static void e(Exception e) {
        e(getTag(), e);
    }

    /**
     * output INFO level logs
     * @param content msg
     */
    public static void i(Object content) {
        i(getTag(), content);
    }

    public static void init(String _LogPath) {
        LogPath = _LogPath;
        String logpth= getFile(_LogPath);
        logger = new AppLogger(logpth);

    }
    public static void checkIsNewLogNeeded(){
        DateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        String time =  formatter.format(new Date());
        String FileName = time+".txt";
        if(LogName != null) {
            if (!FileName.equalsIgnoreCase(LogName)) {
                String logpth = getFile(LogPath);
                logger = new AppLogger(logpth);
            }

        }
    }

    /**
     * get log file location
     * @return
     */
    public static String getFile(String _LogPath) {
        DateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        String time =  formatter.format(new Date());
        String FileName = time+".txt";
        LogName=FileName;
        File cacheDir = new File(_LogPath + File.separator + "appLog");
        if (!cacheDir.exists()) {
            cacheDir.mkdir();
        }
        File filePath = new File(cacheDir + File.separator + FileName);
        return filePath.toString();
    }
}
