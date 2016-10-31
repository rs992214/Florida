package com.thinkware.florida.utility.log;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * log wrapper.
 * 설정은 한 곳에서만 해야 한다.
 */
public class LogHelper {

    private static final int LOG_FILE_SIZE_LIMIT = 512 * 1024;
    private static final int LOG_FILE_MAX_COUNT = 100;
    private static final SimpleDateFormat formatter = new SimpleDateFormat("MM-dd HH:mm:ss.SSS: ", Locale.getDefault());
    private static final Date date = new Date();
    public static String TAG = "LogHelper";
    public static boolean DEBUG = false;
    private static Logger logger;
    private static FileHandler fileHandler;
    private static Handler viewHandler;
    private static ArrayList<String> logBuffer = new ArrayList<>();

    public static void enableDebug(boolean debug) {
        DEBUG = debug;
    }

    public static void setLogFilePath(String logFilePath) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String logFileName = TAG + "-" + dateFormat.format(Calendar.getInstance().getTime()) + "-log%g.txt";
            fileHandler = new FileHandler(logFilePath
                    + File.separator +
                    logFileName, LOG_FILE_SIZE_LIMIT, LOG_FILE_MAX_COUNT, true);
            fileHandler.setFormatter(new Formatter() {
                @Override
                public String format(LogRecord r) {
                    date.setTime(System.currentTimeMillis());

                    StringBuilder ret = new StringBuilder(80);
                    ret.append(formatter.format(date));
                    ret.append(r.getMessage());
                    return ret.toString();
                }
            });
            logger = Logger.getLogger(LogHelper.class.getName());
            logger.addHandler(fileHandler);
            logger.setLevel(Level.ALL);
            logger.setUseParentHandlers(false);
            Log.d(TAG, "init success");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void enableWriteLogFile(String logFilePath) {
        disableWriteLogFile();
        write("#### Enabled write log file!!");
        setLogFilePath(logFilePath);
    }

    public static void disableWriteLogFile() {
        if (logger != null && fileHandler != null) {
            write("#### Disabled write log file!!");
            fileHandler.close();
            logger.removeHandler(fileHandler);
            logger = null;
        }
    }

    public static void applyViewHandler(Handler handler) {
        if (handler != null) {
            int size = logBuffer.size();
            for (int i=0; i<size; i++) {
                Message message = handler.obtainMessage();
                message.obj = logBuffer.get(i);
                handler.sendMessage(message);
            }
            logBuffer.clear();
        }
        viewHandler = handler;
    }

    public static void write(String format, Object... args) {
        String msg = buildMessage(format, args);
        if (DEBUG) {
            Log.d(TAG, msg);
        }

        if (logger != null) {
            logger.log(Level.ALL, format + "\n");
        }

        if (viewHandler == null) {
            if (logBuffer.size() > 1000) {
                logBuffer.clear();
            }
            logBuffer.add(format);
        } else {
            Message message = viewHandler.obtainMessage();
            message.obj = format;
            viewHandler.sendMessage(message);
        }
    }

    public static void setTag(String tag) {
        d("Changing log tag to %s", tag);
        TAG = tag;
    }

    public static void v(String format, Object... args) {
        if (DEBUG) {
            String msg = buildMessage(format, args);
            Log.v(TAG, msg);
//            if (logger != null) {
//                logger.log(Level.ALL, String.format("V/%s\n", msg));
//            }
        }
    }

    public static void i(String format, Object... args) {
        if (DEBUG) {
            String msg = buildMessage(format, args);
            Log.i(TAG, msg);
//            if (logger != null) {
//                logger.log(Level.ALL, String.format("I/%s\n", msg));
//            }
        }
    }

    public static void d(String format, Object... args) {
        if (DEBUG) {
            String msg = buildMessage(format, args);
            Log.d(TAG, msg);
//            if (logger != null) {
//                logger.log(Level.ALL, String.format("D/%s\n", msg));
//            }
        }
    }

    public static void e(String format, Object... args) {
        if (DEBUG) {
            String msg = buildMessage(format, args);
            Log.e(TAG, msg);
//            if (logger != null) {
//                logger.log(Level.ALL, String.format("E/%s\n", msg));
//            }
        }
    }

    public static void e(Throwable tr, String format, Object... args) {
        if (DEBUG) {
            String msg = buildMessage(format, args);
            Log.e(TAG, msg, tr);
//            if (logger != null) {
//                logger.log(Level.ALL, String.format("E/%s\n", msg));
//            }
        }
    }

    public static void wtf(String format, Object... args) {
        if (DEBUG) {
            String msg = buildMessage(format, args);
            Log.wtf(TAG, msg);
//            if (logger != null) {
//                logger.log(Level.ALL, String.format("WTF/%s\n", msg));
//            }
        }
    }

    public static void wtf(Throwable tr, String format, Object... args) {
        if (DEBUG) {
            String msg = buildMessage(format, args);
            Log.wtf(TAG, msg, tr);
//            if (logger != null) {
//                logger.log(Level.ALL, String.format("WTF/%s\n", msg));
//            }
        }
    }

    /**
     * Formats the caller's provided message and prepends useful info like
     * calling thread ID and method name.
     */
    private static String buildMessage(String format, Object... args) {
        String msg = (args == null) ? format : String.format(Locale.US, format, args);
        StackTraceElement[] trace = new Throwable().fillInStackTrace().getStackTrace();

        String caller = "<unknown>";
        // Walk up the stack looking for the first caller outside of VolleyLog.
        // It will be at least two frames up, so start there.
        for (int i = 2; i < trace.length; i++) {
            Class<?> clazz = trace[i].getClass();
            if (!clazz.equals(LogHelper.class)) {
                String callingClass = trace[i].getClassName();
                if (callingClass.contains("$")) {
                    callingClass = callingClass.substring(0, callingClass.lastIndexOf('$') + 2);
                    callingClass = callingClass.substring(callingClass.lastIndexOf('.') + 1);
                } else {
                    callingClass = callingClass.substring(callingClass.lastIndexOf('.') + 1);
                    callingClass = callingClass.substring(callingClass.lastIndexOf('$') + 1);
                }

                caller = callingClass + "." + trace[i].getMethodName() + ":" + trace[i].getLineNumber();
                break;
            }
        }
        //return String.format(Locale.US, "[%d] %s: %s",
        //        Thread.currentThread().getId(), caller, msg);
        return String.format(Locale.US, "[%s] %s", caller, msg);
    }

}
