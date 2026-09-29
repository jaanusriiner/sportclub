# Treeneri treeninggruppide nimekirja päring

**Teenus:** `GET /api/trainers/{trainerId}/training-groups`

**Vaste balsamic mockupis:** "Halda treeninggruppe ja treeninguid" vaade (`ManageTrainingsView.vue`, route `/manage-trainings`), Sportklubi ja Treeninggrupp filter-dropdownid, lehekülg 1/1 (vt lisatud pilt `Treeneri-treeninggruppide-nimekirja-paring.png`). Vt ka vaate märkmeid failis `docs/balsamic/notes/ManageTrainingsView-markmed.md`.

![Mockup](./Treeneri-treeninggruppide-nimekirja-paring.png)

## Sisend

Path variable:

| Parameeter | Kirjeldus |
|---|---|
| `trainerId` | Treener, kelle enda treeninggruppide nimekirja päritakse (`user.id`) |

Query parameetrid ja request body puuduvad.

## Väljund

**Response (200 OK):** treeneri enda treeninggruppide nimekiri (`TrainerTrainingGroupDto.java` massiiv). Kui treeneril pole ühtegi treeninggruppi, tagastatakse tühi massiiv `[]`.

```json
[
  {
    "trainingGroupId": 1,
    "trainingGroupName": "Tennis - Algtase",
    "sportclubId": 1,
    "sportclubName": "Beeta Tenniseklubi"
  }
]
```

Väljade tähendus:

- `trainingGroupId` — `training_group.id`
- `trainingGroupName` — `training_group.name`
- `sportclubId` / `sportclubName` — `sportclub.id` / `sportclub.name` (`training_group.sportclub_id` kaudu)

Näidisandmed (`3_import.sql`): treener Jaana Kask (`user.id = 5`) on `training_group.user_id` väärtusega ainsa grupi "Tennis - Algtase" (`training_group.id = 1`, `sportclub.id = 1` "Beeta Tenniseklubi") omanik — tema jaoks tagastatakse ülaltoodud üherealine massiiv.

## Eesmärk

`ManageTrainingsView.vue` kutsub selle teenuse vaate avamisel, et täita lehe ülaosa "Sportklubi" ja "Treeninggrupp" filter-dropdownid. Frontend tuletab "Sportklubi" dropdowni valikud vastuse ridade distinct `sportclubId`/`sportclubName` väärtustest ning "Treeninggrupp" dropdowni valikud vastavalt valitud sportklubile filtreeritud ridadest. Mõlemas dropdownis on ka valik "Kõik", mis vastab järgneva teenuse (`GET /api/trainers/{trainerId}/training-dates`) query parameetri väärtusele `0`.

Teenus tagastab ainult treeninggrupid, mille omanik (`training_group.user_id`) on antud `trainerId` — teise treeneri treeninggrupid siia kunagi ei satu.

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

Filtreerimine toimub `user_id = trainerId` järgi.

### `sportclub`

```sql
CREATE TABLE sportclub (
    id serial  NOT NULL,
    name varchar(100)  NOT NULL,
    CONSTRAINT sportclub_pk PRIMARY KEY (id)
);
```

### `user`

```sql
CREATE TABLE "user" (
    id serial  NOT NULL,
    role_id int  NOT NULL,
    email varchar(255)  NOT NULL,
    password varchar(255)  NOT NULL,
    status varchar(3)  NOT NULL,
    CONSTRAINT user_pk PRIMARY KEY (id)
);
```

Kasutatakse ainult `trainerId` olemasolu kontrollimiseks (vt Veaolukorrad); `sport`, `skill_level` ja `training`/`training_date` tabeleid see teenus otseselt ei kasuta, kuna vastuses ei kajastata spordiala ega toimumisaega — need lisatakse alles järgneva `GET /api/trainers/{trainerId}/training-dates` teenusega.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| `trainerId` väärtusega kasutajat ei leitud | 404 Not Found | `{"errorCode": "PRIMARY_KEY_NOT_FOUND", "message": "Ei leidnud primary keyd 'trainerId' väärtusega: 99"}` |
| Ootamatu serveri viga | 500 Internal Server Error | — |

Kui `trainerId`-l on kasutaja olemas, aga tal pole ühtegi enda treeninggruppi, tagastatakse 200 ja tühi massiiv `[]` (mitte viga).

## Vastuvõtu kriteeriumid

- [ ] Endpoint `GET /api/trainers/{trainerId}/training-groups` on olemas
- [ ] Õnnestunud vastus on 200 ja JSON massiiv `TrainerTrainingGroupDto` objektidega, ainult antud `trainerId` omanduses olevate treeninggruppidega
- [ ] Teise treeneri treeninggrupid ei ole vastuses kunagi kaasas
- [ ] Kui treeneril pole ühtegi treeninggruppi, tagastatakse 200 ja tühi massiiv `[]`
- [ ] Tundmatu `trainerId` korral tagastatakse 404 koos `errorCode: PRIMARY_KEY_NOT_FOUND`
- [ ] Kirjutatud on automaattestid: mitme treeninggrupiga vastus (kui testandmestikus lisandub treenerile teine grupp), tühi tulemus, tundmatu `trainerId`
