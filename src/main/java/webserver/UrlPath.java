package webserver;

public enum UrlPath {
    ROOT_URL("./webapp"),
    HOME_URL("/index.html"),
    LOGIN_FAILED_URL("/user/login_failed.html"),
    LOGIN_URL("/user/login.html"),
    LIST_URL("/user/list.html"),
    SIGN_UP("/user/signup"),
    LOGIN_PATH("/user/login"),
    LIST_PATH("/user/userList");

    private String url;

    UrlPath(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
