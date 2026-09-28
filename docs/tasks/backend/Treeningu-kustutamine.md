# Treeningu kustutamine

**Teenus:** `DELETE /api/training-dates/{trainingDateId}`

**Vaste balsamic mockupis:** "Halda treeninggruppe ja treeninguid" vaade (`ManageTrainingsView.vue`, route `/manage-trainings`), modaalaken "Kinnitan kustutamise" (avaneb tabeli rea "prügikast" ikoonilt), lehekülg 1/1 (vt lisatud pilt `Treeningu-kustutamine.png`). Vt ka vaate märkmeid failis `docs/balsamic/notes/ManageTrainingsView-markmed.md`.

![Mockup](./Treeningu-kustutamine.png)

## Sisend

Path variable:

| Parameeter | Kirjeldus |
|---|---|
| `trainingDateId` | Kustutatav toimumiskord (`training_date.id`) |

Query parameetrid ja request body puuduvad.

## Väljund

**Response (200 OK):** `NONE` — teenus ei tagasta body-d.

## Eesmärk

Treener klõpsab haldusvaate tabeli real "prügikast" ikoonil, mis avab kinnitusmodaali "Kinnitan kustutamise" (kuvab toimumiskorra info). Nupule "Kinnitan treeningu kustutamise" vajutades saadetakse see päring; modaali "Sulge" nupp ei tee API kutset. Pärast õnnestunud kustutamist eemaldub rida tabelist ja vaade laeb nimekirja uuesti.

Teenuse loogika:

1. Kontrolli, et `trainingDateId` viitab olemasolevale `training_date` kirjele.
2. Kustuta `training_date` kirje (koos sellega seotud `user_training` kirjetega, kuna need viitavad `training_date.id`-le foreign key'ga — vt allpool).

**Lahtine ots (vajab täpsustust enne implementeerimist):** mockup ei erista, kas kustutamine peaks olema keelatud, kui toimumiskorral on juba registreerunud kasutajaid (`training_date.user_count > 0`). Praegu on eeldatud, et kustutamine on alati lubatud ja seotud `user_training` kirjed kustutatakse kaskaadis (registreerunud kasutajad kaotavad oma koha ilma eraldi teavituseta) — vt ka `docs/balsamic/notes/ManageTrainingsView-markmed.md`.

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

Kustutatav kirje. `training` (vanemkirje) ega `training_group` selle teenuse käigus ei kustu — ainult see üks toimumiskord.

### `user_training`

```sql
CREATE TABLE user_training (
    id serial  NOT NULL,
    user_id int  NOT NULL,
    training_date_id int  NOT NULL,
    CONSTRAINT user_training_pk PRIMARY KEY (id)
);
-- FK: user_training_training_date (training_date_id -> training_date.id)
```

Kirjed, mis viitavad kustutatavale `training_date.id`-le, tuleb samuti kustutada (vt Eesmärk, lahtine ots).

Näidisandmed (`3_import.sql`): `training_date.id = 1` on seotud ühe `user_training` kirjega (`user_id = 3`, `customer@customer.ee`) — see rida demonstreerib kaskaadkustutamise vajadust.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| `trainingDateId` väärtusega toimumiskorda ei leitud | 404 Not Found | `{"errorCode": "PRIMARY_KEY_NOT_FOUND", "message": "Ei leidnud primary keyd 'trainingDateId' väärtusega: 999"}` |
| Ootamatu serveri viga | 500 Internal Server Error | — |

## Vastuvõtu kriteeriumid

- [ ] Endpoint `DELETE /api/training-dates/{trainingDateId}` on olemas
- [ ] Õnnestunud kustutamisel tagastatakse 200 ilma response body-ta ja `training_date` kirje kustub andmebaasist
- [ ] Kustutatava `training_date`-ga seotud `user_training` kirjed kustuvad samuti (ei jää orvustunud FK viiteid)
- [ ] `training` ja `training_group` (vanemkirjed) jäävad kustutamisel puutumata
- [ ] Olematu `trainingDateId` tagastab 404 ja `PRIMARY_KEY_NOT_FOUND` sõnumiga `'trainingDateId'`
- [ ] Kirjutatud on automaattestid: edukas kustutamine (sh registreerunud kasutajaga rida), olematu `trainingDateId`
