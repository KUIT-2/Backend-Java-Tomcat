package webserver;

import com.sun.source.tree.UsesTree;
import http.util.HttpRequestUtils;
import http.util.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpRequest {

    private StartLine startLine;
    private RequestHeader requestHeader;
    private Map<String, String> body;
    private static final Logger log = Logger.getLogger(RequestHandler.class.getName());

    HttpRequest(BufferedReader br){
        setStartLine(br);
        setRequestHeader(br);
        setBody(br);
    }

    public static HttpRequest from(BufferedReader br){
        return new HttpRequest(br);
    }
    private void setStartLine(BufferedReader br){
        try {
            String[] line = br.readLine().split(" ");
            startLine = new StartLine(line[0], line[1], line[2]);
        } catch (IOException e) {
            log.log(Level.SEVERE,e.getMessage());
        }
    }

    private void setRequestHeader(BufferedReader br){
        try{
            int bodyLength=0;
            boolean cookie = false;
            while(true){
                String line = br.readLine();
                if(line.isEmpty()){
                    break;
                }
                if(line.startsWith(RequestHeaderKey.CONTENT_LENGTH.getKey())){
                    bodyLength = Integer.parseInt(line.split(": ")[1]);
                }
                if(line.startsWith(RequestHeaderKey.COOKIE.getKey())){
                    cookie = Boolean.parseBoolean(line.split(": ")[1].split("=")[1]);
                }
            }
            requestHeader = new RequestHeader(bodyLength, cookie);
        } catch (IOException e) {
            log.log(Level.SEVERE,e.getMessage());
        }
    }

    private void setBody(BufferedReader br){
        try {
            String requestBody = IOUtils.readData(br, this.requestHeader.getContent_length());
            this.body = HttpRequestUtils.parseQueryParameter(requestBody);
        } catch (IOException e) {
            log.log(Level.SEVERE,e.getMessage());
        }
    }

    public String getMethod(){
        return this.startLine.getMethod();
    }

    public String getPath(){
        return this.startLine.getPath();
    }

    public String getVersion(){
        return this.startLine.getVersion();
    }

    public int getContent_length(){
        return this.requestHeader.getContent_length();
    }

    public boolean getCookie(){
        return this.requestHeader.getCookie();
    }

    public String getBodyUserId(){
        return this.body.get(UserInfoElement.USER_ID.getElement());
    }

    public String getBodyPassword(){
        return this.body.get(UserInfoElement.USER_PASSWORD.getElement());
    }

    public String getBodyName(){
        return this.body.get(UserInfoElement.USER_NAME.getElement());
    }

    public String getBodyEmail(){
        return this.body.get(UserInfoElement.USER_EMAIL.getElement());
    }

}
