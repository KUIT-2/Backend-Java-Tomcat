package http;

import java.io.BufferedReader;
import java.io.IOException;
import Enum.*;

public class HttpRequest {

    private RequestStartLine requestStartLine;
    private int contentLength;
    private String cookie = null;
    private BufferedReader br;

    public HttpRequest(BufferedReader br) throws IOException {
        this.br = br;
        this.requestStartLine = new RequestStartLine(br.readLine());
    }

    public int getContentLength() {
        return contentLength;
    }

    public String getCookie() {
        return cookie;
    }

    private void parseRequest(BufferedReader br) throws IOException {

        while(true) {
            String line = br.readLine();
            if (line.isEmpty()) {
                break;
            }
            if (line.startsWith(HttpHeader.CONTENT_LENGTH.getHeader())) {
                contentLength = Integer.parseInt(line.split(": ")[1]);
            }
            if (line.startsWith(HttpHeader.COOKIE.getHeader())) {
                cookie = line.split(": ")[1];
            }
        }
    }
}
