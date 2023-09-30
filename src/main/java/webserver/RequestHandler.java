package webserver;

// 내 폴더 클래스 import
import db.MemoryUserRepository;
import db.Repository;
import http.util.IOUtils;
import http.util.HttpRequestUtils;
import model.User;

// java API import
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RequestHandler implements Runnable{
    Socket connection;
    private final Repository userDB;
    private static final Logger log = Logger.getLogger(RequestHandler.class.getName());

    public RequestHandler(Socket connection) {
        this.connection = connection;
        userDB = MemoryUserRepository.getInstance();
    }

    @Override
    public void run() {
        log.log(Level.INFO, "New Client Connect! Connected IP : " + connection.getInetAddress() + ", Port : " + connection.getPort());
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()){

            // BufferedReader 란 인자로 취한 스트림에 버퍼링 기능을 추가한 입력 스트립 클래스이다. 똑같이 입력 스트림이지만 버퍼링 기능 추가
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            DataOutputStream dos = new DataOutputStream(out);

            // requestprinter(br);

            // startLine 추출 -> 로직 분기
            String[] startLine = br.readLine().split(" ");
            String method = startLine[0];
            String uri = startLine[1];
            //System.out.println(method);

            int bodyLength=0;
            boolean cookie = false;
            while(true){
                String line = br.readLine();
                if(line.isEmpty()){
                    break;
                }
                if(line.startsWith("Content-Length")){
                    bodyLength = Integer.parseInt(line.split(": ")[1]);
                }
                if(line.startsWith("Cookie")){
                    cookie = Boolean.parseBoolean(line.split(": ")[1].split("=")[1]);
                }
            }

            if(method.equals("GET")){
                if(uri.equals("/") || uri.equals("/index.html")){
                    byte[] body = Files.readAllBytes(Paths.get("C:/Users/권민혁/Desktop/2-2/KUIT/3week/Backend-Java-Tomcat/webapp/index.html"));
                    response200Header(dos, body.length);
                    responseBody(dos, body);
                    return;
                }
                if(uri.equals("/user/form.html")){
                    byte[] body = Files.readAllBytes(Paths.get("C:/Users/권민혁/Desktop/2-2/KUIT/3week/Backend-Java-Tomcat/webapp/user/form.html"));
                    response200Header(dos, body.length);
                    responseBody(dos, body);
                    return;
                }
                if(uri.startsWith("/user/signup")){
                    String query = uri.split("\\?")[1];
                    Map<String, String> userInfo = HttpRequestUtils.parseQueryParameter(query);
                    User user = new User(userInfo.get("userId"),userInfo.get("password"), userInfo.get("name"), userInfo.get("email"));
                    userDB.addUser(user);
                    response302Header(dos, "/index.html");
                    return;
//                    byte[] body = Files.readAllBytes(Paths.get("C:/Users/권민혁/Desktop/2-2/KUIT/3week/Backend-Java-Tomcat/webapp/index.html"));
//                    response200Header(dos, body.length);
//                    responseBody(dos, body);
//                    return;
                }
                if(uri.equals("/user/login.html")){
                    byte[] body = Files.readAllBytes(Paths.get("C:/Users/권민혁/Desktop/2-2/KUIT/3week/Backend-Java-Tomcat/webapp/user/login.html"));
                    response200Header(dos, body.length);
                    responseBody(dos, body);
                    return;
                }
                if(uri.equals("/user/login_failed.html")){
                    byte[] body = Files.readAllBytes(Paths.get("C:/Users/권민혁/Desktop/2-2/KUIT/3week/Backend-Java-Tomcat/webapp/user/login_failed.html"));
                    response200Header(dos, body.length);
                    responseBody(dos, body);
                    return;
                }
                if(uri.equals("/user/userList")){
                    if(cookie){
                        byte[] body = Files.readAllBytes(Paths.get("C:/Users/권민혁/Desktop/2-2/KUIT/3week/Backend-Java-Tomcat/webapp/user/list.html"));
                        response200Header(dos, body.length);
                        responseBody(dos, body);
                        return;
                    }
                    response302Header(dos, "/user/login.html");
                }
                if(uri.endsWith(".css")){
                    byte[] body = Files.readAllBytes(Paths.get("C:/Users/권민혁/Desktop/2-2/KUIT/3week/Backend-Java-Tomcat/webapp/index.html"));
                    response200HeaderContent(dos, body.length, "text/css");
                    responseBody(dos, body);
                    return;
                }
            }

            if(method.equals("POST")){
                if(uri.equals("/user/signup")){
                    String requestBody = IOUtils.readData(br, bodyLength);
                    Map<String, String> userInfo = HttpRequestUtils.parseQueryParameter(requestBody);
                    User user = new User(userInfo.get("userId"),userInfo.get("password"), userInfo.get("name"), userInfo.get("email"));
                    userDB.addUser(user);
                    response302Header(dos, "/index.html");
                    return;
//                    byte[] body = Files.readAllBytes(Paths.get("C:/Users/권민혁/Desktop/2-2/KUIT/3week/Backend-Java-Tomcat/webapp/index.html"));
//                    response200Header(dos, body.length);
//                    responseBody(dos, body);
//                    return;
                }
                if(uri.equals("/user/login")){
                    String requestBody = IOUtils.readData(br, bodyLength);
                    Map<String, String> userInfo = HttpRequestUtils.parseQueryParameter(requestBody);
                    User userInDB = userDB.findUserById(userInfo.get("userId"));
                    if(userInDB != null && userInDB.getPassword().equals(userInfo.get("password"))) {
                        response302HeaderLogin(dos, "/index.html");
                        return;
                    }
                    response302Header(dos, "/user/login_failed.html");
                    return;
                }
            }

            String body = IOUtils.readData(br, bodyLength);
            Map<String, String> userInfo = HttpRequestUtils.parseQueryParameter(body);
//            byte[] body = "Hello World".getBytes();
//            response200Header(dos, body.length);
//            responseBody(dos, body);


        } catch (IOException e) {
            log.log(Level.SEVERE,e.getMessage());
        }
    }

    private void requestprinter(BufferedReader br){
        System.out.println("====================\n");
        try{
            String line = "a";
            while (line != null){
                line = br.readLine();
                System.out.println(line);
            }
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos,int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void response200HeaderContent(DataOutputStream dos, int length, String content_type) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + length + "\r\n");
            dos.writeBytes("Content-Type: " + content_type+ "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos, String path) {
        try {
            dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
            // 브라우저에게 redirect을 명시하는 http header 양식
            dos.writeBytes("Location: " + path + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void response302HeaderLogin(DataOutputStream dos, String path){
        try {
            dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
            dos.writeBytes("Location: " + path + "\r\n");
            // 브라우저에게 쿠키를 발행하는 http header 양식
            dos.writeBytes("Set-Cookie: logined=true" + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

}