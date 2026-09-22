# Treeningule registreerimine

**Teenus:** `POST /api/training-dates/{trainingDateId}/register`

**Vaste balsamic mockupis:** "Treeningud" vaade (`TrainingsView.vue`, route `/trainings`), modaalaken "Treeningule registreerimine", lehekülg 1/1. (Lehekülje pilti `docs/balsamic/pdf-images/` kaustas veel ei ole — vt vaate märkmeid failis `docs/balsamic/notes/TrainingsView-markmed.md`.)

## Sisend

Path variable:

| Parameeter | Kirjeldus |
|---|---|
| `trainingDateId` | Treeningu toimumisaeg, millele registreerutakse (`training_date.id`) |

Request body (`TrainingDateRegisterRequestDto.java`):

```json
{
  "userId": 3
}
```

| Väli | Tüüp | Kirjeldus |
|---|---|---|
| `userId` | Integer | Registreeruv kasutaja (`user.id`) |

## Väljund

**Response (200 OK):** õnnestumisteade (`TrainingRegisterResponseDto.java`).

```json
{
  "message": "Oled edukalt treeningule registreerinud"
}
```

## Eesmärk

Kasutaja vajutab "Treeningud" tabeli real nupule "Registreeru" (kuvatakse ainult siis, kui kasutaja on juba treeninggrupi liige ja treeningul on vabu kohti), mis avab modaalakna "Treeningule registreerimine" treeningu infoga. Nupule "Registreeru" vajutades saadetakse see päring.

Teenuse loogika:

1. Kontrolli, et `trainingDateId` viitab olemasolevale `training_date` kirjele.
2. Tuvasta `training_date` kaudu vastav `training_group` (`training_date.training_id` → `training.training_group_id`).
3. Kontrolli, et kasutaja on selle `training_group` liige (`user_training_group` kirje olemas) — vastasel juhul viga `NOT_TRAINING_GROUP_MEMBER`.
4. Kontrolli, et kasutaja pole sellele `training_date`-le juba registreerunud (`user_training` kirjet ei tohi juba olla) — vastasel juhul viga `ALREADY_REGISTERED`.
5. Kontrolli, et `training_date.user_count < training_date.max_size` — vastasel juhul viga `TRAINING_FULL`.
6. Loo `user_training` kirje (`user_id` + `training_date_id`) ja kasvata `training_date.user_count` väärtust ühe võrra.
7. Tagasta õnnestumisteade.

Sammud 3–5 peavad toimuma ühes tehingus koos kirjutamisega (samm 6), et vältida kaht üheaegset registreerumist, mis mõlemad läbivad täituvuskontrolli enne kirjutamist (race condition).

## Seotud andmebaasi tabelid

Vt `docs/database/2_create.sql`.

### `training_date`

```sql
CREATE TABLE training_date (
    id serial  NOT NULL,
    training_id int  NOT NULL,
    facility_id int  NOT NULL,
    start_date date  NOT NULL,
    start_time time  NOT NULL,
    duration int  NOT NULL,
    status varchar(3)  NOT NULL,
    user_count int  NOT NULL,
    max_size int  NOT NULL,
    date_added date  NOT NULL,
    CONSTRAINT training_date_pk PRIMARY KEY (id)
);
```

### `user_training`

```sql
CREATE TABLE user_training (
    id serial  NOT NULL,
    user_id int  NOT NULL,
    training_date_id int  NOT NULL,
    CONSTRAINT user_training_pk PRIMARY KEY (id)
);
-- FK: user_training_user (user_id -> user.id), user_training_training_date (training_date_id -> training_date.id)
```

### `user_training_group`

```sql
CREATE TABLE user_training_group (
    id serial  NOT NULL,
    user_id int  NOT NULL,
    training_group_id int  NOT NULL,
    CONSTRAINT user_traininggroup_pk PRIMARY KEY (id)
);
```

Kasutatakse liikmelisuse kontrolliks (samm 3).

### `training` ja `training_group`

Vajalikud, et jõuda `training_date`-lt vastava `training_group`-ini (`training_date.training_id` → `training.training_group_id`). Struktuur on kirjeldatud taskis "Treeningute nimekirja päring" (`Treeningute-nimekirja-paring.md`).

Näidisandmed (`3_import.sql`): `training_date.id = 1` (Tennis, Algtase, Laagri Tennisekeskus, 20.09.2026 19:30, `user_count = 2`, `max_size = 4`) kuulub `training_group.id = 1` alla, mille liige `customer@customer.ee` (`user.id = 3`) juba on (vt `user_training_group`).

## Veaolukorrad

Vea korral on response body kujul `{ "message": "...", "errorCode": "..." }` (`ApiError`).

| Olukord | Status code | Response body |
|---|---|---|
| `trainingDateId` väärtusega treeningu toimumisaega ei leitud | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'trainingDateId' väärtusega: 999", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| Kasutaja pole vastava treeninggrupi liige | 403 Forbidden | `{ "message": "Registreerumiseks pead olema treeninggrupi liige", "errorCode": "NOT_TRAINING_GROUP_MEMBER" }` |
| Treeningul pole enam vabu kohti (`user_count >= max_size`) | 403 Forbidden | `{ "message": "Sellel treeningul pole enam vabu kohti", "errorCode": "TRAINING_FULL" }` |
| Kasutaja on sellele treeningule juba registreerunud | 403 Forbidden | `{ "message": "Oled juba sellele treeningule registreerunud", "errorCode": "ALREADY_REGISTERED" }` |
| Ootamatu serveri viga | 500 Internal Server Error | — |

## Vastuvõtu kriteeriumid

- [ ] Endpoint `POST /api/training-dates/{trainingDateId}/register` on olemas ja võtab vastu `TrainingDateRegisterRequestDto` JSON-i
- [ ] Õnnestunud registreerimisel tagastatakse 200 ja `TrainingRegisterResponseDto` sõnumiga "Oled edukalt treeningule registreerinud"
- [ ] Andmebaasi luuakse üks `user_training` kirje ja `training_date.user_count` kasvab ühe võrra
- [ ] Kasutaja, kes pole treeninggrupi liige, saab 403 ja errorCode'i `NOT_TRAINING_GROUP_MEMBER` — `user_training` kirjet ei looda
- [ ] Täis treeningule (`user_count >= max_size`) registreerumine tagastab 403 ja errorCode'i `TRAINING_FULL`
- [ ] Juba registreerunud kasutaja korduv registreerimine tagastab 403 ja errorCode'i `ALREADY_REGISTERED`
- [ ] Olematu `trainingDateId` tagastab 404 ja `PRIMARY_KEY_NOT_FOUND` sõnumiga `'trainingDateId'`
- [ ] Kaks samaaegset registreerumist viimasele vabale kohale ei tekita `user_count > max_size` olukorda (race condition kaitse)
- [ ] Kirjutatud on automaattestid: edukas registreerimine, mitte-liige, täis treening, juba registreeritud, olematu `trainingDateId`
