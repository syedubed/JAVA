package com.example.Profile_service.controller;

import com.example.Profile_service.model.Profile;
import com.example.Profile_service.dto.ProfileRequest;
import com.example.Profile_service.service.ProfileService;
import com.example.Profile_service.dto.UpdateProfileRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/profiles")
@SecurityRequirement(name = "bearerAuth")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/{id}")
    public Profile getProfile(@PathVariable Long id) {
        return profileService.getProfile(id);
    }
    @PostMapping
    public ResponseEntity<Profile> createProfile(
            @Valid @RequestBody ProfileRequest request) {

        Profile profile = profileService.createProfile(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(profile);
    }

    @PutMapping("/{id}")
    public Profile updateProfile(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProfileRequest request) {

        return profileService.updateProfile(id, request);
    }

    //DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProfile(@PathVariable Long id) {
        profileService.deleteProfile(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public List<Profile> getAllProfiles() {
        return profileService.getAllProfiles();
    }
}