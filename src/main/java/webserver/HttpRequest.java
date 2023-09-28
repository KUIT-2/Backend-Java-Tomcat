package webserver;

import java.io.BufferedReader;
import java.io.IOException;

public class HttpRequest {

    private BufferedReader br;
    private RequestStartLine requestStartLine;
    private HttpHeaders httpHeaders;

    public static HttpRequest from(BufferedReader br) throws IOException {
        return new HttpRequest(br);
    }
    public HttpRequest(BufferedReader br) throws IOException {
        this.br = br;
        this.requestStartLine = new RequestStartLine(br.readLine());
        this.httpHeaders = new HttpHeaders(br);
    }

    public HttpMethod getMethod() {
        return requestStartLine.getMethod();
    }

    public String getPath() {
        return requestStartLine.getPath();
    }

    public String getCookie() {
        return httpHeaders.getCookie();
    }

    public int getContentLength() {
        return httpHeaders.getContentLength();
    }

    public BufferedReader getBr() {
        return br;
    }
}
