package webserver;

import db.MemoryUserRepository;
import db.Repository;
import http.util.HttpRequestUtils;
import http.util.IOUtils;
import model.User;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static http.util.HttpRequestUtils.parseQueryParameter;

public class RequestHandler implements Runnable{
    Socket connection;
    private static final Logger log = Logger.getLogger(RequestHandler.class.getName());
    private static final String ROOT_URL = "./webapp";

    private final Repository repository;

    public RequestHandler(Socket connection) {
        this.connection = connection;
        repository = MemoryUserRepository.getInstance();
    }

    @Override
    public void run() {
        log.log(Level.INFO, "New Client Connect! Connected IP : " + connection.getInetAddress() + ", Port : " + connection.getPort());
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()){
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            DataOutputStream dos = new DataOutputStream(out);

            byte[] body = "Hello World".getBytes();

            String startLine = br.readLine();
            log.log(Level.INFO, startLine);
            String startLines[] = startLine.split(" ");
            String httpMethod = startLines[0]; // GET, POST, PUT, DELETE 등
            String requestTarget = startLines[1]; //HTTP Request가 전송되는 목표 주소

            int requestContentLength = 0;

            //header
            while (true){
                final String line = br.readLine();
                if(line.equals("")){ //blank line 이후부터는 body값이므로
                    break;
                }
                if (line.startsWith("Content-Length")) {
                    requestContentLength = Integer.parseInt(line.split(": ")[1]);
                    //parseInt : 문자열을 정수로 변환
                }
            }

            log.info(requestTarget.toString());
            //요구사항 1번
            if (httpMethod.equals("GET")&& requestTarget.endsWith("/")){
                body = Files.readAllBytes(Paths.get(ROOT_URL + "/index.html"));
            }

            //요구사항 1번, 2번, 5번
            if (httpMethod.equals("GET")&& requestTarget.endsWith(".html")){
                body = Files.readAllBytes(Paths.get(ROOT_URL + requestTarget));
            }

            //요구사항 2번 - GET
            if (httpMethod.equals("GET")&& requestTarget.contains("/user/signup")){
                String queryString = requestTarget.split("\\?")[1];
                log.log(Level.INFO, queryString);
                Map<String, String> queryParameter = parseQueryParameter(queryString);
                User user = new User(queryParameter.get("userId"), queryParameter.get("password"), queryParameter.get("name"), queryParameter.get("email"));
                repository.addUser(user);
                response302Header(dos,"/index.html");
                return;
            }

            //요구사항 3번 - POST
            if (httpMethod.equals("POST")&& requestTarget.endsWith("/user/signup")){
                String queryString = IOUtils.readData(br, requestContentLength);
                log.log(Level.INFO, queryString);
                Map<String, String> queryParameter = parseQueryParameter(queryString);
                User user = new User(queryParameter.get("userId"), queryParameter.get("password"), queryParameter.get("name"), queryParameter.get("email"));
                repository.addUser(user);
                response302Header(dos,"/index.html");
                return;
            }

            //요구사항 5번 - 로그인하기
            if (httpMethod.equals("POST")&& requestTarget.endsWith("/user/login")) {
                String requestBody = IOUtils.readData(br, requestContentLength);
                log.log(Level.INFO, requestBody);
                Map<String, String> queryParam= parseQueryParameter(requestBody);

                User repositoryUser = repository.findUserById(queryParam.get("userId"));

                login(dos, queryParam, repositoryUser);
            }


            response200Header(dos, body.length);
            responseBody(dos, body);

        } catch (IOException e) {
            log.log(Level.SEVERE,e.getMessage());
        }
    }

    private void login(DataOutputStream dos, Map<String, String> queryParam, User repositoryUser) {
        if (repositoryUser!= null && repositoryUser.getPassword().equals(queryParam.get("password"))){
            //헤더에 Cookie: logined=true를 추가
            //index.html 화면으로 redirect
            response302HeaderWithCookie(dos,"/index.html");
        }else{
            response302Header(dos,"/user/login_failed.html");
            //response302Header(dos,ROOT_URL+"/user/login_failed.html");
        }
        return;
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

}