package webserver;

public enum RequestHeaderKey {
    CONTENT_LENGTH("Content-Length"),
    COOKIE("Cookie");

    private final String key;

    RequestHeaderKey(String key){
        this.key = key;
    }

    public String getKey(){
        return this.key;
    }
}
