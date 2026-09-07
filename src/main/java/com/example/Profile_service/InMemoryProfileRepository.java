package com.example.Profile_service;

import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

//@Repository
public class InMemoryProfileRepository {

    private final ConcurrentHashMap<Long, Profile> profiles =
            new ConcurrentHashMap<>();

    private final AtomicLong nextId = new AtomicLong();

    public Profile saveNew(ProfileRequest request) {
        long id = nextId.incrementAndGet();
        Profile profile = new Profile(
                id,
                request.name(),
                request.email(),
                request.bio()
        );

        profiles.put(id, profile);
        return profile;
    }

    public Optional<Profile> findById(Long id) {
        return Optional.ofNullable(profiles.get(id));
    }


    public Optional<Profile> update(Long id, ProfileRequest request) {
        Profile updated = profiles.computeIfPresent(id, (key, existing) ->
                new Profile(
                        key,
                        request.name(),
                        request.email(),
                        request.bio()
                )
        );

        return Optional.ofNullable(updated);
    }

    //DELETE
    public boolean deleteById(Long id) {
        return profiles.remove(id) != null;
    }

}
