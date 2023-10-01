package webserver;

import db.MemoryUserRepository;
import model.User;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RequestHandler implements Runnable{
    Socket connection;
    private static final Logger log = Logger.getLogger(RequestHandler.class.getName());

    private static final String WEBAPP_PATH = "/Users/jaeyeon/KUIT_Missions/Backend-Java-Tomcat/webapp/";

    public RequestHandler(Socket connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        log.log(Level.INFO, "New Client Connect! Connected IP : " + connection.getInetAddress() + ", Port : " + connection.getPort());
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()){
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            DataOutputStream dos = new DataOutputStream(out);

            String line = br.readLine(); // '\r', '\n' , EOF 를 구분자로 한 줄을 읽어온다.
            int contentLength = 0;

//            HttpRequest httpRequest = HttpRequest.from(br);
//            HttpResponse httpResponse = new HttpResponse(dos);


            //요구사항 1: index.html 반환
            if (line != null && (line.startsWith("GET / ") || (line.startsWith("GET /index.html")))) {
                byte[] body = loadFile("index.html");
                if (body != null) {
                    response200Header(dos, body.length);
                    responseBody(dos, body);
                } else {
                    byte[] notFoundBody = "404 Not Found".getBytes();
                    response404Header(dos, notFoundBody.length);
                    responseBody(dos, notFoundBody);
                }
            }


            //요구사항 2: POST 방식으로 회원가입하기
            if (line != null && (line.startsWith("GET /user/form.html"))) {
                byte[] body = loadFile("user/form.html");
                if (body != null) {
                    response200Header(dos, body.length);
                    responseBody(dos, body);
                } else {
                    byte[] notFoundBody = "404 Not Found".getBytes();
                    response404Header(dos, notFoundBody.length);
                    responseBody(dos, notFoundBody);
                }
            }

            if (line != null && line.startsWith("POST /user/signup")) {

                while (true) {// HTTP 요청의 헤더 부분을 읽어서 요청 바디의 길이를 파악
                    line = br.readLine();
                    if (line.equals("")) { //빈 줄을 만나면 헤더 부분의 읽기를 중지
                        break;
                    }
                    if (line.startsWith("Content-Length")) {
                        contentLength = Integer.parseInt(line.split(": ")[1]); //Content-Length 헤더의 값을 파싱하여 contentLength 변수에 저장. ex) 'Content-Length: 1234' 에서 1234 추출
                    }
                }

                //HTTP 요청의 본문 부분을 읽고 사용자의 정보를 추출해 새로운 사용자를 저장하는 작업을 수행, 이후 클라이언트에 리다이렉션 응답
                char[] bodyChars = new char[contentLength];
                br.read(bodyChars, 0, contentLength); //요청 본문의 내용을 bodyChars에 읽어들임
                String requestBody = new String(bodyChars); //문자배열을 문자열로 변환 후 내용을 requestBody에 저장

                Map<String, String> bodyParams = parseQueryString(requestBody); //("userId=John&password=1234") 형태의 쿼리 문자열을 파싱하여 키와 값을 가지는 맵(bodyParams)으로 변환

                String userId = bodyParams.get("userId");
                String password = bodyParams.get("password");
                String name = bodyParams.get("name");
                String email = bodyParams.get("email");

                User newUser = new User(userId, password, name,email);

                MemoryUserRepository.getInstance().addUser(newUser); //MemoryUserRepository 저장소에 새로운 사용자 추가

                redirectTo(dos, "/"); //클라이언트는 지정된 위치 "/" 로 다시 요청을 보냄.
            }

            //로그인 페이지 이동
            if (line != null && (line.startsWith("GET /user/login.html"))) {
                byte[] body = loadFile("user/login.html");
                if (body != null) {
                    response200Header(dos, body.length);
                    responseBody(dos, body);
                } else {
                    byte[] notFoundBody = "404 Not Found".getBytes();
                    response404Header(dos, notFoundBody.length);
                    responseBody(dos, notFoundBody);
                }
            }

            //요구사항 5: 로그인 하기
            if (line != null && line.startsWith("POST /user/login")) {
                while (true) {
                    line = br.readLine();
                    if (line.equals("")) {
                        break;
                    }

                    if (line.startsWith("Content-Length")) {
                        contentLength = Integer.parseInt(line.split(": ")[1]);
                    }
                }
                char[] bodyChars = new char[contentLength];
                br.read(bodyChars, 0, contentLength);
                String requestBody = new String(bodyChars);

                Map<String, String> bodyParams = parseQueryString(requestBody);

                String userId = bodyParams.get("userId");
                String password = bodyParams.get("password");

                User loginUser = MemoryUserRepository.getInstance().findUserById(userId);

                if (loginUser != null && loginUser.getPassword().equals(password)) {
                    try{
                        dos.writeBytes("HTTP/1.1 302Found \r\n");
                        dos.writeBytes("Location: " + "/" + "\r\n");
                        dos.writeBytes("Set-Cookie: logined=true \r\n"); //유저가 동일하다면 쿠키 설정
                        dos.writeBytes("\r\n");
                    } catch (IOException e) {
                        log.log(Level.SEVERE, e.getMessage());
                    }
                } else {
                    byte[] body = loadFile("user/login_failed.html");
                    if (body != null) {
                        response200Header(dos, body.length);
                        responseBody(dos, body);
                    } else {
                        byte[] notFoundBody = "404 Not Found".getBytes();
                        response404Header(dos, notFoundBody.length);
                        responseBody(dos, notFoundBody);
                    }
                }

            }

            //요구사항6 :사용자 목록 출력
            if (line != null && line.startsWith("GET /user/userList")) {

                boolean loggedIn = false;
                while(!(line = br.readLine()).equals("")) {
                    if(line.startsWith("Cookie")) {
                        String cookies = line.split(": ")[1];
                        loggedIn = cookies.contains("logined=true");
                    }
                }
                if(loggedIn) {
                    byte[] body = loadFile("user/list.html");
                    if(body != null) {
                        response200Header(dos, body.length);
                        responseBody(dos, body);
                    } else {
                        byte[] notFoundBody = "404 Not Found".getBytes();
                        response404Header(dos, notFoundBody.length);
                        responseBody(dos, notFoundBody);
                    }
                } else {
                    byte[] body = loadFile("user/login.html");
                    if (body != null) {
                        response200Header(dos, body.length);
                        responseBody(dos, body);
                    } else {
                        byte[] notFoundBody = "404 Not Found".getBytes();
                        response404Header(dos, notFoundBody.length);
                        responseBody(dos, notFoundBody);
                    }
                }
            }


            if (line != null && line.endsWith(".css")) {
                byte[] body = loadFileFromURL(line.split(" ")[1].substring(1));
                if (body != null) {
                    response200HeaderWithContentType(dos, body.length, "text/css");
                    responseBody(dos, body);
                } else {
                    byte[] notFoundBody = "404 Not Found".getBytes();
                    response404Header(dos, notFoundBody.length);
                    responseBody(dos, notFoundBody);
                }
                return;
            }




        } catch (IOException e) {
            log.log(Level.SEVERE,e.getMessage());
        }
    }

    private Map<String, String> parseQueryString(String queryString) {
        Map<String, String> params = new HashMap<>();
        for (String param : queryString.split("&")) {
            String[] keyValue = param.split("=");
            if (keyValue.length > 1) {
                params.put(keyValue[0], keyValue[1]);
            }
        }

        return params;
    }


    private byte[] loadFile(String filename) throws IOException {
        File file = new File(WEBAPP_PATH + filename);
        if (file.exists() && file.isFile()) {
            try (FileInputStream fis = new FileInputStream(file);
                 ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

                byte[] buffer = new byte[1024];
                int bytesRead;

                while ((bytesRead = fis.read(buffer)) != -1) {
                    bos.write(buffer, 0, bytesRead);
                }

                return bos.toByteArray();
            }
        }
        return null;
    }

    private byte[] loadFileFromURL(String url) throws IOException {
        File file = new File(WEBAPP_PATH + url);
        if (file.exists() && file.isFile()) {
            try (FileInputStream fis = new FileInputStream(file);
                 ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

                byte[] buffer = new byte[1024];
                int bytesRead;

                while ((bytesRead = fis.read(buffer)) != -1) {
                    bos.write(buffer, 0, bytesRead);
                }

                return bos.toByteArray();
            }
        }
        return null;
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

    private void redirectTo(DataOutputStream dos, String locaion) {
        try{
            dos.writeBytes("HTTP/1.1 302Found \r\n");
            dos.writeBytes("Location: " + locaion + "\r\n");
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