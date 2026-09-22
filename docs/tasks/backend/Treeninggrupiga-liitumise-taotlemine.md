# Treeninggrupiga liitumise taotlemine

**Teenus:** `POST /api/training-groups/{trainingGroupId}/join-applications`

**Vaste balsamic mockupis:** "Treeningud" vaade (`TrainingsView.vue`, route `/trainings`), modaalaken "Treeninggrupiga liitumise taotlemine", lehekülg 1/1. (Lehekülje pilti `docs/balsamic/pdf-images/` kaustas veel ei ole — vt vaate märkmeid failis `docs/balsamic/notes/TrainingsView-markmed.md`.)

## Sisend

Path variable:

| Parameeter | Kirjeldus |
|---|---|
| `trainingGroupId` | Treeninggrupp, millega liituda soovitakse (`training_group.id`) |

Request body (`JoinApplicationRequestDto.java`):

```json
{
  "userId": 3
}
```

| Väli | Tüüp | Kirjeldus |
|---|---|---|
| `userId` | Integer | Taotluse esitav kasutaja (`user.id`) |

## Väljund

**Response (200 OK):** õnnestumisteade (`JoinApplicationResponseDto.java`).

```json
{
  "message": "Taotlus esitatud"
}
```

## Eesmärk

Kasutaja vajutab "Treeningud" tabeli real nupule "Taotle Liitumist" (kuvatakse siis, kui kasutaja pole veel treeninggrupi liige), mis avab modaalakna "Treeninggrupiga liitumise taotlemine" treeninggrupi infoga. Nupule "Taotle" vajutades saadetakse see päring.

Teenus **ei tee** kasutajat kohe liikmeks — see loob ainult ootel oleva liitumistaotluse (`join_application`, `status = 'PEN'`). Taotluse kinnitamine ja seeläbi `user_training_group` kirje loomine (nt treeneri/admini poolt) ei kuulu selle teenuse ega selle taski alla.

Teenuse loogika:

1. Kontrolli, et `trainingGroupId` viitab olemasolevale `training_group` kirjele.
2. Kontrolli, et kasutaja pole juba selle grupi liige (`user_training_group`) ega oma selle grupi kohta juba ootel taotlust (`join_application` kirjet staatusega `'PEN'`) — vastasel juhul viga `JOIN_APPLICATION_UNAVAILABLE`.
3. Loo `join_application` kirje (`user_id`, `training_group_id`, `status = 'PEN'`).
4. Tagasta õnnestumisteade.

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
-- FK: joinapplication_user (user_id -> user.id), join_application_training_group (training_group_id -> training_group.id)
```

`3_import.sql` ei sisalda hetkel ühtegi `join_application` näidiskirjet — see teenus on esimene, mis sinna kirjutab.

### `user_training_group`

```sql
CREATE TABLE user_training_group (
    id serial  NOT NULL,
    user_id int  NOT NULL,
    training_group_id int  NOT NULL,
    CONSTRAINT user_traininggroup_pk PRIMARY KEY (id)
);
```

Kasutatakse kontrollimaks, kas kasutaja on juba grupi liige (samm 2).

### `training_group`

Struktuur on kirjeldatud taskis "Treeningute nimekirja päring" (`Treeningute-nimekirja-paring.md`).

Näidisandmed (`3_import.sql`): `training_group.id = 5` (Golf, Edasijõudnud, Tore Golfklubi, Reena Sibul) on grupp, mille liige `customer@customer.ee` (`user.id = 3`) hetkel **pole** — vastavalt mockupile kuvatakse sellele reale "Taotle Liitumist".

## Veaolukorrad

Vea korral on response body kujul `{ "message": "...", "errorCode": "..." }` (`ApiError`).

| Olukord | Status code | Response body |
|---|---|---|
| `trainingGroupId` väärtusega treeninggruppi ei leitud | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'trainingGroupId' väärtusega: 999", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| Kasutaja on juba grupi liige või tal on juba ootel taotlus | 403 Forbidden | `{ "message": "Oled selle treeninggrupiga juba liitunud või taotlus on juba esitatud", "errorCode": "JOIN_APPLICATION_UNAVAILABLE" }` |
| Ootamatu serveri viga | 500 Internal Server Error | — |

## Vastuvõtu kriteeriumid

- [ ] Endpoint `POST /api/training-groups/{trainingGroupId}/join-applications` on olemas ja võtab vastu `JoinApplicationRequestDto` JSON-i
- [ ] Õnnestunud taotlemisel tagastatakse 200 ja `JoinApplicationResponseDto` sõnumiga "Taotlus esitatud"
- [ ] Andmebaasi luuakse üks `join_application` kirje staatusega `'PEN'`
- [ ] Kirjeldatud teenus ei loo `user_training_group` kirjet ega tee kasutajat automaatselt liikmeks
- [ ] Juba liikme korduv taotlus tagastab 403 ja errorCode'i `JOIN_APPLICATION_UNAVAILABLE` — uut `join_application` kirjet ei looda
- [ ] Kasutaja, kellel on sama grupi kohta juba ootel (`'PEN'`) taotlus, korduv taotlus tagastab samuti 403 ja errorCode'i `JOIN_APPLICATION_UNAVAILABLE`
- [ ] Olematu `trainingGroupId` tagastab 404 ja `PRIMARY_KEY_NOT_FOUND` sõnumiga `'trainingGroupId'`
- [ ] Kirjutatud on automaattestid: edukas taotlus, juba liikme taotlus, juba ootel taotlus, olematu `trainingGroupId`
