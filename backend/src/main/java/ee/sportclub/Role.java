package ee.sportclub;


import lombok.Getter;

@Getter
public enum Role {
    ADMIN("admin"),
    TRAINER("trainer"),
    CUSTOMER("customer");

    private final String code;

    Role(String code) {
        this.code = code;
    }
}

