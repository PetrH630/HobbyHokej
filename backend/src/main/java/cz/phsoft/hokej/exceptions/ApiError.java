package cz.phsoft.hokej.exceptions;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.Map;

public class ApiError {

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private String clientIp; // ← nově

    // Volitelné: podrobnosti (validace, více chyb, atd.)
    private Map<String, String> details;

    public ApiError(int status, String error, String message, String path,
                    String clientIp) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.clientIp = clientIp;
    }

    public ApiError(int status,
                            String error,
                            String message,
                            String path,
                            String clientIp,
                            Map<String, String> details) {
        this(status, error, message, path, clientIp);
        this.details = details;
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public int getStatus() { return status; }
    public String getError() { return error; }
    public String getMessage() { return message; }
    public String getPath() { return path; }
    public String getClientIp() { return clientIp; }
    public Map<String, String> getDetails() { return details; }
}
