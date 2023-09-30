package Controller;

import webserver.HttpRequest;
import webserver.HttpResponse;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static webserver.URL.*;

public class ListController implements Controller {




    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            System.out.println("exception");
            //log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos, String path) {
        try {
            dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
            dos.writeBytes("Location: " + path + "\r\n");
            dos.writeBytes("\r\n");

        } catch (IOException e) {
            System.out.println("exception");
            //log.log(Level.SEVERE, e.getMessage());
        }
    }

    @Override
    public void responseBody(DataOutputStream dos, byte[] body) {

        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void execute(HttpRequest httpRequest, HttpResponse httpResponse) throws IOException {
        if (!httpRequest.getCookie().equals("logined=true")) {
            response302Header(httpResponse.dos, LOGIN_URL.getValue());
            return;
        }
        byte[] body = Files.readAllBytes(Paths.get(ROOT_URL.getValue() + LIST_URL.getValue()));
        response200Header(httpResponse.dos, body.length);
        responseBody(httpResponse.dos, body);
    }

}
