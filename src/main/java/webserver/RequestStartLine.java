package webserver;

public class RequestStartLine {

    private HttpMethod method;
    private String path;
    private String version;

    public RequestStartLine(String startLine) {
        String[] startLineArr = startLine.split(" ");
        this.method = HttpMethod.valueOf(startLineArr[0]);
        this.path = startLineArr[1];
        this.version = startLineArr[2];
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }
}
