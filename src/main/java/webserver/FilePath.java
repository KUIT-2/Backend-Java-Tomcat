package webserver;

public enum FilePath {
    ROOT_PATH("C:/Users/권민혁/Desktop/2-2/KUIT/3week/Backend-Java-Tomcat/webapp/index.html"),
    FORM_TO_SIGNUP_PATH("C:/Users/권민혁/Desktop/2-2/KUIT/3week/Backend-Java-Tomcat/webapp/user/form.html"),
    LOGIN_PATH("C:/Users/권민혁/Desktop/2-2/KUIT/3week/Backend-Java-Tomcat/webapp/user/login.html"),
    LOGIN_FAILED_PATH("C:/Users/권민혁/Desktop/2-2/KUIT/3week/Backend-Java-Tomcat/webapp/user/login_failed.html"),
    USER_LIST_PATH("C:/Users/권민혁/Desktop/2-2/KUIT/3week/Backend-Java-Tomcat/webapp/user/list.html"),
    WEBAPP_PATH("C:/Users/권민혁/Desktop/2-2/KUIT/3week/Backend-Java-Tomcat/webapp/");

    private final String path;

    FilePath(String path){
        this.path = path;
    }

    public String getPath(){
        return this.path;
    }

}
