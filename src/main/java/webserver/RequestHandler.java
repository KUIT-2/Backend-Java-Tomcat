package webserver;

import db.MemoryUserRepository;
import http.util.IOUtils;
import model.User;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RequestHandler implements Runnable{
    Socket connection;
    private static final Logger log = Logger.getLogger(RequestHandler.class.getName());
    private final String ROOT_DIR = "./webapp";
    private final String HOME_URL = "/index.html";

    public RequestHandler(Socket connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        log.log(Level.INFO, "New Client Connect! Connected IP : " + connection.getInetAddress() + ", Port : " + connection.getPort());
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()){
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            DataOutputStream dos = new DataOutputStream(out);
            // http request 분석 및 출력
            String line;
            StringBuilder httpRequestHeader = new StringBuilder();
            int requestContentLength = 0;
            // startLine 분석
            String[] startLines = br.readLine().split(" ");
            String method = startLines[0];
            String url = startLines[1];
            if (url.equals("/")){
                url = HOME_URL;
            }
            log.log(Level.INFO, url);
            // header 분석
            while((line = br.readLine()) != null){
                if (line.equals("")){   // black line, header 끝
                    break;
                }
                if (line.startsWith("Content-Length")){
                    requestContentLength = Integer.parseInt(line.split(": ")[1]);
                }
                httpRequestHeader.append(line).append("\n");
            }
            log.log(Level.INFO, httpRequestHeader.toString());

            // body 분석
            if (requestContentLength > 0){
                String requestBody = IOUtils.readBody(br, requestContentLength);
                log.log(Level.SEVERE, requestBody);
            }

            byte[] body = new byte[0];
            // 요구사항 1. 파일 반환
            if (method.equals("GET") && url.endsWith(".html")){
                body = Files.readAllBytes(Paths.get(ROOT_DIR, url));
            }
            // 요구사항 2. GET 회원가입
            if (method.equals("GET") && url.startsWith("/user/signup?")){
                MemoryUserRepository userDB = MemoryUserRepository.getInstance();
                String queryString = url.split("\\?")[1];
                User newUser = User.fromQueryString(queryString);
                userDB.addUser(newUser);
                response302Header(dos, HOME_URL);
                return;
            }

            // 요구사항 3. POST 회원가입
            if (method.equals("POST") && url.equals("/user/signup")){
                MemoryUserRepository userDB = MemoryUserRepository.getInstance();
                if (requestContentLength > 0){
                    String requestBody = IOUtils.readBody(br, requestContentLength);
                    User newUser = User.fromQueryString(requestBody);
                    userDB.addUser(newUser);
                    response302Header(dos, HOME_URL);
                    return;
                }
                // TODO: Exception?
            }
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

    private void response302Header(DataOutputStream dos, String url) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Location: " + url + "\r\n");
            dos.writeBytes("\r\n");
            dos.flush();
            dos.close();
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