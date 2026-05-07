package com.example.danbook.domain.product;

public enum MainCategory {
    FOOD("식품"),
    LIFE_HEALTH("생활/건강"),
    FASHION("패션잡화"),
    BEAUTY("화장품/미용"),
    DIGITAL_APPLIANCE("디지털/가전"),
    SPORTS_LEISURE("스포츠/레저"),
    FURNITURE_INTERIOR("가구/인테리어");

    private final String displayName;

    MainCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
