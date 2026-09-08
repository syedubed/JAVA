package com.example.Profile_service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.json.JsonMapper;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileProfileRepositoryTests {

    @TempDir
    Path temporaryDirectory;

    private FileProfileRepository newRepository() {
        return new FileProfileRepository(
                JsonMapper.builder().build(),
                temporaryDirectory.resolve("profiles.json").toString()
        );
    }

    @Test
    void shouldKeepProfileAcrossRepositoryInstances() {
        // ARRANGE: save through the first repository.
        FileProfileRepository first = newRepository();

        Profile created = first.saveNew(new ProfileRequest(
                "Ubaid",
                "ubaid@example.com",
                "Saved in a file",
                "phoenix, Arizona",
                "+16025550123"
        ));

        // ACT: create another repository using the same file.
        FileProfileRepository second = newRepository();

        // ASSERT: the saved profile is still available.
        Profile loaded = second.findById(created.id()).orElseThrow();

        assertEquals(created, loaded);
    }

    @Test
    void shouldKeepUpdatesAcrossRepositoryInstances() {
        FileProfileRepository first = newRepository();

        Profile created = first.saveNew(new ProfileRequest(
                "Ubaid",
                "ubaid@example.com",
                "Original bio",
                "Phoenix, Arizona",
                "+16025550123"
        ));

        Profile updated = first.update(
                created.id(),
                new UpdateProfileRequest(
                        "Ubaid Updated",
                        "updated@example.com",
                        "Updated bio",
                        "Tempe, Arizona"
                )
        ).orElseThrow();

        FileProfileRepository second = newRepository();

        assertEquals(
                updated,
                second.findById(created.id()).orElseThrow()
        );
    }

    @Test
    void shouldKeepDeletionAndIdCounterAcrossRepositoryInstances() {
        FileProfileRepository first = newRepository();

        Profile created = first.saveNew(new ProfileRequest(
                "Ubaid",
                "ubaid@example.com",
                "Profile to delete",
                "Phoenix, Arizona",
                "+16025550123"
        ));

        assertTrue(first.deleteById(created.id()));

        FileProfileRepository second = newRepository();

        // The deleted profile must stay deleted.
        assertTrue(second.findById(created.id()).isEmpty());

        Profile next = second.saveNew(new ProfileRequest(
                "Alex",
                "alex@example.com",
                "New profile",
                "Scottsdale, Arizona",
                "+16025550123"
        ));

        // Deleting and reopening must not reset the ID counter.
        assertEquals(
                Long.valueOf(created.id() + 1),
                next.id()
        );
    }
}