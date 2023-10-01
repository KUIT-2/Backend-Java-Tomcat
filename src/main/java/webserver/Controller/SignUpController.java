package webserver.Controller;

import db.MemoryUserRepository;
import model.User;
import webserver.Controller.Controller;
import webserver.HttpRequest;
import webserver.HttpResponse;
import webserver.UrlPath;

public class SignUpController implements Controller {
    private final MemoryUserRepository repository = MemoryUserRepository.getInstance();

    @Override
    public void execute(HttpRequest httpRequest, HttpResponse httpResponse) {
        User user = new User(
                httpRequest.getParameter("userId"),
                httpRequest.getParameter("password"),
                httpRequest.getParameter("name"),
                httpRequest.getParameter("email")
        );
        repository.addUser(user);
        httpResponse.redirect(UrlPath.HOME_URL.getUrl());
    }
}