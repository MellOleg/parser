package org.ogee.domain.model;

public class ItemInfo {

    private final String id;
    private final String name;

    public ItemInfo(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}