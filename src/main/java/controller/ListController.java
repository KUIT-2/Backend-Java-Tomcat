package controller;

import static Enum.HttpStatus.FOUND;
import static Enum.HttpStatus.OK;
import static Enum.Url.HOME;
import static Enum.Url.LIST;
import static Enum.Url.ROOT;

import http.HttpRequest;
import http.HttpResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ListController implements Controller{

    @Override
    public HttpResponse handleRequest(HttpRequest request) {

    }
}
