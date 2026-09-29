# Treeninggrupi liitumistaotluste nimekirja päring

**Teenus:** `GET /api/trainers/{trainerId}/join-applications`

**Vaste balsamic mockupis:** "Halda treeninggruppe ja treeninguid" vaade (`ManageTrainingsView.vue`, route `/manage-trainings`), sektsioon "Treeninggruppide liitumistaotlused", lehekülg 1/1 (vt lisatud pilt `Treeninggrupi-liitumistaotluste-nimekirja-paring.png`). Vt ka vaate märkmeid failis `docs/balsamic/notes/ManageTrainingsView-markmed.md`.

![Mockup](./Treeninggrupi-liitumistaotluste-nimekirja-paring.png)

## Sisend

Path variable:

| Parameeter | Kirjeldus |
|---|---|
| `trainerId` | Treener, kelle enda treeninggruppidele esitatud ootel liitumistaotlusi päritakse (`user.id`) |

Query parameetrid ja request body puuduvad.

## Väljund

**Response (200 OK):** ootel (`status = 'PEN'`) liitumistaotluste nimekiri treeneri enda treeninggruppidele (`PendingJoinApplicationDto.java` massiiv). Kui ootel taotlusi pole, tagastatakse tühi massiiv `[]`.

```json
[
  {
    "joinApplicationId": 1,
    "userId": 10,
    "userFullName": "Jaanus Riiner",
    "sportclubId": 1,
    "sportclubName": "Beeta Tenniseklubi",
    "trainingGroupId": 1,
    "trainingGroupName": "Tennis - Algtase",
    "trainingGroupMemberCount": 5
  },
  {
    "joinApplicationId": 2,
    "userId": 11,
    "userFullName": "Toomas Vara",
    "sportclubId": 1,
    "sportclubName": "Beeta Tenniseklubi",
    "trainingGroupId": 1,
    "trainingGroupName": "Tennis - Algtase",
    "trainingGroupMemberCount": 4
  }
]
```

Väljade tähendus:

- `joinApplicationId` — `join_application.id`
- `userId` / `userFullName` — `join_application.user_id` kaudu leitud `user.id` ja `profile.first_name` + `profile.last_name`
- `sportclubId` / `sportclubName` — `join_application.training_group_id` kaudu leitud `training_group.sportclub_id` / `sportclub.name`
- `trainingGroupId` / `trainingGroupName` — `training_group.id` / `name`
- `trainingGroupMemberCount` — `training_group` liikmete koguarv, ehk `COUNT(*)` tabelist `user_training_group` antud `training_group_id` kohta. **See ei ole** ühegi üksiku `training_date.user_count`/`max_size` väärtus — grupi liikmelisus ja üksiku treeningkorra täituvus on eraldiseisvad (grupp ei pea kõiki liikmeid korraga ühele toimumiskorrale mahutama), seega võib see arv olla suurem kui ühegi toimumiskorra `max_size`.

**Lahtine ots (vajab täpsustust enne implementeerimist):** mockup näitab ka veergu "Taotluse Kuupäev", kuid `join_application` tabelis (vt allpool) pole taotluse esitamise kuupäeva jaoks veergu. See väli on kasutaja otsusel **teadlikult vastusest välja jäetud** — enne reaalset kuupäeva kuvamist tuleb `join_application` tabelile lisada vastav veerg (nt `date_added`, sarnaselt `training_date.date_added`-ga) ja teenust täiendada.

Näidisandmed: `3_import.sql` ei sisalda hetkel ühtegi `join_application` näidiskirjet (vt ka `docs/tasks/backend/Treeninggrupiga-liitumise-taotlemine.md`) — ülaltoodud JSON on seetõttu illustratiivne (kasutab mockup'i enda nimesid), mitte seemet vastavaid päris ID-sid.

## Eesmärk

`ManageTrainingsView.vue` kutsub selle teenuse vaate avamisel, et täita sektsiooni "Treeninggruppide liitumistaotlused" tabel. Iga rea juures on nupud taotluse kinnitamiseks (`PUT /api/join-applications/{joinApplicationId}/confirm`, vt `Liitumistaotluse-kinnitamine.md`) ja tagasilükkamiseks (`PUT /api/join-applications/{joinApplicationId}/reject`, vt `Liitumistaotluse-tagasilukkamine.md`). Pärast kummagi toimingut peab frontend selle nimekirja uuesti laadima, et menetletud rida enam ei kuvataks.

Teenus tagastab ainult `status = 'PEN'` (ootel) kirjed — juba kinnitatud/tagasi lükatud taotlused ei ole kunagi vastuses.

## Seotud andmebaasi tabelid

Vt `docs/database/2_create.sql`.

### `join_application`

```sql
CREATE TABLE join_application (
    id serial  NOT NULL,
    user_id int  NOT NULL,
    training_group_id int  NOT NULL,
    status varchar(3)  NOT NULL,
    CONSTRAINT joinapplication_pk PRIMARY KEY (id)
);
```

Filtreerimine: `status = 'PEN'` JA `training_group_id` kuulub treeneri (`training_group.user_id = trainerId`) treeninggruppide hulka.

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

`user_id = trainerId` piirab tulemuse treeneri enda gruppidele.

### `user_training_group`

```sql
CREATE TABLE user_training_group (
    id serial  NOT NULL,
    user_id int  NOT NULL,
    training_group_id int  NOT NULL,
    CONSTRAINT user_traininggroup_pk PRIMARY KEY (id)
);
```

Kasutatakse `trainingGroupMemberCount` arvutamiseks (`COUNT(*) WHERE training_group_id = ...`).

### `sportclub`, `profile`, `user`

`sportclub.name` (vt `Treeneri-treeninggruppide-nimekirja-paring.md`), `profile.first_name`/`last_name` (vt `Treeningute-nimekirja-paring.md`) ja `user` (`trainerId` olemasolu kontroll, vt Veaolukorrad) struktuurid on kirjeldatud viidatud tasides.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| `trainerId` väärtusega kasutajat ei leitud | 404 Not Found | `{"errorCode": "PRIMARY_KEY_NOT_FOUND", "message": "Ei leidnud primary keyd 'trainerId' väärtusega: 99"}` |
| Ootamatu serveri viga | 500 Internal Server Error | — |

Kui treeneril pole ühtegi ootel taotlust, tagastatakse 200 ja tühi massiiv `[]` (mitte viga).

## Vastuvõtu kriteeriumid

- [ ] Endpoint `GET /api/trainers/{trainerId}/join-applications` on olemas
- [ ] Õnnestunud vastus on 200 ja JSON massiiv `PendingJoinApplicationDto` objektidega
- [ ] Tulemuses on ainult `status = 'PEN'` kirjed treeninggruppide kohta, mille omanik on antud `trainerId`
- [ ] Juba kinnitatud/tagasi lükatud taotlused (kui `Status.java`-le on lisatud `ACC`/`REJ`, vt `Liitumistaotluse-kinnitamine.md`) ei kuvata
- [ ] `trainingGroupMemberCount` vastab `user_training_group` kirjete koguarvule antud `training_group_id` kohta
- [ ] Kui ootel taotlusi pole, tagastatakse 200 ja tühi massiiv `[]`
- [ ] Tundmatu `trainerId` korral tagastatakse 404 koos `errorCode: PRIMARY_KEY_NOT_FOUND`
- [ ] Kirjutatud on automaattestid: mitme taotlusega vastus, kinnitatud/tagasi lükatud taotluse väljajätmine, tühi tulemus, tundmatu `trainerId`
