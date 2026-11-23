package sudokuApp;

import com.example.sudoku.BuildConfig;

import android.util.Log;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class AppLogger {

    public static Logger getLogger(Class<?> clazz) {
        Logger logger = Logger.getLogger(clazz.getName());
        logger.setUseParentHandlers(false);

        if (logger.getHandlers().length == 0) {
            Handler handler = new LogcatHandler();
            Level level = BuildConfig.DEBUG ? Level.ALL : Level.WARNING;
            logger.setLevel(level);
            handler.setLevel(level);
            logger.addHandler(handler);
        }
        return logger;
    }

    /** Передає записи java.util.logging у Logcat з відповідним рівнем і тегом = ім'я класу. */
    private static class LogcatHandler extends Handler {
        @Override
        public void publish(LogRecord record) {
            if (!isLoggable(record)) return;

            int v = record.getLevel().intValue();
            int priority = v >= Level.SEVERE.intValue() ? Log.ERROR
                    : v >= Level.WARNING.intValue() ? Log.WARN
                    : v >= Level.INFO.intValue() ? Log.INFO
                    : Log.DEBUG;

            String name = record.getLoggerName();
            String tag = name.substring(name.lastIndexOf('.') + 1);
            Log.println(priority, tag, record.getMessage());
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() {
        }
    }
}