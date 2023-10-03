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

            /**
             * 요구사항 1번: index.html 반환하기
             */
            if (httpMethod.equals("GET") && (requestUrl.equals("/") || requestUrl.equals("/index.html"))) {
                body = Files.readAllBytes(Paths.get(ROOT_URL+HOME_URL));
                response200Header(dos, body.length);
                responseBody(dos, body);
            }


            /**
             * 요구사헝 2,3,4번 GET, POST 방식으로 회원가입하기
             */
            //form.html 반환
            if (httpMethod.equals("GET") && (requestUrl.equals("/user/form.html"))) {
                body = Files.readAllBytes(Paths.get(ROOT_URL+SIGNUP_URL));
            }

            String[] urlQueryString = requestUrl.split("\\?");
            Map<String, String> queryParameter = new HashMap<>();

            //2번: GET 방식
            if (httpMethod.equals("GET") && urlQueryString[0].equals("/user/signup")) {
                String[] queryParams = urlQueryString[1].split("&");
                signupUser(dos, body, queryParameter, queryParams);
            }

            //3번: POST 방식
            if (httpMethod.equals("POST") && requestUrl.equals("/user/signup")) {
                String postQueryString = IOUtils.readData(br, requestContentLength);
                String[] queryParams = postQueryString.split("&");
                signupUser(dos, body, queryParameter, queryParams);
            }


            response200Header(dos, body.length);
            responseBody(dos, body);

        } catch (IOException e) {
            log.log(Level.SEVERE,e.getMessage());
        }

    }

    private void signupUser(DataOutputStream dos, byte[] body, Map<String, String> queryParameter, String[] queryParams) {
        getQueryParameter(queryParams, queryParameter);
        User user = new User(queryParameter.get("userId"), queryParameter.get("password"), queryParameter.get("name"), queryParameter.get("email"));
        repository.addUser(user);
        response302Header(dos, HOME_URL);
        responseBody(dos,body);
        return;
    }

    private void getQueryParameter(String[] queryParams, Map<String, String> queryParameter) {
        for (String queryParam : queryParams) {
            log.info(queryParam);
            String[] querySplit = queryParam.split("=");
            queryParameter.put(querySplit[0],querySplit[1]);
        }
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

    private Map<String, String> getQueryParameter(String queryString) {
        return null;
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