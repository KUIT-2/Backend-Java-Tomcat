package controller;

import static Enum.ContentType.HTML;
import static Enum.HttpStatus.OK;
import static Enum.HttpStatus.SERVVER_ERROR;
import static Enum.Url.HOME;
import static Enum.Url.ROOT;

import http.HttpRequest;
import http.HttpResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class HomeController implements Controller {
    @Override
    public HttpResponse handleRequest(HttpRequest request) {

    }
}
