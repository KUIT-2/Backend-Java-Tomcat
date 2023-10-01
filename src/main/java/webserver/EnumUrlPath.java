package webserver;

public enum EnumUrlPath {
    INDEX("/"),
    SIGN_UP_FORM("/user/form.html"),
    SIGN_UP_REQUEST("/user/signup"),
    LOGIN_FORM("/user/login.html"),
    LOGIN_REQUEST_FAILED("/user/login_failed"),
    LOGIN_RUEST("/user/login"),
    USER_LIST1("/user/userList"),
    USER_LIST2("/user/list.html"),
    CSS("/css/styles.css");

    private final String value;

    EnumUrlPath(String urlPath) {
        this.value = urlPath;
    }

    public String getValue() {
        return this.value;
    }
}
