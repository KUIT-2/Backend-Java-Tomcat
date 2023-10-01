package webserver;

import db.MemoryUserRepository;
import http.util.HttpRequestUtils;
import http.util.IOUtils;
import model.User;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RequestHandler implements Runnable {
    Socket connection;
    private static final Logger log = Logger.getLogger(RequestHandler.class.getName());

    public RequestHandler(Socket connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        log.log(Level.INFO, "New Client Connect! Connected IP : " + connection.getInetAddress().getHostAddress() + ", Port : " + connection.getPort());
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            DataOutputStream dos = new DataOutputStream(out);

            String startLine = br.readLine();
            String requestPath = parseRequestPath(startLine);
            int contentLength = parseContentLength(br);
            String requestBody = IOUtils.readBody(br, contentLength);

            if (requestPath.endsWith(".css")) {
                handleCssRequest(dos, requestPath);
            } else if (requestPath.equals("/user/signup")) {
                if (startLine.contains("GET")) {
                    response302Header(dos, "/user/form.html");
                } else if (startLine.contains("POST")) {
                    handleFormSubmission(requestBody, dos);
                }
            } else if (requestPath.equals("/user/login")) {
                if (startLine.contains("GET")) {
                    serverFile("webapp/user/login.html", dos);
                } else if (startLine.contains("POST")) {
                    handleLogin(requestBody, dos);
                }
            } else if (requestPath.equals("/user/list")) {
                // Check for the logined cookie
                if (startLine.contains("GET") && startLine.contains("Cookie: logined=true")) {
                    handleUserListRequest(dos);
                } else {
                    response302Header(dos, "/user/login.html");
                }
            } else if (requestPath.equals("/") || requestPath.equals("/index.html")) {
                serverFile("webapp/index.html", dos);
            } else if (requestPath.equals("/form.html")) {
                serverFile("webapp/user/form.html", dos);
            } else {
                serverFile(requestPath, dos);
            }

        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }
    private String parseRequestPath(String startLine) {
        String[] tokens = startLine.split(" ");
        return tokens.length > 1 ? tokens[1] : "/";
    }

    private int parseContentLength(BufferedReader br) throws IOException {
        int contentLength = 0;
        String line;
        while ((line = br.readLine()) != null && !line.isEmpty()) {
            if (line.startsWith("Content-Length:")) {
                contentLength = Integer.parseInt(line.split(":")[1].trim());
            }
        }
        return contentLength;
    }

    private void serverFile(String filePath, DataOutputStream dos) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                byte[] fileData = Files.readAllBytes(file.toPath());
                response200Header(dos, fileData.length);  // 수정: Content-Length 설정
                responseBody(dos, fileData);
            } else {
                String notFoundMessage = "404 Not Found: " + filePath;
                byte[] notFoundBody = notFoundMessage.getBytes();
                response404Header(dos, notFoundBody.length);
                responseBody(dos, notFoundBody);
            }
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void handleFormSubmission(String requestBody, DataOutputStream dos) throws IOException {
        Map<String, String> formDataMap = HttpRequestUtils.parseQueryParameter(requestBody);

        MemoryUserRepository userRepository = MemoryUserRepository.getInstance();
        String userId = formDataMap.get("userId");
        String password = formDataMap.get("password");
        String name = formDataMap.get("name");
        String email = formDataMap.get("email");

        User newUser = new User(userId, password, name, email);
        userRepository.addUser(newUser);

        String redirectUrl = "/user/login.html";
        response302HeaderWithCookie(dos, redirectUrl);
    }

    private void handleCssRequest(DataOutputStream dos, String requestPath) {
        try {
            String cssFilePath = "webapp" + requestPath;
            File file = new File(cssFilePath);

            if (file.exists()) {
                byte[] fileData = Files.readAllBytes(file.toPath());
                response200HeaderWithContentType(dos, fileData.length, "text/css");
                responseBody(dos, fileData);
            } else {
                String notFoundMessage = "404 Not Found: " + requestPath;
                byte[] notFoundBody = notFoundMessage.getBytes();
                response404Header(dos, notFoundBody.length);
                responseBody(dos, notFoundBody);
            }
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }


    private void handleUserListRequest(DataOutputStream dos) {
        try {
            String userListPagePath = "webapp/user/list.html";
            File file = new File(userListPagePath);
            if (file.exists()) {
                byte[] fileData = Files.readAllBytes(file.toPath());
                response200HeaderWithContentType(dos, fileData.length, "text/html");
                responseBody(dos, fileData);
            } else {
                String notFoundMessage = "404 Not Found: " + userListPagePath;
                byte[] notFoundBody = notFoundMessage.getBytes();
                response404Header(dos, notFoundBody.length);
                responseBody(dos, notFoundBody);
            }
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private String readRequestBody(BufferedReader br, int contentLength) throws IOException {
        StringBuilder requestBody = new StringBuilder();

        for (int i = 0; i < contentLength; i++) {
            requestBody.append((char) br.read());
        }

        return requestBody.toString();
    }


    private void handleLogin(String requestBody, DataOutputStream dos) {
        Map<String, String> formDataMap = HttpRequestUtils.parseQueryParameter(requestBody);

        MemoryUserRepository userRepository = MemoryUserRepository.getInstance();
        String userId = formDataMap.get("userId");
        String password = formDataMap.get("password");

        if (userRepository.isValidUser(userId, password)) {
            response302Header(dos, "/index.html");
        } else {
            response302Header(dos, "/user/login_failed.html");
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

    private void response404Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 404 Not Found\r\n");
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
