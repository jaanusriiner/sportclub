package ee.sportclub;

import lombok.Getter;

@Getter
public enum Status {
    STATUS_ACTIVE("A"),
    STATUS_DELETED("D"),
    STATUS_ACCEPTED("ACC"),
    STATUS_REJECTED("REJ");

    private final String code;

    Status(String code) {
        this.code = code;
    }
}