package webserver;

public enum EnumHTTPVersion {
    HTTP_1_1("HTTP/1.1");

    private final String value;


    EnumHTTPVersion(String version) {
        this.value = version;
    }

    public String getValue() {
        return this.value;
    }
}
