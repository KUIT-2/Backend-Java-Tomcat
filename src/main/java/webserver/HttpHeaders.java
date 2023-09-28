package webserver;

import java.io.BufferedReader;
import java.io.IOException;

public class HttpHeaders {
    private String cookie="";
    private int contentLength;
    private BufferedReader br;

    public HttpHeaders(BufferedReader br) throws IOException {
        this.br = br;
        while(true) {
            final String line = br.readLine();
            if (line.equals("")) break;
            if(line.startsWith(HttpHeader.COOKIE.getMessage())) {
                this.cookie = line.split(": ")[1];
            }
            if (line.startsWith(HttpHeader.CONTENT_LENGTH.getMessage())) {
                this.contentLength = Integer.parseInt(line.split(": ")[1]);
            }
        }
    }

    public String getCookie() {
        return cookie;
    }

    public int getContentLength() {
        return contentLength;
    }
}
