package de.technikteam.model;

public class NavigationItem {
    private final String label;
    private final String url;
    private final String icon;
    private final String requiredPermission;

    public NavigationItem(String label, String url, String icon, String requiredPermission) {
        this.label = label;
        this.url = url;
        this.icon = icon;
        this.requiredPermission = requiredPermission;
    }

    public String getLabel() {
        return label;
    }

    public String getUrl() {
        return url;
    }

    public String getIcon() {
        return icon;
    }

    public String getRequiredPermission() {
        return requiredPermission;
    }
}