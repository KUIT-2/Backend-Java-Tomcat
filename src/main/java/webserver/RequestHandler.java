package webserver;

import db.MemoryUserRepository;
import http.util.IOUtils;
import model.User;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RequestHandler implements Runnable{
    Socket connection;
    private static final Logger log = Logger.getLogger(RequestHandler.class.getName());
    private String BASE_PATH = "./webapp";
    private String INDEX_PATH = "/index.html";
    private String FORM_PATH = "/user/form.html";

    private String LOGIN_PATH = "/user/login.html";
    private String LOGIN_FAILED = "/user/login_failed.html";

    private String LIST_PATH = "/user/list.html";

    public RequestHandler(Socket connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        log.log(Level.INFO, "New Client Connect! Connected IP : " + connection.getInetAddress() + ", Port : " + connection.getPort());
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()){
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            DataOutputStream dos = new DataOutputStream(out);

            String startline = br.readLine();
            System.out.println(startline);
            String lines[] = startline.split(" ");
            String method = lines[0];
            String url = lines[1];



            int requestContentLength = 0;
            String Cookie = "";
            while (true) {
                final String line = br.readLine();
                if (line.equals("")) {
                    break;
                }
                // header info
                if (line.startsWith("Content-Length")) {
                    requestContentLength = Integer.parseInt(line.split(": ")[1]);
                }

                if(line.startsWith("Cookie")){
                    Cookie = line.split(": ")[1];
                }
                System.out.println(line);
            }
            System.out.println("url : " + url);

            if(url.equals("/")){
                url = BASE_PATH + INDEX_PATH;
            }

            if(url.equals("/user/form.html")){
                url = BASE_PATH + FORM_PATH;
            }

            if(url.equals("/user/signup")){
                String user_info = IOUtils.readData(br, requestContentLength);
                System.out.println(user_info);
                String user_info_sub[] = user_info.split("&");
                String userId = user_info_sub[0].split("=")[1];
                String password = user_info_sub[1].split("=")[1];
                String name = user_info_sub[2].split("=")[1];
                String email = user_info_sub[3].split("=")[1];

//                System.out.println("userId : " + userId);
//                System.out.println("password : " + password);
//                System.out.println("name : " + name);
//                System.out.println("email : " + email);

                User user = new User(userId, password, name, email);
                MemoryUserRepository.getInstance().addUser(user);
                response302Header(dos,"../");
            }

            if(url.equals("/user/login.html")){
                url = BASE_PATH + LOGIN_PATH;
            }

            if(url.equals("/user/login")){
                String login_info = IOUtils.readData(br, requestContentLength);
                String login_info_sub[] = login_info.split("&");
                String login_id = login_info_sub[0].split("=")[1];
                String login_password = login_info_sub[1].split("=")[1];
                User user = MemoryUserRepository.getInstance().findUserById(login_id);
                System.out.println(user);
                if(user != null){
                    if(user.getPassword().equals(login_password)){
                        response302HeaderWithCookie(dos, "../");
                    }
                    else{
                        url = BASE_PATH + LOGIN_FAILED;
                    }
                }
                else{
                    url = BASE_PATH + LOGIN_FAILED;
                }
                System.out.println("login_info : " + login_info);
            }

            if(url.equals("/user/userList")){
//                String user_info = IOUtils.readData(br, requestContentLength);
//                System.out.println(user_info);
                if(Cookie.equals("logined=true")){
                    url = BASE_PATH + LIST_PATH;
                }
                else{
                    response302Header(dos, "../");
                }
            }

            byte[] body = new byte[0];
            body = Files.readAllBytes(Paths.get(url));

            response200Header(dos, body.length);
            responseBody(dos, body);

        } catch (IOException e) {
            log.log(Level.SEVERE,e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
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

    private void response302Header(DataOutputStream dos, String path) {
        try {
            dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
            dos.writeBytes("Location: " + path + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void response302HeaderWithCookie(DataOutputStream dos, String path) {
        try {
            dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
            dos.writeBytes("Location: " + path + "\r\n");
            dos.writeBytes("Set-Cookie: logined=true" + "\r\n");

            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

}