package dev.azure;

public final class Feature {
    public enum Category {
        BASE("Base finding"),
        VISUAL("Visual");

        public final String label;

        Category(String label) {
            this.label = label;
        }
    }

    public final String name;
    public final String desc;
    public final Category category;
    public boolean enabled;

    public Feature(String name, String desc, Category category, boolean enabled) {
        this.name = name;
        this.desc = desc;
        this.category = category;
        this.enabled = enabled;
    }

    public void toggle() {
        enabled = !enabled;
    }
}
