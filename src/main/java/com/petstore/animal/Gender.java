package com.petstore.animal;

/**
 * 动物性别枚举（统一使用 雄 / 雌）。
 *
 * @author pet-store
 */
public enum Gender {

    /** 雄性 */
    MALE("雄"),

    /** 雌性 */
    FEMALE("雌");

    private final String displayName;

    Gender(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
