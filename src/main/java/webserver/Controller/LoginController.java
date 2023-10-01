package webserver.Controller;

import db.MemoryUserRepository;
import model.User;
import webserver.HttpRequest;
import webserver.HttpResponse;
import webserver.UrlPath;

public class LoginController implements Controller{
    private final MemoryUserRepository repository = MemoryUserRepository.getInstance();
    @Override
    public void execute(HttpRequest httpRequest, HttpResponse httpResponse) {
        String userId = httpRequest.getParameter("userId");
        User user = repository.findUserById(userId);
        if (user != null && user.getPassword().equals(httpRequest.getParameter("password"))) {
            httpResponse.addHeader("Set-Cookie", "logined=true");
            httpResponse.redirect(UrlPath.HOME_URL.getUrl());
        }
        httpResponse.redirect(UrlPath.LOGIN_FAILED_URL.getUrl());
    }
}
