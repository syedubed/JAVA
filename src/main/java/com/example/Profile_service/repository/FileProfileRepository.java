package com.example.Profile_service.repository;

import com.example.Profile_service.dto.ProfileRequest;
import com.example.Profile_service.dto.UpdateProfileRequest;
import com.example.Profile_service.model.Profile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class FileProfileRepository {

    private final Path file;
    private final JsonMapper mapper;

    public FileProfileRepository(
            JsonMapper mapper,
            @Value("${profiles.storage.file:data/profiles.json}")
            String filename) {

        this.mapper = mapper;
        this.file = Path.of(filename).toAbsolutePath();
    }

    public record StoredProfiles(
            long lastId,
            List<Profile> profiles
    ) {}

    // CREATE
    public synchronized Profile saveNew(ProfileRequest request) {
        StoredProfiles stored = readFile();
        long id = Math.incrementExact(stored.lastId());

        Profile profile = new Profile(
                id,
                request.name(),
                request.email(),
                request.bio(),
                request.address(),
                request.mobileNumber()
        );

        List<Profile> profiles = new ArrayList<>(stored.profiles());
        profiles.add(profile);

        writeFile(new StoredProfiles(id, profiles));
        return profile;
    }

    // READ
    public synchronized Optional<Profile> findById(Long id) {
        return readFile().profiles().stream()
                .filter(profile -> profile.id().equals(id))
                .findFirst();
    }

    // UPDATE
    public synchronized Optional<Profile> update(
            Long id,
            UpdateProfileRequest request) {

        StoredProfiles stored = readFile();
        List<Profile> profiles = new ArrayList<>(stored.profiles());

        for (int index = 0; index < profiles.size(); index++) {

            Profile currentProfile = profiles.get(index);

            if (currentProfile.id().equals(id)) {
                Profile updated = new Profile(
                        id,
                        request.name(),
                        request.email(),
                        request.bio(),
                        request.address(),
                        currentProfile.mobileNumber()
                );

                profiles.set(index, updated);
                writeFile(new StoredProfiles(stored.lastId(), profiles));

                return Optional.of(updated);
            }
        }

        return Optional.empty();
    }

    // DELETE
    public synchronized boolean deleteById(Long id) {
        StoredProfiles stored = readFile();
        List<Profile> profiles = new ArrayList<>(stored.profiles());

        boolean deleted = profiles.removeIf(
                profile -> profile.id().equals(id)
        );

        if (deleted) {
            writeFile(new StoredProfiles(stored.lastId(), profiles));
        }

        return deleted;
    }

    // Load the saved data.
    private StoredProfiles readFile() {
        if (Files.notExists(file)) {
            return new StoredProfiles(0, List.of());
        }

        try {
            String json = Files.readString(file);
            return mapper.readValue(json, StoredProfiles.class);
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not read profiles from " + file,
                    exception
            );
        }
    }

    // Save to a temporary file, then replace the original.
    private void writeFile(StoredProfiles stored) {
        Path temporaryFile = null;

        try {
            Files.createDirectories(file.getParent());

            String json = mapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(stored);

            temporaryFile = Files.createTempFile(
                    file.getParent(),
                    "profiles-",
                    ".tmp"
            );

            Files.writeString(temporaryFile, json);

            Files.move(
                    temporaryFile,
                    file,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING
            );
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not save profiles to " + file,
                    exception
            );
        } finally {
            if (temporaryFile != null) {
                try {
                    Files.deleteIfExists(temporaryFile);
                } catch (IOException ignored) {
                    // Cleanup failure does not change the saved profiles.
                }
            }
        }
    }

    // to fetch all the profiles
    public synchronized List<Profile> findAll() {
        return new ArrayList<>(readFile().profiles());
    }
}