package webserver;

import webserver.Controller.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static webserver.Url.*;

public class RequestMapper {
    Map<String, Controller> controllers = new HashMap<>(){{
        put(HOME.getUrl(), new HomeController());
        put("/", new HomeController());
        put(LOGIN_FAILED.getUrl(), new ForwardController());
        put(FORM.getUrl(), new ForwardController());
        put("/user/signup", new SignUpController());
        put("/user/userList", new ListController());
        put("/user/login", new LoginController());
        put(LOGIN.getUrl(), new LoginController());
        put(CSS.getUrl(), new CssController());
    }};
    private HttpRequest httpRequest;
    private HttpResponse httpResponse;
    private Controller controller;

    public RequestMapper(HttpRequest httpRequest, HttpResponse httpResponse) {
        this.httpRequest = httpRequest;
        this.httpResponse = httpResponse;
        this.controller = controllers.get(httpRequest.getPath());
    }

    public void proceed() throws IOException {
        controller.execute(httpRequest, httpResponse);
    }
}
