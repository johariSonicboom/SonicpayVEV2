package com.sonicboom.sonicpayvui.utils;

import android.os.Build;

import java.io.File;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class AppLogger {
    private static final String TAG = "AppLogger";

    Logger logger;
    FileHandler fh;
    private final int limit = 15728640; //15 MB
    DateFormat dateFormat = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss.SSS]");

    private class log_message {
        Level level;
        String sourceMethod;
        String message;
        String sDateTime;
        public log_message(Level _level, String _sourceMethod, String _message) {
            level = _level;

            sDateTime = dateFormat.format(new Date());
            sourceMethod = sDateTime + " [" + _sourceMethod + "]";
            message = _message;
        }
    }

    /** Queue Task ******************************/
    BlockingQueue<log_message> logs;

    private class LogFormatter extends Formatter {

        public String format;
        public LogFormatter(String logformat) {
            format = logformat;
        }

        @Override
        public String format(LogRecord record) {
            String source;
            if (record.getSourceClassName() != null) {
                source = record.getSourceClassName().substring(record.getSourceClassName().lastIndexOf('.') + 1);
                if (record.getSourceMethodName() != null) {
                    source += (source != "" ? "." : "") + record.getSourceMethodName();
                }
            } else {
                source = record.getLoggerName();
            }
            String message = formatMessage(record);
            String throwable = "";
            if (record.getThrown() != null) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                pw.println();
                record.getThrown().printStackTrace(pw);
                pw.close();
                throwable = sw.toString();
            }
            return String.format(format,
                    source,
                    record.getLoggerName(),
                    record.getLevel(),
                    message,
                    throwable);
        }
    }

    public AppLogger(String logPath) {

        try {
            writeLog("initializing logger [" + logPath + "]...");

            logger = Logger.getLogger(logPath);
            /*
             * SEVERE (highest value)
             * WARNING
             * INFO
             * CONFIG
             * FINE
             * FINER
             * FINEST (lowest value)
             *
             * ALL - log all
             * OFF - off logging
             * */
            logger.setLevel(Level.INFO);

            // This block configure the logger with handler and formatter
            fh = new FileHandler(logPath, true);
            //fh.setFormatter(new SimpleFormatter());
            //fh.setFormatter(new LogFormatter("[%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL] %4$s %2$s%n%5$s%n"));
            fh.setFormatter(new LogFormatter("%3$-7s %1$s %4$s%n"));
            logger.addHandler(fh);

            // the following statement is used to log any messages
            logger.info("logger [" + logPath + "] initialized");

            // checking queue for task
            logs = new LinkedBlockingDeque<log_message>();
            queue_handler th = new queue_handler();
            th.start();

            writeLog("logger [" + logPath + "] initialized");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeLog(String message) {
        // System.out.println("[Java_AppLogger] -> " + message);
    }

    private void add_log(Level level, String sourceMethod, String message) {
        if (logs != null) {
            logs.add(new log_message(level, sourceMethod, message));
        }
    }

    public static void PurgeLogFile(String _LogPath, int dayToKeep){
        try {
            LogUtils.i(TAG, "PurgeLogFile In");

            File f = new File(_LogPath + File.separator + "appLog");
            File[] logFile = f.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".txt");
                }
            });
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Arrays.sort(logFile, Comparator.comparingLong(File::lastModified).reversed());
            }
            int day = 1;
            for (File a : logFile) {
                //LogUtils.i(TAG, "LogFileName: " + f.getName());
                if (day > dayToKeep) {
                    File[] fileToDelete = f.listFiles(new FilenameFilter() {
                        public boolean accept(File dir, String name) {
                            return name.startsWith(a.getName().replace(".txt", ""));
                        }
                    });
                    for (File d : fileToDelete) {
                        LogUtils.i(TAG, "Log to delete: " + d.getName());
                        d.delete();
                    }
                }
                day++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class queue_handler extends Thread {
        public void run() {
            try {
                while (true) {
                    log_message log = logs.take();
                    logger.logp(log.level, "", log.sourceMethod, log.message);

                    sleep(200);
                }
            } catch (Exception e) {
                writeLog("[Error] queue_handler");
            }
        }
    }

    public void info( String sourceMethod, String message) {
        try {
            add_log(Level.INFO, sourceMethod, message);

        } catch (Exception e) {
            writeLog("[Error] info: " + e.toString());
        }
    }

    public void warning( String sourceMethod, String message) {
        try {
            add_log(Level.WARNING, sourceMethod, message);
        } catch (Exception e) {
            writeLog("[Error] warning: " + e.toString());
        }
    }

    public void error( String sourceMethod, String message) {
        try {
            add_log(Level.SEVERE, sourceMethod, message);
        } catch (Exception e) {
            writeLog("[Error] error: " + e.toString());
        }
    }
}
