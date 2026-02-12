package cz.phsoft.hokej.models.services.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DemoModePolicy {

    @Value("${app.demo-mode:false}")
    private boolean demoMode;

    public boolean isDemoMode() {
        return demoMode;
    }

    public boolean isProtectedDemoUser(Long userId) {
        return demoMode && userId != null && userId <= 10;
    }
}
