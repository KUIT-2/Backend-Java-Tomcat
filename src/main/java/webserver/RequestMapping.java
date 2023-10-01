package webserver;

import webserver.Controller.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RequestMapping {
    private static final Map<String, Controller> controllers = new HashMap<>();
    private final Controller controller;
    private final HttpResponse response;
    private final HttpRequest request;

    public RequestMapping(HttpRequest request, HttpResponse response) {
        this.request = request;
        this.response = response;
        controller = controllers.get(request.getUrl());
    }
    static {
        controllers.put(UrlPath.SIGN_UP.getUrl(), new SignUpController());
        controllers.put(UrlPath.LOGIN_PATH.getUrl(), new LoginController());
        controllers.put(UrlPath.LIST_PATH.getUrl(), new ListController());
        controllers.put(UrlPath.HOME_URL.getUrl(), new HomeController());
        controllers.put("/", new HomeController());
    }
    public void proceed() throws IOException {
        if (controller != null) {
            controller.execute(request, response);
            return;
        }
        response.forward(request.getUrl());
    }
}