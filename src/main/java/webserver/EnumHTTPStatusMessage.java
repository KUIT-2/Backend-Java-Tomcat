package webserver;

public enum EnumHTTPStatusMessage {
    OK_200("OK"), FOUND_302("Found");

    private final String value;

    EnumHTTPStatusMessage(String message) {
        this.value = message;
    }

    public String getValue() {
        return this.value;
    }
}
