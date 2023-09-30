package webserver.Controller;

import db.MemoryUserRepository;
import http.util.HttpRequestUtils;
import http.util.IOUtils;
import model.User;
import webserver.HttpRequest;
import webserver.HttpResponse;

import java.io.IOException;
import java.util.Map;

public class SignUpController implements Controller{
    @Override
    public void execute(HttpRequest httpRequest, HttpResponse httpResponse) throws IOException {
        //httpRequest.getMethod().isEqual("POST")
        String requestsBody = IOUtils.readData(httpRequest.getBr(), httpRequest.getContentLength());

        Map<String, String> requestsParameter = HttpRequestUtils.parseQueryParameter(requestsBody);
        User user = new User(requestsParameter.get("userId"),
                requestsParameter.get("password"),
                requestsParameter.get("name"),
                requestsParameter.get("email"));
        MemoryUserRepository.getInstance().addUser(user);
        httpResponse.response302Header("/index.html");
    }
}
