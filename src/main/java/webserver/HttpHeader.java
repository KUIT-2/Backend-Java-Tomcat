package webserver;

public enum HttpHeader {
    CONTENT_TYPE_HTML("Content-Type: text/html;charset=utf-8\r\n"),
    CONTENT_TYPE_CSS("Content-Type: text/css;charset=utf-8\r\n"),
    CONTENT_LENGTH("Content-Length: "),
    LOCATION("Location: "),
    COOKIE("Cookie: ");

    private String message;

    HttpHeader(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
