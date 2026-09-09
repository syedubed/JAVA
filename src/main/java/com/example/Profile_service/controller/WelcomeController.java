package com.example.Profile_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WelcomeController {

    @GetMapping("/")
    public String welcome() {
        return "Profile service is running!";
    }

    @ GetMapping("/about")
    public String about(){
        return " this appilication manages the profile ";
    }
}
