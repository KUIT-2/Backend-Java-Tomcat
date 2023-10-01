package webserver.Controller;

import webserver.HttpRequest;
import webserver.HttpResponse;
import webserver.UrlPath;

public class HomeController implements Controller{
    @Override
    public void execute(HttpRequest httpRequest, HttpResponse httpResponse) {
        httpResponse.forward(UrlPath.HOME_URL.getUrl());
    }
}
