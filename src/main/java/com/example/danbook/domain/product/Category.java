package com.example.danbook.domain.product;

public enum Category {
    HEALTH_FOOD(MainCategory.FOOD, "건강식품"),
    SEASONING(MainCategory.FOOD, "조미료"),

    KITCHENWARE(MainCategory.LIFE_HEALTH, "주방용품"),
    STATIONERY(MainCategory.LIFE_HEALTH, "문구/사무용품"),
    BATHROOM_SUPPLIES(MainCategory.LIFE_HEALTH, "욕실용품"),

    FASHION_ACCESSORY(MainCategory.FASHION, "패션소품"),
    WOMEN_BAG(MainCategory.FASHION, "여성가방"),
    SOCKS(MainCategory.FASHION, "양말"),
    TRAVEL_BAG_ACCESSORY(MainCategory.FASHION, "여행용 가방/소품"),

    NAIL_CARE(MainCategory.BEAUTY, "네일케어"),
    MASK_PACK(MainCategory.BEAUTY, "마스크/팩"),

    PHONE_ACCESSORY(MainCategory.DIGITAL_APPLIANCE, "휴대폰 액세서리"),
    PERIPHERAL_DEVICE(MainCategory.DIGITAL_APPLIANCE, "주변기기"),
    LAPTOP_ACCESSORY(MainCategory.DIGITAL_APPLIANCE, "노트북 액세서리"),
    SEASONAL_APPLIANCE(MainCategory.DIGITAL_APPLIANCE, "계절 가전"),

    GOLF(MainCategory.SPORTS_LEISURE, "골프"),
    HIKING(MainCategory.SPORTS_LEISURE, "등산"),

    INTERIOR_ACCESSORY(MainCategory.FURNITURE_INTERIOR, "인테리어 소품"),
    HOME_DECOR(MainCategory.FURNITURE_INTERIOR, "홈데코");

    private final MainCategory mainCategory;
    private final String displayName;

    Category(MainCategory mainCategory, String displayName) {
        this.mainCategory = mainCategory;
        this.displayName = displayName;
    }

    public MainCategory getMainCategory() {
        return mainCategory;
    }

    public String getDisplayName() {
        return displayName;
    }
}
