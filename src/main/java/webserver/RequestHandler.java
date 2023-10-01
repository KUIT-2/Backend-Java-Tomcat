package webserver;

import db.MemoryUserRepository;
import http.util.HttpRequestUtils;
import http.util.IOUtils;
import model.User;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RequestHandler implements Runnable {
    Socket connection;
    private static final Logger log = Logger.getLogger(RequestHandler.class.getName());
    MemoryUserRepository userRepository = MemoryUserRepository.getInstance();

    public RequestHandler(Socket connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        log.log(Level.INFO, "New Client Connect! Connected IP : " + connection.getInetAddress() + ", Port : " + connection.getPort());
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            DataOutputStream dos = new DataOutputStream(out);

            String line;
            StringBuilder sb = new StringBuilder();
            int requestContentLength = 0;
            String requestContentBody;
            boolean allowAutoLogin = false;

            while ((line = br.readLine()) != null) {
                if ("".equals(line)) break;
                if (line.contains("Cookie")) {
                    // TODO: 자동로그인 설정
                    allowAutoLogin = isUserLoggedIn(line);
                }
                sb.append(line);
                sb.append("\n");
            }

            String requestHeader = sb.toString();
            String requestHeaderStartLine = getRequestHeaderLine(requestHeader);
            String requestURL = getRequestURL(requestHeaderStartLine);

//            User user = getUserInfoByGET(requestURL);
//            if (user != null) {
//                userRepository.addUser(user);
//                response302Header(dos);
//                return;
//            }

            requestContentLength = getRequestContentLength(requestHeader);
            requestContentBody = IOUtils.readData(br, requestContentLength);

            if (requestURL.strip().equals("/")) {
                response302Header(dos);
                return;
            }

            if (requestURL.equals("/user/signup")) {
                User user = getUserInfo(requestContentBody);
                userRepository.addUser(user);
                response302Header(dos);
                return;
            }

            if (requestURL.equals("/user/login")) {
                if (isLoginSuccess(requestContentBody)) {
                    response302HeaderWithLoginSuccess(dos);
                    return;
                }
                responseResource(dos, "/user/login_failed.html");
                return;
            }

            if (requestURL.equals("/user/userList")) {
                if (allowAutoLogin){
                    for (User user : userRepository.findAll()) {
                        System.out.println(user.getUserId());
                        System.out.println(user.getName());
                        System.out.println(user.getPassword());
                        System.out.println(user.getEmail());
                    }
                    responseResource(dos, "/user/list.html");
                    return;
                }
                response302Header(dos);
                return;

            }

            if (requestURL.endsWith(".css")) {
                responseCSSResource(dos, requestURL);
                return;
            }

            responseResource(dos, requestURL);


        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private boolean isUserLoggedIn(String line) {
        return line.contains("logined=true");
    }


    private void responseCSSResource(DataOutputStream dos, String requestURL) throws IOException {
        File requestFile = getFile(getFullFilePath(requestURL));
        if (requestFile.isDirectory()) return;
        byte[] body = Files.readAllBytes(requestFile.toPath());

        response200CSSHeader(dos, body.length);
        responseBody(dos, body);
    }


    private boolean isLoginSuccess(String string) {
        Map<String, String> inputUser = HttpRequestUtils.parseQueryParameter(string);
        String inputUserId = inputUser.get("userId");
        String inputUserPw = inputUser.get("password");

        User user = userRepository.findUserById(inputUserId);
        if (user == null) return false;
        return inputUserPw.equals(user.getPassword());
    }

    private int getRequestContentLength(String requestHeader) {
        String[] requestHeaderTokenList = requestHeader.split("\n");
        for (String line : requestHeaderTokenList) {
            if (line.startsWith("Content-Length")) {
                return Integer.parseInt(line.split(": ")[1]);
            }
        }
        return 0;
    }

    private User getUserInfo(String str) {
        Map<String, String> userInfo = HttpRequestUtils.parseQueryParameter(str);
        return new User(userInfo.get("userId"), userInfo.get("password"), userInfo.get("name"), userInfo.get("email"));
    }

    private String getRequestURL(String requestHeaderLine) {
        return requestHeaderLine.split(" ")[1];
    }

    private String getRequestHeaderLine(String requestMessage) {
        return requestMessage.split("\n")[0];
    }

    private void responseResource(DataOutputStream dos, String url) throws IOException {
        File requestFile = getFile(getFullFilePath(url));
        if (requestFile.isDirectory()) return;
        byte[] body = Files.readAllBytes(requestFile.toPath());

        response200Header(dos, body.length);
        responseBody(dos, body);
    }

    private File getFile(String fullFilePath) {
        Path path = Paths.get(fullFilePath);
        return path.toFile();
    }

    private String getFullFilePath(String url) {
        return "./webapp" + url;
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

    private void response302Header(DataOutputStream dos) {
        String location = "/index.html";
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Location: " + location + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void response302HeaderWithLoginSuccess(DataOutputStream dos) {
        String location = "/index.html";
        try {
            dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
            dos.writeBytes("Set-Cookie: logined=true \r\n");
            dos.writeBytes("Location: " + location + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void response200CSSHeader(DataOutputStream dos, int length) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/css \r\n");
            dos.writeBytes("Content-Length: " + length + "\r\n");
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