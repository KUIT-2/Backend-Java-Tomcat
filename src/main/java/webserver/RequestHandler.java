package webserver;

// 내 폴더 클래스 import
import db.MemoryUserRepository;
import db.Repository;
import http.util.IOUtils;
import http.util.HttpRequestUtils;
import model.User;

// java API import
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RequestHandler implements Runnable{
    Socket connection;
    private final Repository userDB;
    private static final Logger log = Logger.getLogger(RequestHandler.class.getName());

    public RequestHandler(Socket connection) {
        this.connection = connection;
        userDB = MemoryUserRepository.getInstance();
    }

    @Override
    public void run() {
        log.log(Level.INFO, "New Client Connect! Connected IP : " + connection.getInetAddress() + ", Port : " + connection.getPort());
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()){

            // BufferedReader 란 인자로 취한 스트림에 버퍼링 기능을 추가한 입력 스트립 클래스이다. 똑같이 입력 스트림이지만 버퍼링 기능 추가
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            DataOutputStream dos = new DataOutputStream(out);

            //HttpRequest 에서 request 분석 끝냄. 요구사항 2-2
            HttpRequest httpRequest = HttpRequest.from(br);
            HttpResponse httpResponse = HttpResponse.to(dos);

            if(httpRequest.getMethod().equals(HttpMethod.GET.getMethod())){
                if(httpRequest.getPath().equals(RequestURI.SLASH.getUri()) || httpRequest.getPath().equals(RequestURI.INDEX_HTML.getUri())){
                    httpResponse.forward(FilePath.ROOT_PATH.getPath());
                    return;
                }
                if(httpRequest.getPath().equals(RequestURI.USER_FORM_HTML.getUri())){
                    httpResponse.forward(FilePath.FORM_TO_SIGNUP_PATH.getPath());
                    return;
                }
                if(httpRequest.getPath().startsWith(RequestURI.USER_SIGNUP.getUri())){
                    User user = new User(httpRequest.getBodyUserId(),
                                            httpRequest.getBodyPassword(),
                                            httpRequest.getBodyName(),
                                            httpRequest.getBodyEmail());
                    userDB.addUser(user);
                    httpResponse.redirect(RequestURI.INDEX_HTML.getUri());
                    return;
                }
                if(httpRequest.getPath().equals(RequestURI.USER_LOGIN_HTML.getUri())){
                    httpResponse.forward(FilePath.LOGIN_PATH.getPath());
                    return;
                }
                if(httpRequest.getPath().equals(RequestURI.USER_LOGIN_FAILED_HTML.getUri())){
                    httpResponse.forward(FilePath.LOGIN_FAILED_PATH.getPath());
                    return;
                }
                if(httpRequest.getPath().equals(RequestURI.USER_USER_LIST.getUri())){
                    if(httpRequest.getCookie()){
                        httpResponse.forward(FilePath.USER_LIST_PATH.getPath());
                        return;
                    }
                    httpResponse.redirect(RequestURI.USER_LOGIN_HTML.getUri());
                }
                if(httpRequest.getPath().endsWith(RequestURI.CSS.getUri())){
                    httpResponse.forwardContentType(FilePath.WEBAPP_PATH.getPath()+httpRequest.getPath(), "text/css");
                    return;
                }
            }

            if(httpRequest.getMethod().equals(HttpMethod.POST.getMethod())){
                if(httpRequest.getPath().equals(RequestURI.USER_SIGNUP.getUri())){
                    User user = new User(httpRequest.getBodyUserId(),
                                            httpRequest.getBodyPassword(),
                                            httpRequest.getBodyName(),
                                            httpRequest.getBodyEmail());
                    userDB.addUser(user);
                    httpResponse.redirect(RequestURI.INDEX_HTML.getUri());
                    return;
                }
                if(httpRequest.getPath().equals(RequestURI.USER_LOGIN_PAGE.getUri())){
                    User userInDB = userDB.findUserById(httpRequest.getBodyUserId());
                    if(userInDB != null && userInDB.getPassword().equals(httpRequest.getBodyPassword())) {
                        httpResponse.redirectWithCookie(RequestURI.INDEX_HTML.getUri());
                        return;
                    }
                    httpResponse.redirect(RequestURI.USER_LOGIN_FAILED_HTML.getUri());
                    return;
                }
            }

        } catch (IOException e) {
            log.log(Level.SEVERE,e.getMessage());
        }
    }
}