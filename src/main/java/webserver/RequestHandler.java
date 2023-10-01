package webserver;

import db.MemoryUserRepository;
import db.Repository;
import http.util.IOUtils;
import model.User;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static http.util.HttpRequestUtils.getQueryParameter;
import static enums.URL.*;


public class RequestHandler implements Runnable{
    Socket connection;
    private static final Logger log = Logger.getLogger(RequestHandler.class.getName());

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
            /*
            / InputStream에는 클라이언트의 요청 message가 들어있음
            / -> request의 첫줄 : header -> get /index.html /HTTP 1.1 이면 webapp폴더의 index.html 을 return (요구사항 1번)
            */

            byte[] body = new byte[0];

            String startLine = br.readLine();       // startLine : request의 startLine
            String[] startLines = startLine.split(" ");      // startLines : startLine의 구성요소
            String method = startLines[0];
            String url = startLines[1];

            int requestContentLength = 0;
            String cookie = "";

            while (true){
                final String line = br.readLine();      // line : HTTP request message 헤더의 한 행
                if(line.equals("")){
                    break;
                }
                if(line.startsWith("Content-Length")){
                    requestContentLength = Integer.parseInt(line.split(": ")[1]);
                }       // request header의 Content-Length : 메시지의 body 크기를 byte단위로 표시
                if(line.startsWith("Cookie")){
                    cookie = line.split(": ")[1];
                }       // request header의 Cookie : 쿠키 정보

            }

            // index.html
            if(method.equals("GET") && url.endsWith(".html")){
                body = Files.readAllBytes(Paths.get(HOME_URL.getUrl() + url));
            }

            // 루트경로이면 다시 index.html로 복귀
            if(url.equals("/")){
                body = Files.readAllBytes(Paths.get(ROOT_URL.getUrl() + HOME_URL.getUrl()));
            }

            // 회원 가입
            /*
            / 1번에서 구현한 index.html에서 signup 버튼을 클릭하면 url이 user/form.html로 변경되야 함
            / form.html에서 정보입력후 회원가입 완료 후 다시 url을 index.html로 변경
            / 요청 url의 쿼리스트링을 통해 회원가입정보를 전달
            / -> 쿼리스트링을 파싱하여 새로운 user instance 생성
            */
            if(url.equals("/user/signup")){
                String queryString = IOUtils.readData(br, requestContentLength);
                Map<String, String> queryParameter = getQueryParameter(queryString);
                User user = new User(queryParameter.get("userId"), queryParameter.get("password"), queryParameter.get("name"), queryParameter.get("email"));
                // 파싱된 매개변수를 사용하여 User 객체 생성 -> User의 회원가입정보 나타냄

                repository.addUser(user);       // db에 User 추가
                response302Header(dos, HOME_URL.getUrl());       // HTTP 302 Redirect 응답 생성 & HOME_URL로 복귀
                return;
            }

            // 로그인
            if(url.equals("/user/login")){
                String queryString = IOUtils.readData(br, requestContentLength);
                Map<String, String> queryParameter = getQueryParameter(queryString);
                User user = repository.findUserById(queryParameter.get("userId"));
                login(dos, queryParameter, user);
                return;
            }

            // 사용자 목록
            if(url.equals("/user/userList")){
                if(!cookie.equals("logined=true")){
                    response302Header(dos, LOGIN_URL.getUrl());          // user가 로그인하지 않은 상태이면 로그인페이지로 redirect
                    return;
                }
                body = Files.readAllBytes(Paths.get(ROOT_URL.getUrl() + LIST_URL.getUrl()));      // user가 로그인 상태이면 사용자 목룍페이지의 내용을 body에 저장
            }

            // css
            if(method.equals("GET") && url.endsWith(".css")){
                body = Files.readAllBytes(Paths.get(ROOT_URL.getUrl() + url));
                response200HeaderWithContentType(dos, body.length, "text/css");
                responseBody(dos, body);
                return;
            }

            responseBody(dos, body);
            response200Header(dos, body.length);

        } catch (IOException e) {
            log.log(Level.SEVERE,e.getMessage());
        }
    }

    private void login(DataOutputStream dos, Map<String, String> queryParameter, User user){
        if(user != null && user.getPassword().equals(queryParameter.get("passwd"))){
            response302HeaderWithCookie(dos, HOME_URL.getUrl());
            return;
        }
        response302Header(dos, LOGIN_FAILED_URL.getUrl());
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

    private void response200HeaderWithContentType(DataOutputStream dos, int lengthOfBodyContent, String contentType) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: " + contentType + ";charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos, String path){
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
            dos.writeBytes("Set-Cookie: logined=true" + "\r\n");        // user 로그인 OK

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