package webserver.Controller;

import db.MemoryUserRepository;
import http.util.HttpRequestUtils;
import http.util.IOUtils;
import webserver.HttpRequest;
import webserver.HttpResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.logging.Level;

import static webserver.Url.*;

public class LoginController implements Controller{
    @Override
    public void execute(HttpRequest httpRequest, HttpResponse httpResponse) throws IOException {
        if(httpRequest.getMethod().isEqual("POST")){
            String requestsBody = IOUtils.readData(httpRequest.getBr(), httpRequest.getContentLength());

            Map<String, String> requestsParameter = HttpRequestUtils.parseQueryParameter(requestsBody);
            String userId = requestsParameter.get("userId");
            String password = requestsParameter.get("password");

            if (MemoryUserRepository.getInstance().validateUser(userId, password)) {
                httpResponse.responseLoginSuccess(HOME.getUrl());
                return;
            }
            httpResponse.response302Header(LOGIN_FAILED.getUrl());
            return;
        }

        httpResponse.setBody(Files.readAllBytes(Paths.get(ROOT.getUrl() + LOGIN.getUrl())));
        httpResponse.response200Header();
        httpResponse.responseBody();
    }

}
