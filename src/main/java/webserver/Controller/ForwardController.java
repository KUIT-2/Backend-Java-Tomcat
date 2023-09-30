package webserver.Controller;

import webserver.HttpRequest;
import webserver.HttpResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static webserver.Url.ROOT;

public class ForwardController implements Controller{

    @Override
    public void execute(HttpRequest httpRequest, HttpResponse httpResponse) throws IOException {
        httpResponse.setBody(Files.readAllBytes(Paths.get(ROOT.getUrl() + httpRequest.getPath())));
        httpResponse.response200Header();
        httpResponse.responseBody();
    }
}
