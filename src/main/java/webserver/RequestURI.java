package webserver;

public enum RequestURI {
    SLASH("/"),
    INDEX_HTML("/index.html"),
    USER_FORM_HTML("/user/form.html"),
    USER_SIGNUP("/user/signup"),
    USER_LOGIN_PAGE("/user/login"),
    USER_LOGIN_HTML("/user/login.html"),
    USER_LOGIN_FAILED_HTML("/user/login_failed.html"),
    USER_USER_LIST("/user/userList"),
    CSS(".css");

    private final String uri;

    RequestURI(String uri){
        this.uri = uri;
    }

    public String getUri(){
        return this.uri;
    }


}
