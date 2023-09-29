package Controller;

import db.Repository;
import http.util.IOUtils;
import model.User;
import webserver.HttpRequest;
import webserver.HttpResponse;
import webserver.URL;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

import static http.util.HttpRequestUtils.getQueryParameter;

public class SignUpController implements Controller {

    Repository repository;
    HttpRequest httpRequest;
    HttpResponse httpResponse;


    @Override
    public void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    private void response302Header(DataOutputStream dos, String path) {
        try {
            dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
            dos.writeBytes("Location: " + path + "\r\n");
            dos.writeBytes("\r\n");

            httpResponse.redirect(URL.LOGIN_FAILED_URL.getValue());

        } catch (IOException e) {
            System.out.println("exception");
            //log.log(Level.SEVERE, e.getMessage());
        }
    }

    @Override
    public void execute(HttpRequest httpRequest, HttpResponse httpResponse) throws IOException {
        this.httpRequest = httpRequest;
        this.httpResponse = httpResponse;

        String queryString = IOUtils.readData(httpRequest.br, httpRequest.getRequestContentLength());
        Map<String, String> queryParameter = getQueryParameter(queryString);
        User user = new User(queryParameter.get("userId"), queryParameter.get("password"), queryParameter.get("name"), queryParameter.get("email"));
        repository.addUser(user);

        response302Header(httpResponse.dos, httpRequest.getUrl());
    }


}
