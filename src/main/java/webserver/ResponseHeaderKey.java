package webserver;

public enum ResponseHeaderKey {
    OK_202("HTTP/1.1 200 OK"),
    REDIRECT_302("HTTP/1.1 302 Redirect"),
    CONTENT_TYPE_KEY_HTML("Content-Type: text/html;charset=utf-8"),
    CONTENT_TYPE_KEY("Content-Type: "),
    CONTENT_LENGTH_KEY("Content-Length: "),
    LOCATION_KEY("Location: "),
    SET_COOKIE_KEY("Set-Cookie: "),
    LOGIN_TRUE("logined=true"),
    LINE_BREAK("\r\n");

    private final String key;

    ResponseHeaderKey(String key){
        this.key = key;
    }

    public String getKey(){
        return this.key;
    }
}
