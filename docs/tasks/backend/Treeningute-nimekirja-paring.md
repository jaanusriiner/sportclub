# Treeningute nimekirja päring

**Teenus:** `GET /api/trainings`

**Vaste balsamic mockupis:** "Treeningud" vaade (`TrainingsView.vue`, route `/trainings`), lehekülg 1/1. (Lehekülje pilti `docs/balsamic/pdf-images/` kaustas veel ei ole — vt vaate märkmeid failis `docs/balsamic/notes/TrainingsView-markmed.md`.)

## Sisend

Query parameetrid:

| Parameeter | Kohustuslik | Kirjeldus |
|---|---|---|
| `userId` | Jah | Kasutaja, kelle vaates olevate tabeli ridade `isTrainingGroupMember` väärtus arvutatakse (`user_training_group` olemasolu järgi). |
| `areaId` | Ei | Filtreerib treeninggrupi asukoha (`facility.area_id`) järgi. |
| `sportId` | Ei | Filtreerib spordiala järgi. |
| `trainerId` | Ei | Filtreerib treeneri (`training_group.user_id`) järgi. |
| `date` | Ei | Filtreerib järgmise treeningu toimumiskuupäeva (`training_date.start_date`) järgi. |
| `time` | Ei | Filtreerib järgmise treeningu toimumiskellaaja (`training_date.start_time`) järgi. |

Kõik valikulised filtrid kombineeritakse AND-loogikaga. Filtri puudumisel piirangut ei rakendata.

## Väljund

**Response (200 OK):** treeninggruppide nimekiri (`TrainingGroupOverviewDto.java` massiiv) koos iga grupi järgmise toimuva treeninguga.

```json
[
  {
    "trainingGroupId": 1,
    "sportId": 1,
    "sportName": "Tennis",
    "facilityId": 1,
    "facilityName": "Laagri Tennisekeskus",
    "trainerId": 5,
    "trainerName": "Jaana Kask",
    "sportclubId": 1,
    "sportclubName": "Beeta Tenniseklubi",
    "skillLevelId": 1,
    "skillLevelName": "Algtase",
    "trainingDateId": 1,
    "nextTrainingDate": "2026-09-20",
    "nextTrainingTime": "19:30",
    "userCount": 2,
    "maxSize": 4,
    "isTrainingGroupMember": true
  },
  {
    "trainingGroupId": 2,
    "sportId": 1,
    "sportName": "Tennis",
    "facilityId": 2,
    "facilityName": "Pärnu Tennise- ja Padelikeskus",
    "trainerId": 6,
    "trainerName": "Jaanus Tubli",
    "sportclubId": 2,
    "sportclubName": "Alta Tenniseklubi",
    "skillLevelId": 2,
    "skillLevelName": "Kesktase",
    "trainingDateId": 2,
    "nextTrainingDate": "2026-09-19",
    "nextTrainingTime": "18:30",
    "userCount": 4,
    "maxSize": 4,
    "isTrainingGroupMember": true
  },
  {
    "trainingGroupId": 3,
    "sportId": 1,
    "sportName": "Tennis",
    "facilityId": 3,
    "facilityName": "Tallink Tennisekeskus",
    "trainerId": 7,
    "trainerName": "Aivar Lahe",
    "sportclubId": 3,
    "sportclubName": "Laeva Tenniseklubi",
    "skillLevelId": 3,
    "skillLevelName": "Edasijõudnud",
    "trainingDateId": 3,
    "nextTrainingDate": "2026-09-20",
    "nextTrainingTime": "18:00",
    "userCount": 2,
    "maxSize": 4,
    "isTrainingGroupMember": true
  },
  {
    "trainingGroupId": 4,
    "sportId": 2,
    "sportName": "Football",
    "facilityId": 4,
    "facilityName": "Hiiu Staadion",
    "trainerId": 8,
    "trainerName": "Mihkel Maru",
    "sportclubId": 4,
    "sportclubName": "FC Jalg",
    "skillLevelId": 4,
    "skillLevelName": "Algtase",
    "trainingDateId": 4,
    "nextTrainingDate": "2026-09-20",
    "nextTrainingTime": "19:00",
    "userCount": 16,
    "maxSize": 22,
    "isTrainingGroupMember": true
  },
  {
    "trainingGroupId": 5,
    "sportId": 4,
    "sportName": "Golf",
    "facilityId": 5,
    "facilityName": "Niitvälja Golf",
    "trainerId": 9,
    "trainerName": "Reena Sibul",
    "sportclubId": 5,
    "sportclubName": "Tore Golfklubi",
    "skillLevelId": 5,
    "skillLevelName": "Edasijõudnud",
    "trainingDateId": 5,
    "nextTrainingDate": "2026-09-20",
    "nextTrainingTime": "14:00",
    "userCount": 0,
    "maxSize": 12,
    "isTrainingGroupMember": false
  }
]
```

Väljade tähendus:

- `trainingGroupId` — `training_group.id`
- `sportId` / `sportName` — `sport.id` / `sport.name` (`training_group.sport_id` kaudu)
- `facilityId` / `facilityName` — järgmise toimuva `training_date.facility_id` / `facility.name`
- `trainerId` / `trainerName` — `training_group.user_id` kaudu leitud `user.id` ja `profile.first_name` + `profile.last_name`
- `sportclubId` / `sportclubName` — `sportclub.id` / `sportclub.name` (`training_group.sportclub_id` kaudu)
- `skillLevelId` / `skillLevelName` — `skill_level.id` / `skill_level.name` (`training_group.skill_level_id` kaudu)
- `trainingDateId` — treeninggrupi järgmise (lähima tulevikus toimuva) `training_date.id`
- `nextTrainingDate` / `nextTrainingTime` — sama kirje `start_date` / `start_time`
- `userCount` / `maxSize` — sama kirje `user_count` / `max_size`
- `isTrainingGroupMember` — kas päritud `userId` on selle `training_group.id` kohta `user_training_group` tabelis olemas

Kui `userId` ei ole mõne treeninggrupi liige (`isTrainingGroupMember: false`), ei kuva frontend `userCount`/`maxSize` väärtusi kasutajale (vt `TrainingsView-markmed.md`), kuid backend tagastab need väljad ikkagi.

## Eesmärk

`TrainingsView.vue` kutsub selle teenuse vaate avamisel ja iga kord, kui kasutaja muudab tabeli kohal olevaid filtreid (Piirkond, Spordiala, Treener, Kuupäev, Kellaaeg). `isTrainingGroupMember` väli otsustab, kas tabeli reas kuvatakse nupp "Registreeru" (kasutaja on liige ja kohti on vabu), tekst "Kohad on täis" (kasutaja on liige, aga `userCount >= maxSize`) või nupp "Taotle Liitumist" (kasutaja pole liige).

## Seotud andmebaasi tabelid

Vt `docs/database/2_create.sql`.

### `training_group`

```sql
CREATE TABLE training_group (
    id serial  NOT NULL,
    sportclub_id int  NOT NULL,
    sport_id int  NOT NULL,
    user_id int  NOT NULL,
    name varchar(100)  NOT NULL,
    description varchar(255)  NULL,
    skill_level_id int  NOT NULL,
    CONSTRAINT traininggroup_pk PRIMARY KEY (id)
);
```

### `training`

```sql
CREATE TABLE training (
    id serial  NOT NULL,
    training_group_id int  NOT NULL,
    default_facility_id int  NOT NULL,
    name varchar(255)  NOT NULL,
    maxsize int  NOT NULL,
    description varchar(255)  NULL,
    default_start_date date  NULL,
    default_end_date date  NULL,
    default_start_time time  NOT NULL,
    default_end_time time  NOT NULL,
    duration int  NOT NULL,
    weekdays varchar(255)  NOT NULL,
    CONSTRAINT training_pk PRIMARY KEY (id)
);
```

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

Teenus kasutab iga treeninggrupi kohta ainult **järgmist tulevikus toimuvat** `training_date` kirjet (väikseim `start_date`/`start_time`, mis on praegusest hetkest hiljem).

### `facility`

```sql
CREATE TABLE facility (
    id serial  NOT NULL,
    area_id int  NOT NULL,
    name varchar(255)  NOT NULL,
    address varchar(255)  NOT NULL,
    description varchar(255)  NULL,
    CONSTRAINT facility_pk PRIMARY KEY (id)
);
```

`facility.area_id` on aluseks `areaId` filtrile.

### `sportclub`

```sql
CREATE TABLE sportclub (
    id serial  NOT NULL,
    name varchar(100)  NOT NULL,
    CONSTRAINT sportclub_pk PRIMARY KEY (id)
);
```

### `skill_level`

```sql
CREATE TABLE skill_level (
    id serial  NOT NULL,
    sport_id int  NOT NULL,
    name varchar(30)  NOT NULL,
    CONSTRAINT skilllevel_pk PRIMARY KEY (id)
);
```

### `sport`

```sql
CREATE TABLE sport (
    id serial  NOT NULL,
    name varchar(255)  NOT NULL,
    CONSTRAINT sport_pk PRIMARY KEY (id)
);
```

### `profile` (treeneri nime jaoks)

```sql
CREATE TABLE profile (
    id serial  NOT NULL,
    user_id int  NOT NULL,
    first_name varchar(255)  NOT NULL,
    last_name varchar(255)  NOT NULL,
    phone_number int  NOT NULL,
    area_id int  NOT NULL,
    CONSTRAINT profile_pk PRIMARY KEY (id)
);
```

`trainerName` moodustatakse `training_group.user_id` kaudu leitud kasutaja `profile.first_name` + `profile.last_name` väljadest.

### `user_training_group` (liikmelisuse kontroll)

```sql
CREATE TABLE user_training_group (
    id serial  NOT NULL,
    user_id int  NOT NULL,
    training_group_id int  NOT NULL,
    CONSTRAINT user_traininggroup_pk PRIMARY KEY (id)
);
```

`isTrainingGroupMember` on `true`, kui päritud `userId` + iga rea `trainingGroupId` kombinatsioon eksisteerib selles tabelis.

Näidisandmed (`3_import.sql`, pärast lisatud kandeid):

| training_group_id | sport | facility | trainer | sportclub | skill_level | training_date_id | kuupäev/kellaaeg | user_count/max_size |
|---|---|---|---|---|---|---|---|---|
| 1 | Tennis | Laagri Tennisekeskus | Jaana Kask | Beeta Tenniseklubi | Algtase | 1 | 2026-09-20 19:30 | 2/4 |
| 2 | Tennis | Pärnu Tennise- ja Padelikeskus | Jaanus Tubli | Alta Tenniseklubi | Kesktase | 2 | 2026-09-19 18:30 | 4/4 |
| 3 | Tennis | Tallink Tennisekeskus | Aivar Lahe | Laeva Tenniseklubi | Edasijõudnud | 3 | 2026-09-20 18:00 | 2/4 |
| 4 | Football | Hiiu Staadion | Mihkel Maru | FC Jalg | Algtase | 4 | 2026-09-20 19:00 | 16/22 |
| 5 | Golf | Niitvälja Golf | Reena Sibul | Tore Golfklubi | Edasijõudnud | 5 | 2026-09-20 14:00 | 0/12 |

`user_training_group` sisaldab kasutaja `customer@customer.ee` (`user.id = 3`) liikmelisust treeninggruppides 1–4, kuid mitte grupis 5 — see annab näidisandmetes `isTrainingGroupMember: false` ainult Golfi reale.

Tabelit `area` (`docs/database/2_create.sql`) kasutab see teenus ainult kaudselt `areaId` filtri kaudu (`facility.area_id`), otseselt `area` andmeid vastuses ei tagastata.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| `userId` väärtusega kasutajat ei leitud | 404 Not Found | `{"errorCode": "PRIMARY_KEY_NOT_FOUND", "message": "Ei leidnud primary keyd 'userId' väärtusega: 99"}` |
| Ootamatu serveri viga | 500 Internal Server Error | — |

`areaId`/`sportId`/`trainerId`/`date`/`time` filtrite puhul, mis ei anna ühtegi vastet, tagastatakse 200 ja tühi massiiv `[]` (mitte viga) — sama loogika, mis teistel selle projekti nimekirja-päringutel (vt `Piirkondade-nimekirja-paring.md`).

## Vastuvõtu kriteeriumid

- [ ] Endpoint `GET /api/trainings` on olemas ja nõuab kohustuslikku query parameetrit `userId`
- [ ] Õnnestunud vastus on 200 ja JSON massiiv `TrainingGroupOverviewDto` objektidega, üks kirje treeninggrupi kohta koos selle järgmise toimuva `training_date`-ga
- [ ] `isTrainingGroupMember` kajastab õigesti `user_training_group` tabeli sisu antud `userId` kohta
- [ ] Valikulised filtrid `areaId`, `sportId`, `trainerId`, `date`, `time` toimivad AND-loogikaga ja puuduva filtri korral piirangut ei rakendata
- [ ] Kui filtrile ei vasta ükski treeninggrupp, tagastatakse 200 ja tühi massiiv `[]`
- [ ] Treeninggrupp, millel pole ühtegi tulevikus toimuvat `training_date` kirjet (kõik toimumisajad on minevikus), jäetakse vastusest täielikult välja — sellist gruppi kasutajale ei kuvata
- [ ] Tundmatu `userId` korral tagastatakse 404 koos `errorCode: PRIMARY_KEY_NOT_FOUND`
- [ ] Kirjutatud on automaattestid: mitme treeninggrupiga vastus, tühi tulemus filtri korral, liikmelisuse tõene/väär juhtum, tundmatu `userId`
