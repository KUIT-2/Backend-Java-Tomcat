package webserver;

public enum StatusCode {
    REDIRECT(302),
    OK(200);

    private int statusCode;

    StatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

}
