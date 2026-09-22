# Minu treeningute nimekirja päring

**Teenus:** `GET /api/users/{userId}/trainings`

**Vaste balsamic mockupis:** "Treeningud" vaade (`TrainingsView.vue`, route `/trainings`), sektsioon "Minu Grupid ja Treeningud", lehekülg 1/1. (Lehekülje pilti `docs/balsamic/pdf-images/` kaustas veel ei ole — vt vaate märkmeid failis `docs/balsamic/notes/TrainingsView-markmed.md`.)

## Sisend

Path variable:

| Parameeter | Kirjeldus |
|---|---|
| `userId` | Kasutaja, kelle registreeritud treeninguid päritakse (`user.id`) |

Query parameetrid ja request body puuduvad.

## Väljund

**Response (200 OK):** kasutaja tulevaste registreeritud treeningute nimekiri (`MyTrainingDto.java` massiiv). Kui kasutajal pole ühtegi tulevast registreeringut, tagastatakse tühi massiiv `[]`.

```json
[
  {
    "trainingDateId": 1,
    "trainingGroupId": 1,
    "sportName": "Tennis",
    "facilityName": "Laagri Tennisekeskus",
    "trainerName": "Jaana Kask",
    "nextTrainingDate": "2026-09-20",
    "nextTrainingTime": "19:30",
    "userCount": 2,
    "maxSize": 4
  }
]
```

Väljade tähendus:

- `trainingDateId` — `training_date.id`, millele kasutaja on registreerunud (`user_training` kaudu)
- `trainingGroupId` — `training_date.training_id` → `training.training_group_id`
- `sportName` — `sport.name` (`training_group.sport_id` kaudu)
- `facilityName` — `training_date.facility_id` → `facility.name`
- `trainerName` — `training_group.user_id` kaudu leitud `profile.first_name` + `profile.last_name`
- `nextTrainingDate` / `nextTrainingTime` — `training_date.start_date` / `start_time`
- `userCount` / `maxSize` — `training_date.user_count` / `max_size`

Näidisandmed (`3_import.sql`): `customer@customer.ee` (`user.id = 3`) on registreerunud `user_training` kirjega `training_date.id = 1` peale (Tennis, Algtase, Laagri Tennisekeskus, Jaana Kask, 20.09.2026 19:30).

## Eesmärk

`TrainingsView.vue` kutsub selle teenuse vaate avamisel, et täita "Minu Grupid ja Treeningud" sektsioon sisse logitud kasutaja enda registreeritud treeningutega. Kui nimekiri on tühi, kuvab frontend teksti "Hetkel pole ühelegi treeningule registreeritud." selle teenuse vastuse põhjal.

Teenus tagastab ainult registreeringud, mille `training_date.start_date` (koos `start_time`-ga) on praegusest hetkest hiljem — möödas olevaid registreeringuid siin ei kuvata.

## Seotud andmebaasi tabelid

Vt `docs/database/2_create.sql`.

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

Selle tabeli kaudu leitakse kasutaja registreeringud.

### `training_date`, `training`, `training_group`, `facility`, `sport`, `profile`

Struktuur on identne taskiga "Treeningute nimekirja päring" (`Treeningute-nimekirja-paring.md`) — vaata sealt täpsed `CREATE TABLE` laused. See teenus kasutab samu tabeleid, ainult filtreerituna `user_training` kaudu ühe konkreetse kasutaja registreeringutele, mitte kõiki treeninggruppe.

## Veaolukorrad

Vea korral on response body kujul `{ "message": "...", "errorCode": "..." }` (`ApiError`).

| Olukord | Status code | Response body |
|---|---|---|
| `userId` väärtusega kasutajat ei leitud | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'userId' väärtusega: 99", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| Ootamatu serveri viga | 500 Internal Server Error | — |

## Vastuvõtu kriteeriumid

- [ ] Endpoint `GET /api/users/{userId}/trainings` on olemas
- [ ] Õnnestunud vastus on 200 ja JSON massiiv `MyTrainingDto` objektidega, ainult antud `userId` registreeringute kohta
- [ ] Vastuses on ainult tulevikus toimuvad treeningud (möödas olevad `training_date` kirjed välja jäetud)
- [ ] Kui kasutajal pole registreeringuid, tagastatakse 200 ja tühi massiiv `[]`
- [ ] Tundmatu `userId` korral tagastatakse 404 koos `errorCode: PRIMARY_KEY_NOT_FOUND`
- [ ] Kirjutatud on automaattestid: mitme registreeringuga kasutaja, registreeringuteta kasutaja, ainult tulevaste kuupäevade tagastamine, tundmatu `userId`
