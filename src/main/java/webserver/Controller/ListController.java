package webserver.Controller;

import webserver.HttpRequest;
import webserver.HttpResponse;
import webserver.UrlPath;

import java.util.Map;

public class ListController implements Controller{
    @Override
    public void execute(HttpRequest httpRequest, HttpResponse httpResponse) {
        /*Map<String, String> headers = httpResponse.getHeaders();
        for(Map.Entry<String, String> entrySet : headers.entrySet()){
            System.out.println(entrySet.getKey() + " : " + entrySet.getValue());
        }*/
/*

        if(httpResponse.getHeaders().containsValue("logined=true")){
            httpResponse.redirect(UrlPath.LIST_URL.getUrl());
            return;
        }
        httpResponse.redirect(UrlPath.LOGIN_URL.getUrl());*/

        httpResponse.redirect(UrlPath.LIST_URL.getUrl());
    }
}
