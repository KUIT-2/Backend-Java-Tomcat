package webserver;

import static Enum.ContentType.HTML;
import static Enum.HttpHeader.CONTENT_LENGTH;
import static Enum.HttpHeader.CONTENT_TYPE;
import static Enum.HttpHeader.COOKIE;
import static Enum.HttpMethod.GET;
import static Enum.HttpMethod.POST;
import static Enum.HttpStatus.OK;
import static Enum.HttpVersion.HTTP;
import static Enum.LoginStatus.LOGINED;
import static Enum.LoginStatus.LOGOUT;
import static Enum.QueryKey.EMAIL;
import static Enum.QueryKey.NAME;
import static Enum.QueryKey.PASSWORD;
import static Enum.QueryKey.USERID;
import static Enum.Url.CSS;
import static Enum.Url.FORM;
import static Enum.Url.HOME;
import static Enum.Url.JPEG;
import static Enum.Url.LIST;
import static Enum.Url.LOGIIN_REQUEST;
import static Enum.Url.LOGIN;
import static Enum.Url.LOGIN_FAILED;
import static Enum.Url.PNG;
import static Enum.Url.ROOT;
import static Enum.Url.SIGNUP;
import static Enum.Url.USER_LIST;

import controller.Controller;
import db.MemoryUserRepository;
import http.HttpRequest;
import http.util.HttpRequestUtils;
import http.util.IOUtils;
import java.io.*;
import java.net.Socket;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.User;
import Enum.*;

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

            HttpRequest httpRequest = new HttpRequest(br);
//            String requestLine = br.readLine();
//            String[] requestComponents = requestLine.split(" ");
//            //[0]은 메서드 ex)GET [1]은 URL /index.html [2]는 HTTP버전
//            HttpMethod method = HttpMethod.valueOf(requestComponents[0]);
//            Url url = Url.valueOf(requestComponents[1]);
//            int contentLength = 0;
//            String cookie = null;
//
//            while(true) {
//                String line = br.readLine();
//                log.info(line);
//                if (line.isEmpty()) {
//                    break;
//                }
//                if (line.startsWith(CONTENT_LENGTH.getHeader())) {
//                    contentLength = Integer.parseInt(line.split(": ")[1]);
//                }
//                if (line.startsWith(COOKIE.getHeader())) {
//                    cookie = line.split(" ")[1];
//                }
//            }

            if (method == GET && url == CSS) {
                File file = new File(ROOT.getPath(), url.getPath());
                FileInputStream fis = new FileInputStream(file);
                byte[] body = new byte[(int) file.length()];
                response200HeaderWithContentType(dos, fis.read(body), ContentType.CSS);
                responseBody(dos, body);
            }

            if (method == GET && url == JPEG) {
                File file = new File(ROOT.getPath(), url.getPath());
                FileInputStream fis = new FileInputStream(file);
                byte[] body = new byte[(int) file.length()];
                response200HeaderWithContentType(dos, fis.read(body), ContentType.JPEG);
                responseBody(dos, body);
            }

            if (method == GET && url == PNG) {
                File file = new File(ROOT.getPath(), url.getPath());
                FileInputStream fis = new FileInputStream(file);
                byte[] body = new byte[(int) file.length()];
                response200HeaderWithContentType(dos, fis.read(body), ContentType.PNG);
                responseBody(dos, body);
            }

            if (method == GET && url == HOME) {
                //파일 객체 생성
                File file = new File(ROOT.getPath(), url.getPath());
                //파일과 연결
                FileInputStream fis = new FileInputStream(file);
                byte[] indexBody = new byte[(int) file.length()];
                //파일 내용 읽어옴
                response200Header(dos, fis.read(indexBody));
                responseBody(dos, indexBody);
                log.info(url.getPath());
            }

            if (url == FORM) {
                File file = new File(ROOT.getPath(), url.getPath());
                FileInputStream fis = new FileInputStream(file);
                byte[] indexBody = new byte[(int) file.length()];

                response200Header(dos, fis.read(indexBody));
                responseBody(dos, indexBody);
                log.info(url.getPath());
            }

//          회원가입 get 방식
//            //http://localhost:8080/user/signup?userId=asdfa&password=sdf&name=asdf&email=asdf%40asdf.asf
//            if (requestURL.startsWith("/user/signup") && requestComponents[0].equals("GET")) {
//                URL url = new URL("http://localhost:8080" + requestURL);
//                Map<String, String> queryParams = HttpRequestUtils.parseQueryParameter(
//                        url.getQuery());
//                User user = new User(queryParams.get("userId"), queryParams.get("password"),
//                        queryParams.get("name"), queryParams.get("email"));
//
//                memoryUserRepository.addUser(user);
//
//                String redirectUrl = "/index.html";
//                response302RedirectHeader(dos, redirectUrl);
//                log.info(requestURL);
//            }
//          회원가입 post 방식
            if (url==SIGNUP && method==POST) {
                String query = IOUtils.readData(br, contentLength);
                Map<String, String> queryParams = HttpRequestUtils.parseQueryParameter(query);
                User user = new User(queryParams.get(USERID.getKey()), queryParams.get(PASSWORD.getKey()),
                        queryParams.get(NAME.getKey()), queryParams.get(EMAIL.getKey()));

                log.info(query);

                memoryUserRepository.addUser(user);
                response302RedirectHeader(dos, HOME);
                log.info(url.getPath());
            }

            if (url==LOGIN) {
                File file = new File(ROOT.getPath(), url.getPath());
                FileInputStream fis = new FileInputStream(file);
                byte[] indexBody = new byte[(int) file.length()];

                response200Header(dos, fis.read(indexBody));
                responseBody(dos, indexBody);
                log.info(url.getPath());
            }

            if (url==LOGIN_FAILED) {
                File file = new File(ROOT.getPath(), url.getPath());
                FileInputStream fis = new FileInputStream(file);
                byte[] indexBody = new byte[(int) file.length()];

                response200Header(dos, fis.read(indexBody));
                responseBody(dos, indexBody);
                log.info(url.getPath());
            }

//            로그인 시 전달되는 유저와 repository에 있는 유저가 동일한지 확인하고 동일하다면 헤더에 Cookie: logined=true를 추가하고, index.html 화면으로 redirect 한다.
//            로그인이 실패한다면 logined_failed.html로 redirect한다.
            if (url==LOGIIN_REQUEST) {
                String query = IOUtils.readData(br, contentLength);
                login302Header(dos, query);
            }

            if (url==USER_LIST) {
                list302Header(dos, cookie);
            }

            if (url== LIST) {
                File file = new File(ROOT.getPath(), url.getPath());
                FileInputStream loginFis = new FileInputStream(file);
                byte[] indexBody = new byte[(int) file.length()];

                response200Header(dos, loginFis.read(indexBody));
                responseBody(dos, indexBody);
                log.info(url.getPath());
            }

        } catch (IOException e) {
            log.log(Level.SEVERE,e.getMessage());
        }
    }

    private boolean isLogin(String userId, String password) {
        return memoryUserRepository.findUserById(userId) != null
                && memoryUserRepository.findUserById(userId).getPassword().equals(password);
    }

    private String location(String query) {
        Map<String, String> parse = HttpRequestUtils.parseQueryParameter(query);
        if (isLogin(parse.get(USERID.getKey()), parse.get(PASSWORD.getKey()))) {
            return "Location: " + HOME.getPath() + "\r\n";
        }
        return "Location: " + LOGIN_FAILED.getPath() + "\r\n";
    }

    private String setCookie(String query) {
        Map<String, String> parse = HttpRequestUtils.parseQueryParameter(query);
        return isLogin(parse.get(USERID.getKey()), parse.get(PASSWORD.getKey())) ?
                "Set-Cookie: " + LOGINED.getStatus()
                : "Set-Cookie: " + LOGOUT.getStatus();
    }

    private String cookieLocation(String cookie) {
        return isCookieLogined(cookie) ? "Location: " + LIST.getPath() + "\r\n"
                : "Location: " + LOGIN.getPath() + "\r\n";
    }

    private boolean isCookieLogined(String cookie) {
        return cookie != null && cookie.startsWith(LOGINED.getStatus());
    }

    private void response200HeaderWithContentType(DataOutputStream dos, int lengthOfBodyContent, ContentType contentType) {
        try {
            dos.writeBytes(HTTP.getVersion() + " " + OK.getMessage() + " " + OK.getCode() + "\r\n");
            dos.writeBytes(CONTENT_TYPE.getHeader() + ": " + contentType.getValue() + "\r\n");
            dos.writeBytes(CONTENT_LENGTH.getHeader() + " " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }
    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes(HTTP.getVersion() + " " + OK.getMessage() + " " + OK.getCode() + "\r\n");
            dos.writeBytes(CONTENT_TYPE.getHeader() + ": " + HTML.getValue() + ";charset=utf-8\r\n");
            dos.writeBytes(CONTENT_LENGTH.getHeader() + lengthOfBodyContent + "\r\n");
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

    private void response302RedirectHeader(DataOutputStream dos, Url redirectUrl) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Location: " + redirectUrl.getPath() + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void login302Header(DataOutputStream dos, String query) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes(location(query));
            dos.writeBytes(setCookie(query));
            dos.writeBytes("\r\n");

        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void list302Header(DataOutputStream dos, String cookie) {
        try {
            log.info(cookie);
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes(cookieLocation(cookie));
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }
}