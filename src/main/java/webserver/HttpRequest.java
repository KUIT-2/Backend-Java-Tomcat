package webserver;

import java.io.BufferedReader;
import java.io.IOException;

public class HttpRequest {

    public BufferedReader br;
    static String[] startLines;
    static int requestContentLength;
    static String cookie;


    public HttpRequest() {
    }

    public static HttpRequest from(BufferedReader br) throws IOException {

        String startLine;
        startLine = br.readLine();
        startLines = startLine.split(" ");

        while (true) {
            final String line = br.readLine();
            if (line.isEmpty()) { //한줄 비어있으면 밑은 body니까
                break;
            }
            if (line.startsWith("Content-length")) {
                requestContentLength = Integer.parseInt(line.split(": ")[1]);
            }
            if (line.startsWith("Cookie")) {
                cookie = line.split(": ")[1];
            }
        }


        return new HttpRequest();
    }

    public int getRequestContentLength() {
        return requestContentLength;
    }

    public String getCookie() {
        return cookie;
    }

    public String getMethod() {
        return startLines[0];
    }

    public String getUrl() {
        return startLines[1];
    }
}
