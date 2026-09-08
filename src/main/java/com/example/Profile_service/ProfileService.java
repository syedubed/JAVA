package com.example.Profile_service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProfileService {
//    private final InMemoryProfileRepository repository;
//
//    public ProfileService(InMemoryProfileRepository repository) {
//        this.repository = repository;
//    }

    private final FileProfileRepository repository;

    public ProfileService(FileProfileRepository repository) {
        this.repository = repository;
    }

    public Profile getProfile(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Profile not found"
                ));
    }
//POST
    public Profile createProfile(ProfileRequest request) {
        return repository.saveNew(request);
    }
 // PUT
    public Profile updateProfile(Long id, UpdateProfileRequest request) {
        return repository.update(id, request)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Profile not found"
                ));
    }
    // DELETE
    public void deleteProfile(Long id) {
        boolean deleted = repository.deleteById(id);

        if (!deleted) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Profile not found"
            );
        }
    }

    // to fetch all the users
    public List<Profile> getAllProfiles() {
        return repository.findAll();
    }
}
