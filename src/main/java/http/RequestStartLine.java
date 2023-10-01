package http;

import Enum.*;

public class RequestStartLine {
    private final HttpMethod method;
    private final HttpVersion httpVersion;
    private final Url url;

    public RequestStartLine(String startLine) {
        String[] requestComponents = startLine.split(" ");
        this.method = HttpMethod.valueOf(requestComponents[0]);
        this.url = Url.valueOf(requestComponents[1]);
        this.httpVersion = HttpVersion.valueOf((requestComponents[2]));
    }

    public HttpMethod getMethod() {
        return method;
    }

    public Url getUrl() {
        return url;
    }
}
