package webserver;

import db.MemoryUserRepository;
import db.Repository;
import model.User;
import http.util.IOUtils;
import http.util.HttpRequestUtils;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RequestHandler implements Runnable {
    private static final String ROOT_URL = "./webapp";
    private static final String HOME_URL = "/index.html";
    private static final String LOGIN_FAILED_URL = "/user/login_failed.html";
    private static final String LOGIN_URL = "/user/login.html";
    private static final String LIST_URL = "/user/list.html";

    private final Socket connection;
    private final Logger log = Logger.getLogger(RequestHandler.class.getName());
    private final Repository repository;

    public RequestHandler(Socket connection) {
        this.connection = connection;
        this.repository = MemoryUserRepository.getInstance();
    }

    @Override
    public void run() {
        log.log(Level.INFO, "* New Client Connect *\n Connected IP: " + connection.getInetAddress() + ", Port: " + connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            DataOutputStream dos = new DataOutputStream(out);

            String startLine = br.readLine();
            String[] startLineTokens = startLine.split(" ");
            String method = startLineTokens[0];
            String url = startLineTokens[1];

            int requestContentLength = 0;
            String cookie = "";

            while (true) {
                String line = br.readLine();
                if (line == null || line.equals("")) {
                    break;
                }

                if (line.startsWith("Content-Length")) {
                    requestContentLength = Integer.parseInt(line.split(": ")[1]);
                }

                if (line.startsWith("Cookie")) {
                    cookie = line.split(": ")[1];
                }
            }

            byte[] body = new byte[0];

            // Handle various requests
            if (method.equals("GET") && url.endsWith(".html")) {
                body = Files.readAllBytes(Paths.get(ROOT_URL + url));
            } else if (url.equals("/")) {
                body = Files.readAllBytes(Paths.get(ROOT_URL + HOME_URL));
            } else if (url.equals("/user/signup")) {
                handleSignup(br, dos, requestContentLength);
                return;
            } else if (url.equals("/user/login")) {
                handleLogin(br, dos, requestContentLength);
                return;
            } else if (url.equals("/user/userList")) {
                handleUserList(dos, cookie);
                return;
            } else if (method.equals("GET") && url.endsWith(".css")) {
                handleCssRequest(dos, url);
                return;
            } else {
                handleOtherRequests(url, dos);
                return;
            }

            response200Header(dos, body.length, "text/html");
            responseBody(dos, body);
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        } finally {
            try {
                connection.close();
            } catch (IOException e) {
                log.log(Level.SEVERE, e.getMessage());
            }
        }
    }

    private void handleSignup(BufferedReader br, DataOutputStream dos, int contentLength) throws IOException {
        String requestBody = IOUtils.readBody(br, contentLength);
        Map<String, String> formData = HttpRequestUtils.parseQueryParameter(requestBody);

        String userId = formData.get("userId");
        String password = formData.get("password");
        String name = formData.get("name");
        String email = formData.get("email");

        User newUser = new User(userId, password, name, email);
        repository.addUser(newUser);

        response302Header(dos, HOME_URL);
    }

    private void handleLogin(BufferedReader br, DataOutputStream dos, int contentLength) throws IOException {
        String requestBody = IOUtils.readBody(br, contentLength);
        Map<String, String> formData = HttpRequestUtils.parseQueryParameter(requestBody);

        String userId = formData.get("userId");
        String password = formData.get("password");

        User user = repository.findUserById(userId);

        if (user != null && user.getPassword().equals(password)) {
            response302HeaderWithCookie(dos, HOME_URL);
        } else {
            response302Header(dos, LOGIN_FAILED_URL);
        }
    }

    private void handleUserList(DataOutputStream dos, String cookie) throws IOException {
        if (cookie != null && cookie.equals("logined=true")) {
            byte[] body = Files.readAllBytes(Paths.get(ROOT_URL + LIST_URL));
            response200Header(dos, body.length, "text/html");
            responseBody(dos, body);
        } else {
            response302Header(dos, LOGIN_URL);
        }
    }

    private void handleCssRequest(DataOutputStream dos, String url) throws IOException {
        String filePath = ROOT_URL + url;
        byte[] fileData = Files.readAllBytes(Paths.get(filePath));
        response200Header(dos, fileData.length, "text/css");
        responseBody(dos, fileData);
    }

    private void handleOtherRequests(String url, DataOutputStream dos) throws IOException {
        String filePath = ROOT_URL + url;

        if (Files.exists(Paths.get(filePath))) {
            byte[] fileData = Files.readAllBytes(Paths.get(filePath));
            String contentType = Files.probeContentType(Paths.get(filePath));
            response200Header(dos, fileData.length, contentType + "; charset=UTF-8");
            responseBody(dos, fileData);
        } else {
            String notFoundMessage = "404 Not Found: " + url;
            byte[] notFoundBody = notFoundMessage.getBytes();
            response404Header(dos, notFoundBody.length);
            responseBody(dos, notFoundBody);
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent, String contentType) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK\r\n");
            dos.writeBytes("Content-Type: " + contentType + "\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
            dos.flush();
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos, String path) {
        try {
            dos.writeBytes("HTTP/1.1 302 Redirect\r\n");
            dos.writeBytes("Location: " + path + "\r\n");
            dos.writeBytes("\r\n");
            dos.flush();
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void response302HeaderWithCookie(DataOutputStream dos, String path) {
        try {
            dos.writeBytes("HTTP/1.1 302 Redirect\r\n");
            dos.writeBytes("Location: " + path + "\r\n");
            dos.writeBytes("Set-Cookie: logined=true\r\n");
            dos.writeBytes("\r\n");
            dos.flush();
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void response404Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 404 Not Found\r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
            dos.flush();
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
