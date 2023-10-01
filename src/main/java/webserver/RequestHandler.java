package webserver;

import db.MemoryUserRepository;
import db.Repository;
import http.util.IOUtils;
import model.User;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static http.util.HttpRequestUtils.parseQueryParameter;

public class RequestHandler implements Runnable{
    Socket connection;
    private static final Logger log = Logger.getLogger(RequestHandler.class.getName());

    public RequestHandler(Socket connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        log.log(Level.INFO, "New Client Connect! Connected IP : " + connection.getInetAddress() + ", Port : " + connection.getPort());
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()){
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            DataOutputStream dos = new DataOutputStream(out);

            HttpRequest httpRequest = HttpRequest.from(br);
            HttpResponse httpResponse = HttpResponse.from(dos);

            RequestMapping requestMapping = new RequestMapping(httpRequest, httpResponse);
            requestMapping.proceed();

            /*String requestLine = br.readLine();
            String[] requestParts = requestLine.split(" ");
            String method = requestParts[0]; //명령어
            String url = requestParts[1]; // url

            int contentLength = 0;
            String cookie = "";

            while(true){
                final String line = br.readLine();
                //아무것도 없으면 벗어남
                if(line.equals(""))
                    break;

                //context-length
                if(line.startsWith("Content-Length"))
                    contentLength = Integer.parseInt(line.split(": ")[1]);

                //cookie
                if(line.startsWith("Cookie"))
                    cookie = line.split(": ")[1];
            }

            byte[] body = new byte[0];

            //1번
            if ("GET".equals(method) && url.endsWith(".html")) {
                Path htmlPath = Paths.get(ROOT_URL+url);
                body = Files.readAllBytes(htmlPath);

            }

            if (url.equals("/")) {
                body = Files.readAllBytes(homePath);
            }

            //회원가입
            if("/user/signup".equals(url)){
                String queryString = IOUtils.readData(br,contentLength);
                System.out.println("HTTP Request Body for /user/sign:");
                System.out.println(queryString);
                Map<String, String> queryPras = parseQueryParameter(queryString);

                repository.addUser(new User(queryPras.get("userId"),
                        queryPras.get("password"),
                        queryPras.get("name"),
                        queryPras.get("email")));
                response302Header(dos,"/index.html");
                return;
            }

            //로그인 페이지
            if("/user/login".equals(url)){
                String queryString = IOUtils.readData(br,contentLength);

                Map<String, String> queryPras = parseQueryParameter(queryString);
                User user = repository.findUserById(queryPras.get("userId"));
                doLogin(user, dos, queryPras);
                return;
            }

            if ("/user/userList".equals(url)) {
                if (!cookie.contains("logined=true")) {
                    response302Header(dos,LOGIN_URL);
                    return;
                }
                body = Files.readAllBytes(Paths.get(ROOT_URL + LIST_URL));
            }

            if("GET".equals(method) && url.endsWith(".css")){
                response200HeaderCss(dos,body.length);
                responseBody(dos,body);
                return;
            }

            response200Header(dos, body.length);
            responseBody(dos, body);*/

        } catch (IOException e) {
            log.log(Level.SEVERE,e.getMessage());
        }
    }

    /*private void doLogin(User user, DataOutputStream dos, Map<String, String> queryPras){
        if(user != null && user.getPassword().equals(queryPras.get("password"))){
            response302HeaderWithCookie(dos,HOME_URL);
            return;
        }
        response302Header(dos, LOGIN_FAILED_URL);
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
    private void response200HeaderCss(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/css\r\n");
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
    }*/

}