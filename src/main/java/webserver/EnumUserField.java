package webserver;

public enum EnumUserField {
    userId("userId"),
    password("password"),
    name("name"),
    email("email");

    private final String value;

    EnumUserField(String userField) {
        this.value = userField;
    }

    public String getValue() {
        return this.value;
    }
}
