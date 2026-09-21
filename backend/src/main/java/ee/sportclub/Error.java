package ee.sportclub;

import lombok.Getter;

@Getter
public enum Error {
    INCORRECT_CREDENTIALS("Vale kasutajanimi või parool"),
    USER_UNAVAILABLE("Sellise kasutajanimega (email) aktiivne kasutaja on juba süsteemis olemas"),
    SPORT_MISSING("sportIds: Vali vähemalt üks spordiala");

    private final String message;

    Error(String message) {
        this.message = message;
    }
}

