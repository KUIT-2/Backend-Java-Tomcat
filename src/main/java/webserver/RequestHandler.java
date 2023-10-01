package webserver;

import db.MemoryUserRepository;
import http.util.HttpRequestUtils;
import http.util.IOUtils;
import model.User;

import java.io.*;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RequestHandler implements Runnable{
    Socket connection;
    private static final Logger log = Logger.getLogger(RequestHandler.class.getName());

    public RequestHandler(Socket connection) {
        this.connection = connection;
    }

    /*RequestHandler 클래스의 run 메서드에서 클라이언트로부터의 요청을 받아들이고,
    응답을 생성하여 클라이언트에게 반환*/
    @Override
    public void run() {
        log.log(Level.INFO, "New Client Connect! Connected IP : " + connection.getInetAddress() + ", Port : " + connection.getPort());
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()){
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            DataOutputStream dos = new DataOutputStream(out);

            int contentLength = 0;
            while (true){
                String line = br.readLine();
                if(line.equals("")){
                    break;
                }
                if(line.startsWith("Content-Length")){
                    contentLength = Integer.parseInt(line.split(": ")[1]);
                }
            }

            // Request Body를 읽어와서 회원 가입 처리 또는 파일 반환 처리
            String requestBody = IOUtils.readBody(br, contentLength);
            String requestPath = parseRequestPath(br);

            //경로 따라서 처리
            if(requestPath.equals("/user/signup")){
                // POST 방식으로 회원 가입 요청 처리
                handleFormSubmission(requestBody, dos);
            } else if (requestPath.equals("/login")) {
                // 로그인 부분
                handleLogin(requestBody, dos);
            } else if(requestPath.equals("/")||requestPath.equals("/index.html")){
                serverFile("webapp/index.html",dos);
            } else if(requestPath.equals("/form.html")){
                serverFile("user/form.html",dos);
            }else{
                // 기타 페이지 처리
                serverFile(requestPath, dos);
            }
        } catch (IOException e) {
            log.log(Level.SEVERE,e.getMessage());
        }
    }

    /*Http 요청에서 경로 추출
    * 첫번째 라인에서 두번째 구획에서 경로 추출*/
    private String parseRequestPath(BufferedReader br) throws IOException{
        String[] requestLine = br.readLine().split(" ");//공백 기준 두번째
        return requestLine.length>1 ? requestLine[1] : "/";
    }

    private void serverFile(String filePath, DataOutputStream dos) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                byte[] fileData = Files.readAllBytes(file.toPath());
                response200Header(dos, fileData.length);
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

    /* form.html 에서 제출된 데이터를 처리
     * 회원 추가 */
    private void handleFormSubmission(String requestBody, DataOutputStream dos) throws IOException {
        // 파싱 및 처리 ,이렇게 하는지 모르겠음
        Map<String, String> formDataMap = HttpRequestUtils.parseQueryParameter(requestBody);

        MemoryUserRepository userRepository = MemoryUserRepository.getInstance();
        String userId = formDataMap.get("userId");
        String password = formDataMap.get("password");
        String name = formDataMap.get("name");
        String email = formDataMap.get("email");

        // 새로운 사용자 생성 및 UserRepository에 추가 , 뉴 인스턴스
        User newUser = new User(userId, password, name, email);
        userRepository.addUser(newUser);

        /* 회원 가입 성공 후 302 Found 상태 코드와 Location 헤더를 통한 리디렉션 부분
         * 회원 가입 후 로그인 페이지로 리디렉션 */
        String redirectUrl = "/login.html";
        response302Header(dos, redirectUrl);
    }
    /* 로그인 부분 */
    private void handleLogin(String requestBody, DataOutputStream dos) {
        Map<String, String> formDataMap = HttpRequestUtils.parseQueryParameter(requestBody);

        MemoryUserRepository userRepository = MemoryUserRepository.getInstance();
        String userId = formDataMap.get("userId");
        String password = formDataMap.get("password");

        if (userRepository.isValidUser(userId, password)) {
            /* 성공하면 index로! */
            response302Header(dos, "/index.html");
        } else {
            /*실패하면 지정된곳으로 이동*/
            response302Header(dos, "/login_failed.html");
        }
    }

    /* 정상 적으로 요청 처리 */
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
    /* 클라이언트에게 새로운 URL로 이동하라고 알리기 */
    private void response302Header(DataOutputStream dos, String redirectUrl) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found\r\n");
            dos.writeBytes("Set-Cookie: logined=true\r\n");
            dos.writeBytes("Location: " + redirectUrl + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void response404Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 404 Not Found \r\n");
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