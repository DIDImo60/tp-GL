package logger;

import java.time.LocalDateTime;

public class AuditLogger {

    private static AuditLogger instance;

    private AuditLogger() {
    }

    public static synchronized AuditLogger getInstance() {
        if (instance == null) {
            instance = new AuditLogger();
        }
        return instance;
    }

    public void log(String action) {
        String logMessage = String.format("[%s] AUDIT LOG: %s", LocalDateTime.now(), action);
        System.out.println(logMessage);
    }
}