package cz.phsoft.hokej.models.dto.requests;

import jakarta.validation.constraints.NotNull;

public class ChangePlayerUserRequest {
    @NotNull
    private Long newUserId;

    public Long getNewUserId() { return newUserId; }
    public void setNewUserId(Long newUserId) { this.newUserId = newUserId; }
}
