package webserver;

import db.MemoryUserRepository;
import db.Repository;
import http.util.IOUtils;
import model.User;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RequestHandler implements Runnable{
    Socket connection;
    private static final Logger log = Logger.getLogger(RequestHandler.class.getName());
    private static final String ROOT_URL = "./webapp";
    private static final String HOME_URL = "/index.html";
    private static final String SIGNUP_URL = "/user/form.html";
    private static final String LOGIN_FAILED_URL = "/user/login_failed.html";
    private static final String LOGIN_URL = "/user/login.html";
    private static final String LIST_URL = "/user/list.html";

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

            String startLine = br.readLine();
            String[] startLines = startLine.split(" ") ;
            String httpMethod = startLines[0];
            String requestUrl = startLines[1];

            byte[] body = new byte[0];
            int requestContentLength = 0;

            while (true) {
                final String line = br.readLine();
                if (line.equals("")) {
                    break;
                }
                // header info
                if (line.startsWith("Content-Length")) {
                    requestContentLength = Integer.parseInt(line.split(": ")[1]);
                }
            }

            if (httpMethod.equals("GET") && requestUrl.endsWith(".html")) {
                body = Files.readAllBytes(Paths.get(ROOT_URL + requestUrl));
            }

            /**
             * 요구사항 1번: index.html 반환하기
             */
            if (httpMethod.equals("GET") && (requestUrl.equals("/"))) {
                body = Files.readAllBytes(Paths.get(ROOT_URL+HOME_URL));
            }

            /**
             * 요구사헝 2,3,4번 GET, POST 방식으로 회원가입하기
             */
            String[] urlQueryString = requestUrl.split("\\?");

            //2번: GET 방식
            if (httpMethod.equals("GET") && urlQueryString[0].equals("/user/signup")) {
                Map<String, String> queryParameter = getQueryParameter(urlQueryString[1]);
                signupUser(dos, queryParameter);
            }

            //3번: POST 방식
            if (httpMethod.equals("POST") && requestUrl.equals("/user/signup")) {
                String queryString = IOUtils.readData(br, requestContentLength);
                Map<String, String> queryParameter = getQueryParameter(queryString);
                signupUser(dos, queryParameter);
            }

            /**
             * 요구사항 5번: 로그인하기
             */
            if (requestUrl.equals("/user/login")) {
                String queryString = IOUtils.readData(br, requestContentLength);
                Map<String, String> queryParameter = getQueryParameter(queryString);
                User user = repository.findUserById(queryParameter.get("userId"));
                login(dos, queryParameter, user);
                return;
            }

            response200Header(dos, body.length);
            responseBody(dos, body);

        } catch (IOException e) {
            log.log(Level.SEVERE,e.getMessage());
        }

    }

    private void login(DataOutputStream dos, Map<String, String> queryParameter, User user) {
        if (user!= null && user.getPassword().equals(queryParameter.get("password"))) {
            response302HeaderWithCookie(dos,HOME_URL);
            return;
        }
        response302Header(dos,LOGIN_FAILED_URL);
    }

    private void signupUser(DataOutputStream dos, Map<String, String> queryParameter) {
        User user = new User(queryParameter.get("userId"), queryParameter.get("password"), queryParameter.get("name"), queryParameter.get("email"));
        repository.addUser(user);
        response302Header(dos, HOME_URL);
        return;
    }

    private void response302Header(DataOutputStream dos, String path) {
        try {
            dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
            dos.writeBytes("Location: " + path + "\r\n");
            dos.writeBytes("\r\n");
            dos.flush();
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
            dos.flush();
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private Map<String, String> getQueryParameter(String queryString) {
        Map<String, String> queryParameter = new HashMap<>();
        String[] queryParams = queryString.split("&");
        for (String queryParam : queryParams) {
            String[] querySplit = queryParam.split("=");
            queryParameter.put(querySplit[0],querySplit[1]);
        }
        return queryParameter;
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