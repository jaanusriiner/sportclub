package ee.sportclub;

import lombok.Getter;

@Getter
public enum Error {
    INCORRECT_CREDENTIALS("Vale e-post või parool"),
    USER_UNAVAILABLE("Sellise kasutajanimega (email) aktiivne kasutaja on juba süsteemis olemas"),
    NOT_SPORTCLUB_TRAINER("Antud treener ei ole seotud antud spordiklubiga"),
    SKILL_LEVEL_SPORT_MISMATCH("Antud skill-level ei kuulu antud spordialale"),
    SPORT_MISSING("sportIds: Vali vähemalt üks spordiala"),
    PRIMARY_KEY_NOT_FOUND("Ei leidnud primary keyd"),
    JOIN_APPLICATION_UNAVAILABLE("Oled selle treeninggrupiga juba liitunud või taotlus on juba esitatud"),
    NOT_TRAINING_GROUP_MEMBER("Registreerumiseks pead olema treeninggrupi liige"),
    TRAINING_FULL("Sellel treeningul pole enam vabu kohti"),
    ALREADY_REGISTERED("Oled juba sellele treeningule registreerunud");


    private final String message;

    Error(String message) {
        this.message = message;
    }
}

