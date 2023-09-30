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

            String requestBody = IOUtils.readBody(br, contentLength);

            handleFormSubmission(requestBody,dos);
//             경로 할당
//           String requestPath = parseRequestPath(br);
//            if(requestPath.equals("/")||requestPath.equals("/index.html")){
//                serverFile("webapp/index.html",dos);
//            }
//            else if(requestPath.equals("/form.html")){
//                serverFile("user/form.html",dos);
//            }else if(requestPath.equals("/user/signup")){
//                handleFormSubmission(br,dos);//GET방식으로 구현
//            }
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

    private void serverFile(String filePath,DataOutputStream dos){
        /*파일을 읽어오는 과정에서 IOException 발생 가능성 있어서*/
        try{
            File file = new File(filePath);
            byte[] fileData = Files.readAllBytes(file.toPath());
        }catch (IOException e){
            log.log(Level.SEVERE,e.getMessage());
        }
    }

    /* form.html 에서 제출된 데이터를 처리
     * 회원 추가 */
    private void handleFormSubmission(String requestBody, DataOutputStream dos) throws IOException {
        // 파싱 및 처리
        Map<String, String> formDataMap = HttpRequestUtils.parseQueryParameter(requestBody);

        MemoryUserRepository userRepository = MemoryUserRepository.getInstance();
        String userId = formDataMap.get("userId");
        String password = formDataMap.get("password");
        String name = formDataMap.get("name");
        String email = formDataMap.get("email");

        // 새로운 사용자 생성 및 UserRepository에 추가
        User newUser = new User(userId, password, name, email);
        userRepository.addUser(newUser);

        // 회원 가입 성공 후 index.html로 리디렉션을 위한 응답 생성
        String redirectHtml = "<meta http-equiv=\"refresh\" content=\"0;url=/index.html\">";
        byte[] body = redirectHtml.getBytes();
        response200Header(dos, body.length);
        responseBody(dos, body);
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

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

}