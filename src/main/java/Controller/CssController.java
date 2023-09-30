package Controller;

import webserver.HttpRequest;
import webserver.HttpResponse;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static webserver.URL.ROOT_URL;

public class CssController implements Controller{
    @Override
    public void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void response200HeaderWithContentType(DataOutputStream dos, int lengthOfBodyContent, String contentType) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: " + contentType + ";charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            //log.log(Level.SEVERE, e.getMessage());
        }
    }

    @Override
    public void execute(HttpRequest httpRequest, HttpResponse httpResponse) throws IOException {
        byte[] body = Files.readAllBytes(Paths.get(ROOT_URL + httpRequest.getUrl()));
        String contentType = "image/jpeg";

        if (httpRequest.getUrl().endsWith(".css")) {
            contentType = "text/css";
        } else if (httpRequest.getUrl().endsWith("png")) {
            contentType = "image/png";
        } else if (httpRequest.getUrl().endsWith(".gif")) {
            contentType = "image/gif";
        }

        response200HeaderWithContentType(httpResponse.dos, body.length, contentType);
        responseBody(httpResponse.dos, body);

    }
}
