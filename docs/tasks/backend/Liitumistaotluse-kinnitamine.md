# Liitumistaotluse kinnitamine

**Teenus:** `PUT /api/join-applications/{joinApplicationId}/confirm`

**Vaste balsamic mockupis:** "Halda treeninggruppe ja treeninguid" vaade (`ManageTrainingsView.vue`, route `/manage-trainings`), sektsioon "Treeninggruppide liitumistaotlused", "linnuke" nupp rea kohta, lehekülg 1/1 (vt lisatud pilt `Liitumistaotluse-kinnitamine.png`). Vt ka vaate märkmeid failis `docs/balsamic/notes/ManageTrainingsView-markmed.md`.

![Mockup](./Liitumistaotluse-kinnitamine.png)

## Sisend

Path variable:

| Parameeter | Kirjeldus |
|---|---|
| `joinApplicationId` | Kinnitatav liitumistaotlus (`join_application.id`) |

Query parameetrid ja request body puuduvad.

## Väljund

**Response (200 OK):** `NONE` — teenus ei tagasta body-d.

## Eesmärk

Treener klõpsab "Treeninggruppide liitumistaotlused" tabeli real "linnuke" nupul — mockup ei näita selle juures eraldi kinnitusmodaali, tegevus toimub kohe. Pärast õnnestunud kinnitamist eemaldub rida nimekirjast (frontend laeb `GET /api/trainers/{trainerId}/join-applications` uuesti, vt `Treeninggrupi-liitumistaotluste-nimekirja-paring.md`).

Teenuse loogika:

1. Kontrolli, et `joinApplicationId` viitab olemasolevale `join_application` kirjele.
2. Kontrolli, et selle kirje `status` on hetkel `'PEN'` (ootel) — vastasel juhul viga `JOIN_APPLICATION_ALREADY_PROCESSED`.
3. Loo uus `user_training_group` kirje (`user_id` ja `training_group_id` võetakse `join_application` kirjelt).
4. Uuenda `join_application.status` väärtusele `'ACC'`.

**Vajalik eeltingimus:** `ee.sportclub.Status` enumis on hetkel ainult `STATUS_ACTIVE("A")` ja `STATUS_DELETED("D")` — sellele tuleb lisada `STATUS_ACCEPTED("ACC")` (ja vastuseks teenusele `Liitumistaotluse-tagasilukkamine.md` ka `STATUS_REJECTED("REJ")`). `STATUS_PENDING("PEN")` on juba kavandatud failis `docs/tasks/backend/Treeninggrupiga-liitumise-taotlemine-IMPLEMENTATSIOON.md` — kontrolli, kas see on implementeerimise ajaks juba lisatud, enne kui lood duplikaadi.

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

`status` muutub `'PEN'` → `'ACC'`.

### `user_training_group`

```sql
CREATE TABLE user_training_group (
    id serial  NOT NULL,
    user_id int  NOT NULL,
    training_group_id int  NOT NULL,
    CONSTRAINT user_traininggroup_pk PRIMARY KEY (id)
);
```

Siia lisandub uus kirje kinnitamisel — see teeb taotleja treeninggrupi tegelikuks liikmeks.

`3_import.sql` ei sisalda hetkel ühtegi `join_application` näidiskirjet (vt `Treeninggrupi-liitumistaotluste-nimekirja-paring.md`) — see teenus on üks esimestest, mis sinna kirjutab/sealt loeb.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| `joinApplicationId` väärtusega taotlust ei leitud | 404 Not Found | `{"errorCode": "PRIMARY_KEY_NOT_FOUND", "message": "Ei leidnud primary keyd 'joinApplicationId' väärtusega: 999"}` |
| Taotlus on juba menetletud (`status` pole `'PEN'`) | 403 Forbidden | `{"errorCode": "JOIN_APPLICATION_ALREADY_PROCESSED", "message": "Taotlus on juba menetletud"}` |
| Ootamatu serveri viga | 500 Internal Server Error | — |

## Vastuvõtu kriteeriumid

- [ ] Endpoint `PUT /api/join-applications/{joinApplicationId}/confirm` on olemas
- [ ] `Status.java` enumis on olemas `STATUS_ACCEPTED("ACC")` (ja vajadusel `STATUS_PENDING("PEN")`, kui pole veel lisatud)
- [ ] Õnnestunud kinnitamisel tagastatakse 200 ilma response body-ta, `join_application.status` muutub `'ACC'`-ks ja luuakse uus `user_training_group` kirje õige `user_id`/`training_group_id` paariga
- [ ] Kui taotlus on juba menetletud (`status` != `'PEN'`), tagastatakse 403 ja errorCode `JOIN_APPLICATION_ALREADY_PROCESSED`, uut `user_training_group` kirjet ei looda
- [ ] Olematu `joinApplicationId` tagastab 404 ja `PRIMARY_KEY_NOT_FOUND` sõnumiga `'joinApplicationId'`
- [ ] Kirjutatud on automaattestid: edukas kinnitamine, juba menetletud taotlus, olematu `joinApplicationId`
