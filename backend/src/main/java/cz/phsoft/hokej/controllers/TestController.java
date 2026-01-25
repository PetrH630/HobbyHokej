package cz.phsoft.hokej.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "*")
public class TestController {

    @GetMapping
    public String hello() {
        return "Backend je online!";
    }
}
