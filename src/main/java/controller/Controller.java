package controller;

import http.HttpRequest;
import http.HttpResponse;
import java.io.BufferedReader;

public interface Controller {
    public HttpResponse handleRequest(HttpRequest request);
}
