package org.ogee.application.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class WatchlistService {

    private static final Path WATCHLIST_PATH = Path.of("watchlist.txt");

    public List<String> getItemIds() {
        if (!Files.exists(WATCHLIST_PATH)) {
            System.out.println("Watchlist file not found: " + WATCHLIST_PATH.toAbsolutePath());
            return List.of();
        }

        try {
            return Files.readAllLines(WATCHLIST_PATH)
                    .stream()
                    .map(String::trim)
                    .filter(line -> !line.isBlank())
                    .filter(line -> !line.startsWith("#"))
                    .distinct()
                    .toList();

        } catch (IOException e) {
            throw new RuntimeException("Failed to read watchlist file", e);
        }
    }

    public int getWatchlistSize() {
        return getItemIds().size();
    }
}