package Enum;

public enum Url {
    ROOT("./webapp"),
    HOME("/"),
    FORM("/user/form.html"),
    SIGNUP("/user/signup"),

    LOGIN("/user/login.html"),
    LOGIIN_REQUEST("/user/login"),
    LOGIN_FAILED("/user/login_failed"),
    USER_LIST("/user/userList"),
    LIST("/user/list.html"),
    CSS("/css/styles.css"),
    PNG("/img/KUIT.png"),
    JPEG("/img/picture.jpeg");

    private final String path;
    Url(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
