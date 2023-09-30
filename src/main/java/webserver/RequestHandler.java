package webserver;

import db.MemoryUserRepository;
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

            byte[] body = "Hello World".getBytes();
            response200Header(dos, body.length);
            responseBody(dos, body);
            //경로 할당
            String requestPath = parseRequestPath(br);
            if(requestPath.equals("/")||requestPath.equals("/index.html")){
                serverFile("webapp/index.html",dos);
            }
            else if(requestPath.equals("/form.html")){
                serverFile("user/form.html",dos);
            }else if(requestPath.equals("/user/signup")){
                handleFormSubmission(br,dos);//GET방식으로 구현
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
    private void handleFormSubmission(BufferedReader br,DataOutputStream dos) throws IOException {
        StringBuilder formData = new StringBuilder();
        String inputLineData ;
        while ((inputLineData = br.readLine()) != null && !inputLineData.isEmpty()){
            formData.append(inputLineData).append("\n");
        }
        /* &기호를 기준으로 key-value 쌍을 구분해서 담기 */
        String[] formPairs = formData.toString().split("&");
        /* 해쉬맵은 key & value 값을 쌍으로 저장학 위해 */
        Map<String, String> formDataMap = new HashMap<>();
        for (String pair : formPairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                formDataMap.put(key, value);
            }
        }

        MemoryUserRepository userRepository = MemoryUserRepository.getInstance();
        String userId = formDataMap.get("userId");
        String password = formDataMap.get("password");
        String name = formDataMap.get("name");
        String email = formDataMap.get("email");

        User newUser = new User(userId, password, name, email);
        userRepository.addUser(newUser);

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