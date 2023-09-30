package webserver;

public enum Url {
    ROOT("./webapp"),
    HOME("/index.html"),
    LOGIN("/user/login.html"),
    LOGIN_FAILED("/user/login_failed.html"),
    LIST("/user/list.html"),
    CSS("/css/styles.css"),
    FORM("/user/form.html");
    private String url;

    Url(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
