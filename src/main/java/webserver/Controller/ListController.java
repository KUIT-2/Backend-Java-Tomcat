package webserver.Controller;

import webserver.HttpRequest;
import webserver.HttpResponse;
import webserver.Url;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static webserver.Url.ROOT;

public class ListController implements Controller{
    @Override
    public void execute(HttpRequest httpRequest, HttpResponse httpResponse) throws IOException {
        if(httpRequest.getCookie().contains("logined=true")) {
            //httpRequest.getMethod().isEqual("GET")
            httpResponse.setBody(Files.readAllBytes(Paths.get(ROOT.getUrl() + Url.LIST.getUrl())));
            httpResponse.response200Header();
            httpResponse.responseBody();
            return;
        }
        httpResponse.response302Header(Url.LOGIN.getUrl());
    }
}
