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
            if (requestPath.equals(EnumUrlPath.SIGN_UP_FORM.getValue())) {
                byte[] body = Files.readAllBytes(Paths.get(EnumFilePath.SIGN_UP.getValue()));
                response200Header(dos, body.length);
                responseBody(dos, body);
                return;
            }
            //회원가입 POST
            if (requestPath.equals(EnumUrlPath.SIGN_UP_REQUEST.getValue())) {
                int requestContentLength = getRequestContentLength(br);
                String data = IOUtils.readData(br, requestContentLength);
                Map<String, String> requestMap = HttpRequestUtils.parseQueryParameter(data);

                MemoryUserRepository memoryUserRepository = MemoryUserRepository.getInstance();
                User user = new User(
                        requestMap.get(EnumUserField.userId.getValue()),
                        requestMap.get(EnumUserField.password.getValue()),
                        requestMap.get(EnumUserField.name.getValue()),
                        requestMap.get(EnumUserField.email.getValue())
                );
                memoryUserRepository.addUser(user);

                response302Header(dos, "/");
                return;
            }
            //로그인 화면 GET
            if (requestPath.equals(EnumUrlPath.LOGIN_FORM.getValue())) {
                byte[] body = Files.readAllBytes(Paths.get(EnumFilePath.LOGIN.getValue()));
                response200Header(dos, body.length);
                responseBody(dos, body);
                return;
            }
            //로그인 failed
            if (requestPath.equals(EnumUrlPath.LOGIN_REQUEST_FAILED.getValue())) {
                byte[] body = Files.readAllBytes(Paths.get(EnumFilePath.LOGIN_FAILED.getValue()));
                response200Header(dos, body.length);
                responseBody(dos, body);
                return;
            }
            //로그인 요청 POST
            if (requestPath.equals(EnumUrlPath.LOGIN_RUEST.getValue())) {
                int requestContentLength = getRequestContentLength(br);
                String data = IOUtils.readData(br, requestContentLength);
                Map<String, String> requestMap = HttpRequestUtils.parseQueryParameter(data);

                MemoryUserRepository memoryUserRepository = MemoryUserRepository.getInstance();
                User user = memoryUserRepository.findUserById(requestMap.get(EnumUserField.userId.getValue()));
                if (user != null) {
                    boolean logined = user.getPassword().equals(requestMap.get(EnumUserField.password.getValue()));
                    if (logined) {
                        response302HeaderLogin(dos, EnumUrlPath.INDEX.getValue(), true);
                    } else {
                        response302HeaderLogin(dos, EnumUrlPath.LOGIN_REQUEST_FAILED.getValue(), false);
                    }
                } else {
                    response302HeaderLogin(dos, EnumUrlPath.LOGIN_REQUEST_FAILED.getValue(), false);
                }
                return;
            }
            //User List 출력
            if (requestPath.equals(EnumUrlPath.USER_LIST1.getValue()) || requestPath.equals(EnumUrlPath.USER_LIST2.getValue())) {
                if (getIsRequestLoginedTrue(br)) {
                    byte[] body = Files.readAllBytes(Paths.get(EnumFilePath.USER_LIST.getValue()));
                    response200Header(dos, body.length);
                    responseBody(dos, body);
                    return;
                }
                response302Header(dos, EnumUrlPath.LOGIN_FORM.getValue());
            }
            //css 요청
            if (requestPath.equals(EnumUrlPath.CSS.getValue())) {
                byte[] body = Files.readAllBytes(Paths.get(EnumFilePath.CSS.getValue()));
                response200HeaderCSS(dos, body.length);
                responseBody(dos, body);
                return;
            }
            //그 외에는 메인
            byte[] body = Files.readAllBytes(Paths.get(EnumFilePath.INDEX.getValue()));
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
            dos.writeBytes(
                    EnumHTTPVersion.HTTP_1_1.getValue() + " " +
                            EnumHTTPStatusCode.OK_200.getValue() + " " +
                            EnumHTTPStatusMessage.OK_200.getValue() + "\r\n"
            );
            dos.writeBytes(EnumHTTPHeader.ContentTypeHtml.getValue() + "\r\n");
            dos.writeBytes(EnumHTTPHeader.ContentLength.getValue() + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void response200HeaderCSS(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes(
                    EnumHTTPVersion.HTTP_1_1.getValue() + " " +
                            EnumHTTPStatusCode.OK_200.getValue() + " " +
                            EnumHTTPStatusMessage.OK_200 + "\r\n"
            );
            dos.writeBytes(EnumHTTPHeader.ContentTypeCSS.getValue() + "\r\n");
            dos.writeBytes(EnumHTTPHeader.ContentLength.getValue() + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos, String urlPath) {
        try {
            dos.writeBytes(
                    EnumHTTPVersion.HTTP_1_1.getValue() + " " +
                            EnumHTTPStatusCode.Found_302.getValue() + " " +
                            EnumHTTPStatusMessage.FOUND_302.getValue() + "\r\n"
            );
            dos.writeBytes(EnumHTTPHeader.Location.getValue() + urlPath + "\r\n");
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void response302HeaderLogin(DataOutputStream dos, String urlPath, Boolean logined) {
        try {
            dos.writeBytes(
                    EnumHTTPVersion.HTTP_1_1.getValue() + " " +
                            EnumHTTPStatusCode.Found_302.getValue() + " " +
                            EnumHTTPStatusMessage.FOUND_302.getValue() + "\r\n"
            );
            dos.writeBytes(EnumHTTPHeader.SetCookie.getValue() + "logined=" + logined.toString() + "\r\n");
            dos.writeBytes(EnumHTTPHeader.Location.getValue() + urlPath + "\r\n");
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