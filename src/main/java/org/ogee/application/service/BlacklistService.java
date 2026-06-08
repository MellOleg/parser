package org.ogee.application.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BlacklistService {

    private static final Path BLACKLIST_PATH = Path.of("blacklist.txt");

    public Set<String> getBlacklistedItemIds() {
        if (!Files.exists(BLACKLIST_PATH)) {
            return Set.of();
        }

        try {
            return Files.readAllLines(BLACKLIST_PATH)
                    .stream()
                    .map(String::trim)
                    .filter(line -> !line.isBlank())
                    .filter(line -> !line.startsWith("#"))
                    .collect(Collectors.toSet());

        } catch (IOException e) {
            throw new RuntimeException("Failed to read blacklist file", e);
        }
    }

    public boolean isBlacklisted(String itemId) {
        return getBlacklistedItemIds().contains(itemId);
    }
}