package webserver;

public enum HttpMethod {

    GET("GET"),
    POST("POST");

    private String httpMethod;

    HttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public boolean isEqual(String httpMethod) {
        return this.httpMethod == httpMethod;
    }
}
