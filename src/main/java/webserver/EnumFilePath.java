package webserver;

public enum EnumFilePath {
    INDEX("webapp\\index.html"),
    SIGN_UP("webapp\\user\\form.html"),
    LOGIN("webapp\\user\\login.html"),
    LOGIN_FAILED("webapp\\user\\login_failed.html"),
    USER_LIST("webapp\\user\\list.html"),
    CSS("webapp\\css\\styles.css");

    private final String value;

    EnumFilePath(String path) {
        this.value = path;
    }

    public String getValue() {
        return this.value;
    }
}
