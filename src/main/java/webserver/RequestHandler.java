package webserver;

import db.MemoryUserRepository;
import http.util.IOUtils;
import model.User;

import java.io.*;
import java.net.Socket;
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

            String requestLine = br.readLine();
            log.log(Level.INFO, requestLine);
            String[] request = requestLine.split(" ");
            String httpMethod = request[0];
            String requestURL = request[1];
            String httpVersion = request[2];

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

//            log.log(Level.INFO, "httpMethod: " + request[0] + "requestURL: " + request[1] + "httpVersion: " + request[2]);

            if (httpMethod.equals("GET")) {
                if (requestURL.equals("/") || requestURL.equals("/index.html")) {
                    sendFile(dos, WEB_PORT + "/index.html");
                } else if (requestURL.equals("/user/form.html")) {
                    sendFile(dos, WEB_PORT + "/user/form.html");
                } else if (requestURL.equals("/user/signup")) {
                } else {
                    send404NotFound(dos);
                }
            }

            if (requestURL.equals("/user/signup")) {
                String queryString = IOUtils.readData(br, requestContentLength);
                log.log(Level.INFO, queryString);
                String[] querySplit = queryString.split("&");
                String userId = querySplit[0].split("=")[1];
                String password = querySplit[1].split("=")[1];
                String name = querySplit[2].split("=")[1];
                String email = querySplit[3].split("=")[1];

                log.log(Level.INFO, "userId = " + userId + " pw = " + password + " name = " + name + " email = " + email);

                User user = new User(userId, password, name, email);
                MemoryUserRepository memoryUserRepository = MemoryUserRepository.getInstance();
                memoryUserRepository.addUser(user);


//                sendFile(dos, WEB_PORT + "/index.html");

                sendRedirect(dos, "/");

            }

//            byte[] body = "Hello World".getBytes();
//            response200Header(dos, body.length);
//            responseBody(dos, body);

        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void sendRedirect(DataOutputStream dos, String location) {
        try {
            log.log(Level.INFO, "HTTP 302 Found");

            dos.writeBytes("HTTP/1.1 302 Found\r\n");
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