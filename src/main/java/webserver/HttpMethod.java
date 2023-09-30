package webserver;

public enum HttpMethod {
    GET("GET"), POST("POST");

    private final String method;

    HttpMethod(String method) {
        this.method = method;
    }


    public String getValue() {
        return method;
    }
}
