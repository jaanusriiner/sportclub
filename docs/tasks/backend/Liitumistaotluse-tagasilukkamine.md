# Liitumistaotluse tagasilükkamine

**Teenus:** `PUT /api/join-applications/{joinApplicationId}/reject`

**Vaste balsamic mockupis:** "Halda treeninggruppe ja treeninguid" vaade (`ManageTrainingsView.vue`, route `/manage-trainings`), sektsioon "Treeninggruppide liitumistaotlused", "rist" nupp rea kohta, lehekülg 1/1 (vt lisatud pilt `Liitumistaotluse-tagasilukkamine.png`). Vt ka vaate märkmeid failis `docs/balsamic/notes/ManageTrainingsView-markmed.md`.

![Mockup](./Liitumistaotluse-tagasilukkamine.png)

## Sisend

Path variable:

| Parameeter | Kirjeldus |
|---|---|
| `joinApplicationId` | Tagasi lükatav liitumistaotlus (`join_application.id`) |

Query parameetrid ja request body puuduvad.

## Väljund

**Response (200 OK):** `NONE` — teenus ei tagasta body-d.

## Eesmärk

Treener klõpsab "Treeninggruppide liitumistaotlused" tabeli real "rist" nupul — mockup ei näita selle juures eraldi kinnitusmodaali, tegevus toimub kohe. Pärast õnnestunud tagasilükkamist eemaldub rida nimekirjast (frontend laeb `GET /api/trainers/{trainerId}/join-applications` uuesti, vt `Treeninggrupi-liitumistaotluste-nimekirja-paring.md`).

Teenuse loogika:

1. Kontrolli, et `joinApplicationId` viitab olemasolevale `join_application` kirjele.
2. Kontrolli, et selle kirje `status` on hetkel `'PEN'` (ootel) — vastasel juhul viga `JOIN_APPLICATION_ALREADY_PROCESSED`.
3. Uuenda `join_application.status` väärtusele `'REJ'`. `user_training_group` kirjet **ei** looda.

**Vajalik eeltingimus:** `ee.sportclub.Status` enumis on hetkel ainult `STATUS_ACTIVE("A")` ja `STATUS_DELETED("D")` — sellele tuleb lisada `STATUS_REJECTED("REJ")` (vt ka `Liitumistaotluse-kinnitamine.md`, mis vajab omalt poolt `STATUS_ACCEPTED("ACC")`). Kontrolli implementeerimise ajaks, kas mõlemad väärtused on juba lisatud, enne kui lood duplikaadi.

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

`status` muutub `'PEN'` → `'REJ'`.

`user_training_group` tabelit see teenus **ei muuda** — tagasilükatud taotleja ei saa treeninggrupi liikmeks (struktuur vt `Liitumistaotluse-kinnitamine.md`).

`3_import.sql` ei sisalda hetkel ühtegi `join_application` näidiskirjet (vt `Treeninggrupi-liitumistaotluste-nimekirja-paring.md`).

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| `joinApplicationId` väärtusega taotlust ei leitud | 404 Not Found | `{"errorCode": "PRIMARY_KEY_NOT_FOUND", "message": "Ei leidnud primary keyd 'joinApplicationId' väärtusega: 999"}` |
| Taotlus on juba menetletud (`status` pole `'PEN'`) | 403 Forbidden | `{"errorCode": "JOIN_APPLICATION_ALREADY_PROCESSED", "message": "Taotlus on juba menetletud"}` |
| Ootamatu serveri viga | 500 Internal Server Error | — |

## Vastuvõtu kriteeriumid

- [ ] Endpoint `PUT /api/join-applications/{joinApplicationId}/reject` on olemas
- [ ] `Status.java` enumis on olemas `STATUS_REJECTED("REJ")`
- [ ] Õnnestunud tagasilükkamisel tagastatakse 200 ilma response body-ta ja `join_application.status` muutub `'REJ'`-ks
- [ ] Tagasilükkamine ei loo `user_training_group` kirjet
- [ ] Kui taotlus on juba menetletud (`status` != `'PEN'`), tagastatakse 403 ja errorCode `JOIN_APPLICATION_ALREADY_PROCESSED`
- [ ] Olematu `joinApplicationId` tagastab 404 ja `PRIMARY_KEY_NOT_FOUND` sõnumiga `'joinApplicationId'`
- [ ] Kirjutatud on automaattestid: edukas tagasilükkamine, juba menetletud taotlus, olematu `joinApplicationId`
