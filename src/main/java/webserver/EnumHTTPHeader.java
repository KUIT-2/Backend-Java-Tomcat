package webserver;

public enum EnumHTTPHeader {
    ContentTypeHtml("Content-Type: text/html;charset=utf-8"),
    ContentTypeCSS("Content-Type: text/css;charset=utf-8"),
    ContentLength("Content-Length: "),
    Location("Location: "),
    SetCookie("Set-Cookie: ");

    private final String value;

    EnumHTTPHeader(String header) {
        this.value = header;
    }

    public String getValue() {
        return this.value;
    }
}
