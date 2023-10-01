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

public class RequestHandler implements Runnable {
    Socket connection;
    private static final Logger log = Logger.getLogger(RequestHandler.class.getName());
    private static final String WEB_PORT = "webapp";

    public RequestHandler(Socket connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        log.log(Level.INFO, "New Client Connect! Connected IP : " + connection.getInetAddress() + ", Port : " + connection.getPort());
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            DataOutputStream dos = new DataOutputStream(out);

            byte[] body = new byte[0];

            String requestLine = br.readLine();
            log.log(Level.INFO, "requestLine | " + requestLine);
            String[] request = requestLine.split(" ");
            String httpMethod = request[0];
            String requestURL = request[1];
            String httpVersion = request[2];

            int requestContentLength = 0;
            boolean cookie = false;


            while (true) {
                final String line = br.readLine();
                if (line == null || line.equals("")) {
                    break;
                }
                // header info

                // 요구사항 6
                log.log(Level.INFO, "Request Header | " + line.startsWith("Cookie: "));
                cookie = line.startsWith("Cookie: ");
                log.log(Level.INFO, "cookie | " + cookie);

                if (line.startsWith("Content-Length")) {
                    requestContentLength = Integer.parseInt(line.split(": ")[1]);

                }
            }

//            log.log(Level.INFO, "httpMethod: " + request[0] + "requestURL: " + request[1] + "httpVersion: " + request[2]);

            if (httpMethod.equals("GET")) {
                //요구사항 1
                if (requestURL.equals("/") || requestURL.equals("/index.html")) {
                    sendFile(dos, WEB_PORT + "/index.html");
                }
                // 요구사항 2
                else if (requestURL.equals("/user/form.html")) {
                    sendFile(dos, WEB_PORT + "/user/form.html");
                }
                // 요구사항 7
                else if (requestURL.equals("/css/styles.css")) {
                    body = Files.readAllBytes(Paths.get(WEB_PORT + requestURL));
                    responseCSS(dos, body.length, "text/css");
                    responseBody(dos, body);
                    return;
                }
                // 요구사항 3
                else if (requestURL.equals("/user/login.html")) {
                    sendFile(dos, WEB_PORT + "/user/login.html");
                }
                // 요구사항 5
                else if (requestURL.equals("/user/login_failed.html")) {
                    sendFile(dos, WEB_PORT + "/user/login_failed.html");
                }
                // 요구사항 6
                else if (requestURL.equals("/user/userList")) {

                    if (cookie) {
                        sendFile(dos, WEB_PORT + "/user/list.html");
                    } else {
                        sendRedirect(dos, "/user/login.html");
                    }
                }
                //404
                else {
                    send404NotFound(dos);
                }
            }
            // 요구사항 3
            else if (httpMethod.equals("POST")) {
                String requestBody = IOUtils.readData(br, requestContentLength);
                log.log(Level.INFO, "POST_BODY: " + requestBody);

                //요구사항 2
                if (requestURL.equals("/user/signup")) {
//                    String queryString = IOUtils.readData(br, requestContentLength);
//                    log.log(Level.INFO, queryString);

//                    String[] querySplit = queryString.split("&");
                    String[] querySplit = requestBody.split("&");
                    String userId = querySplit[0].split("=")[1];
                    String password = querySplit[1].split("=")[1];
                    String name = querySplit[2].split("=")[1];
                    String email = querySplit[3].split("=")[1];
                    log.log(Level.INFO, "SIGNUP | userId = " + userId + " pw = " + password + " name = " + name + " email = " + email);

                    User user = new User(userId, password, name, email);
                    MemoryUserRepository memoryUserRepository = MemoryUserRepository.getInstance();
                    memoryUserRepository.addUser(user);

                    // 요구사항 4
                    sendRedirect(dos, "/");
                    return;
                }
                // 요구사항 5
                if (requestURL.equals("/user/login")) {
                    String[] querySplit = requestBody.split("&");
                    String userId = querySplit[0].split("=")[1];
                    String password = querySplit[1].split("=")[1];
                    log.log(Level.INFO, "LOGIN | userId = " + userId + " pw = " + password);

                    MemoryUserRepository memoryUserRepository = MemoryUserRepository.getInstance();
                    User user = memoryUserRepository.findUserById(userId);
                    if (user != null && user.getPassword().equals(password)) {
                        response302Cookie(dos, "true", "/");

                        return;
                    } else {
                        sendRedirect(dos, "/user/login_failed.html");
                    }

                    log.log(Level.INFO, "return findUserById | " + user);
                }
            }

            response200Header(dos, body.length);
            responseBody(dos, body);

        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void response200Cookie(DataOutputStream dos, String loginStatus) {
        try {
            log.log(Level.INFO, "HTTP 200 OK");

            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");

            dos.writeBytes("Cookie: logined=" + loginStatus + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void response302Cookie(DataOutputStream dos, String loginStatus, String location) {
        try {
            log.log(Level.INFO, "HTTP 302 Redirect | Cookie");

            dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");

            dos.writeBytes("Location: http://localhost:8080" + location + "\r\n");
            dos.writeBytes("Set-Cookie: logined=" + loginStatus + "\r\n");

            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void sendRedirect(DataOutputStream dos, String location) {
        try {
            log.log(Level.INFO, "HTTP 302 Redirect");

            dos.writeBytes("HTTP/1.1 302 Redirect\r\n");
            dos.writeBytes("Location: http://localhost:8080" + location + "\r\n");
            dos.writeBytes("\r\n");
            dos.flush();
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void sendFile(DataOutputStream dos, String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists() && file.isFile()) {
                byte[] fileBytes = readFileContents(file);
                response200Header(dos, fileBytes.length);
                responseBody(dos, fileBytes);
            } else {
                send404NotFound(dos);
            }
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void responseCSS(DataOutputStream dos, int lengthOfBodyContent, String contentType) {
        try {
            log.log(Level.INFO, "HTTP 200 OK");

            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/css;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private byte[] readFileContents(File file) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            byte[] buffer = new byte[1024];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            return baos.toByteArray();
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            log.log(Level.INFO, "HTTP 200 OK");

            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void send404NotFound(DataOutputStream dos) {
        try {
            log.log(Level.INFO, "HTTP 404 Not Found");

            dos.writeBytes("HTTP/1.1 404 Not Found\r\n\r\n");
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