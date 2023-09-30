package webserver.Controller;

import webserver.HttpRequest;
import webserver.HttpResponse;
import webserver.Url;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static webserver.Url.ROOT;

public class HomeController implements Controller{

    @Override
    public void execute(HttpRequest httpRequest, HttpResponse httpResponse) throws IOException {
        httpResponse.setBody(Files.readAllBytes(Paths.get(ROOT.getUrl() + Url.HOME.getUrl())));
        httpResponse.response200Header();
        httpResponse.responseBody();
    }
}
