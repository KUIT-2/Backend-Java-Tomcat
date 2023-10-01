package enums;

public enum URL {
    ROOT_URL("./webapp"),
    HOME_URL("/index.html"),
    LOGIN_FAILED_URL("/user/login_failed.html"),
    LOGIN_URL("/user/login.html"),
    LIST_URL("/user/list.html");

    private final String url;

    URL(String url){
        this.url = url;
    }

    public String getUrl(){
        return url;
    }

}
