package webserver;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpResponse {

    private final DataOutputStream dos;
    private static final Logger log = Logger.getLogger(RequestHandler.class.getName());
    HttpResponse(DataOutputStream dos){
        this.dos = dos;
    }

    public static HttpResponse to(DataOutputStream dos){
        return new HttpResponse(dos);
    }

    public void forward(String path){
        try {
            byte[] body = Files.readAllBytes(Paths.get(path));
            set200Header(body.length, ResponseHeaderKey.CONTENT_TYPE_KEY_HTML.getKey());
            setBody(body);
        }catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    public void forwardContentType(String path, String content_type){
        try {
            byte[] body = Files.readAllBytes(Paths.get(path));
            set200Header(body.length, "Content-Type: " + content_type);
            setBody(body);
        }catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }


    public void redirect(String path){
        set302Header(path,false);
    }

    public void redirectWithCookie(String path){
        set302Header(path, true);
    }

    private void set200Header(int lengthOfBodyContent,String key){
        try {
            dos.writeBytes(ResponseHeaderKey.OK_202.getKey() + ResponseHeaderKey.LINE_BREAK.getKey());
            dos.writeBytes(key + ResponseHeaderKey.LINE_BREAK.getKey());
            dos.writeBytes(ResponseHeaderKey.CONTENT_LENGTH_KEY.getKey() + lengthOfBodyContent + ResponseHeaderKey.LINE_BREAK.getKey());
            dos.writeBytes(ResponseHeaderKey.LINE_BREAK.getKey());
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void set302Header(String path, boolean cookie){
        try {
            dos.writeBytes(ResponseHeaderKey.REDIRECT_302.getKey() + ResponseHeaderKey.LINE_BREAK.getKey());
            // 브라우저에게 redirect을 명시하는 http header 양식
            dos.writeBytes(ResponseHeaderKey.LOCATION_KEY.getKey() + path + ResponseHeaderKey.LINE_BREAK.getKey());
            if(cookie){
                dos.writeBytes(ResponseHeaderKey.SET_COOKIE_KEY.getKey() + ResponseHeaderKey.LOGIN_TRUE.getKey() + ResponseHeaderKey.LINE_BREAK.getKey());
            }
            dos.writeBytes(ResponseHeaderKey.LINE_BREAK.getKey());
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void setBody(byte[] body){
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }




}
