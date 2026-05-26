package com.launchforcar.carlauncher.data.model;

public class AppItem {

    private final String name;
    private final String category;

    public AppItem(String name, String category) {
        this.name = name;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }
}