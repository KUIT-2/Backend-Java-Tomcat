package webserver;

import db.MemoryUserRepository;
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

            String requestLine = br.readLine();
            String requestMethod = requestLine.split(" ")[0];
            String requestPath = requestLine.split(" ")[1];
            System.out.println(requestPath);
            //회원가입 화면
            if (requestPath.equals("/user/form.html")) {
                byte[] body = Files.readAllBytes(Paths.get("webapp\\user\\form.html"));
                response200Header(dos, body.length);
                responseBody(dos, body);
                return;
            }
            //회원가입 POST
            if (requestPath.equals("/user/signup")) {
                int requestContentLength = getRequestContentLength(br);
                String data = IOUtils.readData(br, requestContentLength);
                Map<String, String> requestMap = HttpRequestUtils.parseQueryParameter(data);

                MemoryUserRepository memoryUserRepository = MemoryUserRepository.getInstance();
                User user = new User(requestMap.get("userId"), requestMap.get("password"), requestMap.get("name"), requestMap.get("email"));
                memoryUserRepository.addUser(user);

                response302Header(dos, "/");
                return;
            }
            //로그인 화면 GET
            if (requestPath.equals("/user/login.html")) {
                byte[] body = Files.readAllBytes(Paths.get("webapp\\user\\login.html"));
                response200Header(dos, body.length);
                responseBody(dos, body);
                return;
            }
            //로그인 failed
            if (requestPath.equals("/user/login_failed")) {
                byte[] body = Files.readAllBytes(Paths.get("webapp\\user\\login_failed.html"));
                response200Header(dos, body.length);
                responseBody(dos, body);
                return;
            }
            //로그인 요청 POST
            if (requestPath.equals("/user/login")) {
                int requestContentLength = getRequestContentLength(br);
                String data = IOUtils.readData(br, requestContentLength);
                Map<String, String> requestMap = HttpRequestUtils.parseQueryParameter(data);

                MemoryUserRepository memoryUserRepository = MemoryUserRepository.getInstance();
                User user = memoryUserRepository.findUserById(requestMap.get("userId"));
                if (user != null) {
                    boolean logined = user.getPassword().equals(requestMap.get("password"));
                    if (logined) {
                        response302HeaderLogin(dos, "/", true);
                    } else {
                        response302HeaderLogin(dos, "/user/login_failed", false);
                    }
                } else {
                    response302HeaderLogin(dos, "/user/login_failed", false);
                }
                return;
            }
            //User List 출력
            if (requestPath.equals("/user/userList") || requestPath.equals("/user/list.html")) {
                if (getIsRequestLoginedTrue(br)) {
                    byte[] body = Files.readAllBytes(Paths.get("webapp\\user\\list.html"));
                    response200Header(dos, body.length);
                    responseBody(dos, body);
                    return;
                }
                response302Header(dos, "/user/login.html");
            }
            //그 외에는 메인
            byte[] body = Files.readAllBytes(Paths.get("webapp\\index.html"));
            response200Header(dos, body.length);
            responseBody(dos, body);

        } catch (IOException e) {
            log.log(Level.SEVERE,e.getMessage());
        }
    }

    private int getRequestContentLength(BufferedReader br) throws IOException {
        int contentLength = 0;
        while (true) {
            final String line = br.readLine();
            if (line.equals("")) {
                break;
            }
            // header info
            if (line.startsWith("Content-Length")) {
                contentLength = Integer.parseInt(line.split(": ")[1]);
            }
        }
        return contentLength;
    }

    private boolean getIsRequestLoginedTrue(BufferedReader br) throws IOException {
        boolean isLogined = false;
        while (true) {
            final String line = br.readLine();
            if (line.equals("")) {
                break;
            }
            // header info
            if (line.contains("logined")) {
                System.out.println("eiwjofjweoifjweoif");
                isLogined = Boolean.parseBoolean(line.split("=")[1]);
            }
        }
        return isLogined;
    }

    private String getQuery(String requestPath) {
        int index = requestPath.indexOf('?');
        if (index == -1) {
            return "";
        }
        return requestPath.substring(index + 1);
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

    private void response302Header(DataOutputStream dos, String endpoint) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Location: " + endpoint + "\r\n");
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void response302HeaderLogin(DataOutputStream dos, String endPoint, Boolean logined) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Set-Cookie: logined=" + logined.toString() + "\r\n");
            dos.writeBytes("Location: " + endPoint + "\r\n");
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