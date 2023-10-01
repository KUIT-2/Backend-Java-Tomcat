package Enum;

public enum LoginStatus {
    LOGINED("logined=true"), LOGOUT("logined=false");

    private final String status;

    LoginStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
