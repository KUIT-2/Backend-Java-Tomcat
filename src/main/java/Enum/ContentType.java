package Enum;

public enum ContentType {
    HTML("text.html"), CSS("text/css"), PNG("image/png"), JPEG("image/jpeg");

    private final String value;

    ContentType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
