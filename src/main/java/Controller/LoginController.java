package Controller;

import db.Repository;
import http.util.IOUtils;
import model.User;
import webserver.HttpRequest;
import webserver.HttpResponse;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

import static http.util.HttpRequestUtils.getQueryParameter;
import static webserver.URL.HOME_URL;
import static webserver.URL.LOGIN_FAILED_URL;

public class LoginController implements Controller {

    Repository repository;

    @Override
    public void responseBody(DataOutputStream dos, byte[] body) {

    }

    @Override
    public void execute(HttpRequest httpRequest, HttpResponse httpResponse) throws IOException {
        String queryString = IOUtils.readData(httpRequest.br, httpRequest.getRequestContentLength());
        Map<String, String> queryParameter = getQueryParameter(queryString);
        User user = repository.findUserById(queryParameter.get("userId"));
        login(httpResponse.dos, queryParameter, user);
    }

    private void login(DataOutputStream dos, Map<String, String> queryParameter, User user) {
        if (user != null && user.getPassword().equals(queryParameter.get("password"))) {
            response302HeaderWithCookie(dos, HOME_URL.getValue());
            return;
        }
        response302Header(dos, LOGIN_FAILED_URL.getValue());

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

    private void response302HeaderWithCookie(DataOutputStream dos, String path) {
        try{
            dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
            dos.writeBytes("Location: " + path + "\r\n");
            dos.writeBytes("Set-Cookie: logined=true" + "\r\n");

            dos.writeBytes("\r\n");
        } catch (IOException e) {
            System.out.println("exception");
            //log.log(Level.SEVERE, e.getMessage());
        }
    }




}
