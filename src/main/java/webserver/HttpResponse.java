package webserver;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpResponse {
    private static final Logger log = Logger.getLogger(RequestHandler.class.getName());
    private final DataOutputStream dos;
    private final Map<String, String> headers = new HashMap<>();
    public HttpResponse(DataOutputStream dos) {
        this.dos = dos;
    }
    public static HttpResponse from(DataOutputStream dos){
        return new HttpResponse(dos);
    }

    public void forward(String url) {
        try {
            byte[] body = Files.readAllBytes(new File(UrlPath.ROOT_URL.getUrl() + url).toPath());
            if (url.endsWith(".css")) {
                headers.put("Content-Type", "text/css");
            }
            headers.put("Content-Length", String.valueOf(body.length));
            response200Header(body.length);
            responseBody(body);
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    public Map<String, String> getHeaders(){return headers;}



    public void redirect(String url) {
        try {
            dos.writeBytes("HTTP/1.1 302 Redirect \r\n");

            processHeader();

            dos.writeBytes("Location: " + url + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }

    }

    private void processHeader() {
        try{
            Set<String> keys = headers.keySet();
            for(String key : keys){
                dos.writeBytes(key+ ": " + headers.get(key) + " \r\n");
            }
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    public void addHeader(String key, String val) {
        headers.put(key ,val);
    }


    private void response200Header(int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void responseBody(byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.writeBytes("\r\n");
            dos.flush();
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }
}
