package webserver;

public enum EnumHTTPStatusCode {
    OK_200(200), Found_302(302);

    private final int value;

    EnumHTTPStatusCode(int statusCode) {
        this.value = statusCode;
    }

    public int getValue() {
        return value;
    }
}
