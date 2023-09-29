package Controller;

import webserver.HttpRequest;
import webserver.HttpResponse;

import java.io.DataOutputStream;
import java.io.IOException;

public interface Controller {

    byte[] body = new byte[0];

    void responseBody(DataOutputStream dos, byte[] body);

    void execute(HttpRequest httpRequest, HttpResponse httpResponse) throws IOException;
}
