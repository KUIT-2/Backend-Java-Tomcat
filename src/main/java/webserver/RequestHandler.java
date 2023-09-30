package webserver;

import webserver.Controller.*;
import db.MemoryUserRepository;
import http.util.HttpRequestUtils;
import http.util.IOUtils;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static webserver.Url.*;

public class RequestHandler implements Runnable {
    Socket connection;
    private static final Logger log = Logger.getLogger(RequestHandler.class.getName());
    private  Controller controller = new ForwardController();

    public RequestHandler(Socket connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        log.log(Level.INFO, "New Client Connect! Connected IP : " + connection.getInetAddress() + ", Port : " + connection.getPort());
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {

            HttpRequest httpRequest = HttpRequest.from(new BufferedReader(new InputStreamReader(in)));
            HttpResponse httpResponse = HttpResponse.from(new DataOutputStream(out));

            RequestMapper requestMapper = new RequestMapper(httpRequest, httpResponse);
            requestMapper.proceed();


        } catch (
                IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }

    }

}