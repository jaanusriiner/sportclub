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
    MAX_SIZE_TOO_LOW("Maksimaalne osalejate arv ei tohi olla väiksem juba registreerunud kasutajate arvust"),
    NOT_TRAINING_GROUP_TRAINER("Antud treeninggrupp ei kuulu valitud treenerile"),
    NO_TRAINING_DATES("Valitud perioodi ei jää ühtegi valitud nädalapäeva"),
    ALREADY_REGISTERED("Oled juba sellele treeningule registreerunud"),
    NOT_REGISTERED("Sa ei ole sellele treeningule registreerunud"),
    NOT_ADMIN("Selle toimingu jaoks on vaja administraatori õigusi"),
    FACILITY_NAME_UNAVAILABLE("Sellise nimega asukoht on juba olemas"),
    CANNOT_MODIFY_SELF("Iseenda rolli ega staatust ei saa muuta"),
    USER_NOT_TRAINER("Spordiklubidega saab siduda ainult treeneri rolliga kasutajat"),
    JOIN_APPLICATION_ALREADY_PROCESSED("Taotlus on juba menetletud");

    private final String message;

    Error(String message) {
        this.message = message;
    }
}

