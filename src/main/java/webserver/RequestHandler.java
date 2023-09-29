package webserver;

import db.MemoryUserRepository;
import http.util.HttpRequestUtils;
import http.util.IOUtils;
import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.User;

public class RequestHandler implements Runnable{
    Socket connection;
    MemoryUserRepository memoryUserRepository = MemoryUserRepository.getInstance();
    private static final Logger log = Logger.getLogger(RequestHandler.class.getName());

    public RequestHandler(Socket connection) {
        this.connection = connection;
    }

    //💡 클라이언트에서 `http://localhost:{port}/` 혹은 `http://localhost:{port}/index.html` 요청이 올 시에 webapp 폴더의 **index.html** 화면을 반환한다.
    @Override
    public void run() {
        log.log(Level.INFO, "New Client Connect! Connected IP : " + connection.getInetAddress() + ", Port : " + connection.getPort());
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()){
            //HTTP 메서드 요청URI HTTP버전
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            DataOutputStream dos = new DataOutputStream(out);
            String requestLine = br.readLine();

            String[] requestComponents = requestLine.split(" ");
            //[0]은 메서드 ex)GET [1]은 URL /index.html [2]는 HTTP버전
            String requestURL = requestComponents[1];
            int contentLength = 0;

            if (requestURL.equals("/") || requestURL.equals("/index.html")) {

                //파일 객체 생성
                File file = new File("/Users/beomjun/IdeaProjects/Backend-Java-Tomcat/webapp/index.html");
                //파일과 연결
                FileInputStream indexFis = new FileInputStream(file);
                byte[] indexBody = new byte[(int) file.length()];
                //파일 내용 읽어옴
                indexFis.read(indexBody);

                response200Header(dos, indexBody.length);
                responseBody(dos, indexBody);
                log.info(requestURL);
            }
            if (requestURL.equals("/user/login.html")) {
                File file = new File("/Users/beomjun/IdeaProjects/Backend-Java-Tomcat/webapp/user/login.html");
                FileInputStream loginFis = new FileInputStream(file);
                byte[] indexBody = new byte[(int) file.length()];

                response200Header(dos, loginFis.read(indexBody));
                responseBody(dos, indexBody);
                log.info(requestURL);
            }
            if (requestURL.equals("/user/form.html")) {
                File file = new File("/Users/beomjun/IdeaProjects/Backend-Java-Tomcat/webapp/user/form.html");
                FileInputStream formFis = new FileInputStream(file);
                byte[] indexBody = new byte[(int) file.length()];

                response200Header(dos, formFis.read(indexBody));
                responseBody(dos, indexBody);
                log.info(requestURL);
            }

//          get 방식
            //http://localhost:8080/user/signup?userId=asdfa&password=sdf&name=asdf&email=asdf%40asdf.asf
            if (requestURL.startsWith("/user/signup") && requestComponents[0].equals("GET")) {
                URL url = new URL("http://localhost:8080" + requestURL);
                Map<String, String> queryParams = HttpRequestUtils.parseQueryParameter(
                        url.getQuery());
                User user = new User(queryParams.get("userId"), queryParams.get("password"),
                        queryParams.get("name"), queryParams.get("email"));

                memoryUserRepository.addUser(user);

                String redirectUrl = "/index.html";
                response302RedirectHeader(dos, redirectUrl);
                log.info(requestURL);
            }
//          post 방식
            if (requestURL.equals("/user/signup") && requestComponents[0].equals("POST")) {
                while (true) {
                    String line = br.readLine();
                    if (line.equals("")) {
                        break;
                    }
                    if (line.startsWith("Content-Length")) {
                        contentLength = Integer.parseInt(line.split(": ")[1]);
                    }
                }
                String query = IOUtils.readData(br, contentLength);
                Map<String, String> queryParams = HttpRequestUtils.parseQueryParameter(query);
                User user = new User(queryParams.get("userId"), queryParams.get("password"),
                        queryParams.get("name"), queryParams.get("email"));

                log.info(query);
                memoryUserRepository.addUser(user);
                String redirectUrl = "/index.html";
                response302RedirectHeader(dos, redirectUrl);
                log.info(requestURL);
            }

        } catch (IOException e) {
            log.log(Level.SEVERE,e.getMessage());
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

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void response302RedirectHeader(DataOutputStream dos, String redirectUrl) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Location: " + redirectUrl + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

}