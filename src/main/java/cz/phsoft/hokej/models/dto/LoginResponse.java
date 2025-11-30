package cz.phsoft.hokej.models.dto;

public class LoginResponse {
    private String token;
    private String role;
    private boolean enabled;
    private String type; // přidáno

    public LoginResponse(String token, String role, boolean enabled, String type) {
        this.token = token;
        this.role = role;
        this.enabled = enabled;
        this.type = type;
    }
    // getters & setters


public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
