package webserver;

public enum HttpMethod {
    GET("GET"),
    POST("POST"),
    PUt("PUT");

    private  final String method;

    HttpMethod(String method){
        this.method = method;
    }

    public String getMethod(){
        return this.method;
    }
}
