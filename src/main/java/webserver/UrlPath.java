package webserver;

public enum UrlPath {
    ROOT_URL("./webapp"),
    HOME_URL("/index.html"),
    LOGIN_FAILED_URL("/user/login_failed.html"),
    LOGIN_URL("/user/login.html"),
    LIST_URL("/user/list.html");

    private final String path;

    UrlPath(String path){this.path=path;}

    public String getPath(){return path;}
}
