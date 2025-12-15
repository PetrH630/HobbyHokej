package cz.phsoft.hokej.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DebugController {

    @GetMapping("/api/debug/me")
    public Object me(Authentication auth) {
        return auth;
    }
}
