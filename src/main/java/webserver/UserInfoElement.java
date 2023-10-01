package webserver;

public enum UserInfoElement {
    USER_ID("userId"),
    USER_PASSWORD("password"),
    USER_NAME("name"),
    USER_EMAIL("email");

    private final String element;

    UserInfoElement(String element){
        this.element = element;
    }

    public String getElement(){
        return this.element;
    }
}
