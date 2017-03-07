package org.karivar.utils.domain;


public class User {
    private String name;
    private String displayName;

    public User(String name, String displayName) {
        this.name = name;
        this.displayName = displayName;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }
}
