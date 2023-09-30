package webserver;

import Controller.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RequestMapper {
    HttpRequest httpRequest;
    HttpResponse httpResponse;


    Map<String, Controller> controllers = new HashMap<String, Controller>();

    public RequestMapper(HttpRequest httpRequest, HttpResponse httpResponse) {
        this.httpRequest = httpRequest;
        this.httpResponse = httpResponse;

        controllers.put(".html", new ForwardController());
        controllers.put("/", new HomeController());
        controllers.put("/user/signup", new SignUpController());
        controllers.put("/user/login", new LoginController());
        controllers.put("/user/userList", new ListController());
        controllers.put(".css", new CssController());

    }

    public void proceed() throws IOException {
        Controller controller = new ForwardController();
            // 요구 사항 1번
        if (httpRequest.getUrl().endsWith(".html")) {
            controller = controllers.get("./html");
        }
        else if(httpRequest.getUrl().endsWith(".css")||httpRequest.getUrl().endsWith(".png") || httpRequest.getUrl().endsWith(".jpg") || httpRequest.getUrl().endsWith(".jpeg") || httpRequest.getUrl().endsWith(".gif")){
            controller = controllers.get(".css");
        }
        else{
            controller = controllers.get(httpRequest.getUrl());
        }

        controller.execute(httpRequest, httpResponse);
    }
}
