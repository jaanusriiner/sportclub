# Treeningu muutmine

**Teenus:** `PUT /api/training-dates/{trainingDateId}`

**Vaste balsamic mockupis:** "Halda treeninggruppe ja treeninguid" vaade (`ManageTrainingsView.vue`, route `/manage-trainings`), modaalaken "Muuda treeningut" (avaneb tabeli rea "pliiats" ikoonilt), lehekülg 1/1 (vt lisatud pilt `Treeningu-muutmine.png`). Vt ka vaate märkmeid failis `docs/balsamic/notes/ManageTrainingsView-markmed.md`.

![Mockup](./Treeningu-muutmine.png)

## Sisend

Path variable:

| Parameeter | Kirjeldus |
|---|---|
| `trainingDateId` | Muudetav toimumiskord (`training_date.id`) |

Request body (`UpdateTrainingDateRequestDto.java`):

```json
{
  "description": "Kõvakattega siseväljak, kaasa oma reket",
  "maxSize": 4,
  "trainingDate": "2026-10-20",
  "trainingTime": "19:30"
}
```

| Väli | Tüüp | Kirjeldus |
|---|---|---|
| `description` | String | Uus kirjeldus — kirjutatakse `training.description` väljale (vt Väljund) |
| `maxSize` | Integer | Uus maksimaalne osalejate arv — kirjutatakse `training_date.max_size` väljale |
| `trainingDate` | LocalDate | Uus toimumiskuupäev — kirjutatakse `training_date.start_date` väljale |
| `trainingTime` | LocalTime | Uus toimumiskellaaeg — kirjutatakse `training_date.start_time` väljale |

## Väljund

**Response (200 OK):** `NONE` — teenus ei tagasta body-d.

## Eesmärk

Treener klõpsab haldusvaate tabeli real "pliiats" ikoonil, mis avab "Muuda treeningut" modaalakna eeltäidetud väärtustega (kirjeldus, max osalejate arv, kuupäev/kellaaeg — need pärinevad juba eelnevalt laetud `GET /api/trainers/{trainerId}/training-dates` vastusest, vt `Treeneri-treeningute-nimekirja-paring.md`). Modaali "Kinnitan" nupp saadab selle päringu ning sulgeb seejärel modaali; vaade laeb tabeli uuesti, et näidata muudetud väärtusi.

Teenuse loogika:

1. Kontrolli, et `trainingDateId` viitab olemasolevale `training_date` kirjele.
2. Kontrolli, et uus `maxSize` ei ole väiksem kui `training_date.user_count` (juba registreerunud kasutajate arv) — vastasel juhul viga `MAX_SIZE_TOO_LOW`.
3. Uuenda `training_date.max_size`, `start_date`, `start_time`.
4. Leia `training_date.training_id` kaudu vastav `training` kirje ja uuenda selle `description` väli.

`description` mõjutab **kõiki** sama `training` alla kuuluvaid `training_date` ridu (kirjeldus on ühine kogu treeningu, mitte üksiku toimumiskorra kohta) — see on teadlik disainiotsus, kuna `training_date` tabelis kirjelduse jaoks veergu pole (vt `docs/database/2_create.sql`).

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

Muudetavad väljad: `max_size`, `start_date`, `start_time`. `user_count`, `facility_id`, `duration`, `status`, `date_added` selle teenuse kaudu ei muutu.

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

Ainult `description` väli muutub, leitud `training_date.training_id` kaudu.

Näidisandmed (`3_import.sql`): `training_date.id = 1` kuulub `training.id = 1` alla ("Tennis - Algtase (Laagri)", praegune `description = 'Kõvakattega siseväljak'`, `training_date.max_size = 4`, `user_count = 2`) — seega uus `maxSize` ei tohi selle rea puhul olla väiksem kui `2`.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| `trainingDateId` väärtusega toimumiskorda ei leitud | 404 Not Found | `{"errorCode": "PRIMARY_KEY_NOT_FOUND", "message": "Ei leidnud primary keyd 'trainingDateId' väärtusega: 999"}` |
| Uus `maxSize` on väiksem kui `training_date.user_count` | 403 Forbidden | `{"errorCode": "MAX_SIZE_TOO_LOW", "message": "Maksimaalne osalejate arv ei tohi olla väiksem juba registreerunud kasutajate arvust"}` |
| Ootamatu serveri viga | 500 Internal Server Error | — |

## Vastuvõtu kriteeriumid

- [ ] Endpoint `PUT /api/training-dates/{trainingDateId}` on olemas ja võtab vastu `UpdateTrainingDateRequestDto` JSON-i
- [ ] Õnnestunud muutmisel tagastatakse 200 ilma response body-ta
- [ ] `training_date.max_size`, `start_date`, `start_time` uuenevad andmebaasis vastavalt request body väärtustele
- [ ] `training.description` uueneb vastava `training_date.training_id` kaudu leitud kirje juures (ja mõjutab ka sama `training` teisi `training_date` ridu)
- [ ] Kui uus `maxSize` on väiksem kui hetke `user_count`, tagastatakse 403 ja errorCode `MAX_SIZE_TOO_LOW`, andmebaasi ei muudeta
- [ ] Olematu `trainingDateId` tagastab 404 ja `PRIMARY_KEY_NOT_FOUND` sõnumiga `'trainingDateId'`
- [ ] Kirjutatud on automaattestid: edukas muutmine (sh kirjelduse levimine teistele sama `training` ridadele), liiga väike `maxSize`, olematu `trainingDateId`
