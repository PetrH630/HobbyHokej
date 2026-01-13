package cz.phsoft.hokej.models.dto;

public class SuccessResponseDTO {
    private  String message;
    private Long id;
    private String timestamp;

    public SuccessResponseDTO(String message, Long id, String timestamp) {
        this.message = message;
        this.id = id;
        this.timestamp = timestamp;
    }

    public String getMessage() { return message; }
    public Long getId() { return id; }
    public String getTimestamp() { return timestamp; }

}
