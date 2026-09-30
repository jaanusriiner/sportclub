package ee.sportclub;


import lombok.Getter;

@Getter
public enum UserRole {
    ADMIN("admin"),
    TRAINER("trainer"),
    CUSTOMER("customer");

    private final String code;

    UserRole(String code) {
        this.code = code;
    }
}

