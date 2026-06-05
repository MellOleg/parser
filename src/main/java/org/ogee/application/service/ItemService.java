package org.ogee.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ogee.domain.model.ItemInfo;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@Service
public class ItemService {

    private static final Path ITEMS_PATH = Path.of("items");

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, ItemInfo> itemsById = new HashMap<>();

    @PostConstruct
    public void loadItems() {
        if (!Files.exists(ITEMS_PATH)) {
            System.out.println("Items folder not found: " + ITEMS_PATH.toAbsolutePath());
            return;
        }

        try (Stream<Path> paths = Files.walk(ITEMS_PATH)) {
            paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".json"))
                    .forEach(this::loadItemFile);

            System.out.println("Loaded items: " + itemsById.size());

        } catch (IOException e) {
            throw new RuntimeException("Failed to read items database", e);
        }
    }

    private void loadItemFile(Path path) {
        try {
            JsonNode root = objectMapper.readTree(path.toFile());

            String id = getText(root, "id");

            if (id == null || id.isBlank()) {
                id = getItemIdFromFileName(path);
            }

            String name = getLocalizedName(root, "ru");

            if (name == null || name.isBlank()) {
                name = getLocalizedName(root, "en");
            }

            if (name == null || name.isBlank()) {
                name = id;
            }

            itemsById.put(id, new ItemInfo(id, name));

        } catch (Exception e) {
            System.out.println("Failed to load item file: " + path + " | " + e.getMessage());
        }
    }

    private String getLocalizedName(JsonNode root, String language) {
        JsonNode node = root.path("name")
                .path("lines")
                .path(language);

        if (node.isMissingNode() || node.isNull()) {
            return null;
        }

        return node.asText();
    }

    private String getText(JsonNode root, String fieldName) {
        JsonNode node = root.path(fieldName);

        if (node.isMissingNode() || node.isNull()) {
            return null;
        }

        return node.asText();
    }

    private String getItemIdFromFileName(Path path) {
        String fileName = path.getFileName().toString();

        if (fileName.endsWith(".json")) {
            return fileName.substring(0, fileName.length() - ".json".length());
        }

        return fileName;
    }

    public String getItemName(String itemId) {
        ItemInfo itemInfo = itemsById.get(itemId);

        if (itemInfo == null) {
            return itemId;
        }

        return itemInfo.getName();
    }

    public ItemInfo getItemInfo(String itemId) {
        return itemsById.get(itemId);
    }

    public int getLoadedItemsCount() {
        return itemsById.size();
    }
}