package edu.rpi.giveaway.post;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Category {
    FURNITURE("Furniture"),
    ELECTRONICS("Electronics"),
    TEXTBOOKS("Textbooks"),
    KITCHEN("Kitchen"),
    DORM_ESSENTIALS("Dorm essentials"),
    CLOTHING("Clothing"),
    BIKES("Bikes"),
    OTHER("Other");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static Category fromString(String value) {
        if (value == null) return null;
        for (Category category : Category.values()) {
            if (category.displayName.equalsIgnoreCase(value) || category.name().equalsIgnoreCase(value)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown category: " + value);
    }
}
