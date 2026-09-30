package ee.sportclub;

import lombok.Getter;

@Getter
public enum Error {
    INCORRECT_CREDENTIALS("Vale e-post või parool"),
    USER_UNAVAILABLE("Sellise kasutajanimega (email) aktiivne kasutaja on juba süsteemis olemas"),
    SPORT_MISSING("sportIds: Vali vähemalt üks spordiala"),
    NOT_TRAINING_GROUP_MEMBER("Registreerumiseks pead olema treeninggrupi liige"),
    TRAINING_FULL("Sellel treeningul pole enam vabu kohti"),
    ALREADY_REGISTERED("Oled juba sellele treeningule registreerunud");


    private final String message;

    Error(String message) {
        this.message = message;
    }
}

