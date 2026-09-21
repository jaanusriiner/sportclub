# Spordialade nimekirja päring

**Teenus:** `GET /api/sports`

**Vaste balsamic mockupis:** "Registreeri kasutajaks" vaade (`RegisterView.vue`, route `/register`), lehekülg 1/1 (vt lisatud pilt `Spordialade-nimekirja-paring.png`)

![Mockup](./Spordialade-nimekirja-paring.png)

## Sisend

Teenusel puuduvad sisendid.

## Väljund

**Response (200 OK):** kõikide spordialade nimekiri (`SportDto.java` massiiv), sorteeritud spordiala nime (`sport.name`) järgi tähestikuliselt (A–Z).

```json
[
  {
    "sportId": 3,
    "sportName": "Basketball"
  },
  {
    "sportId": 2,
    "sportName": "Football"
  },
  {
    "sportId": 4,
    "sportName": "Golf"
  },
  {
    "sportId": 1,
    "sportName": "Tennis"
  }
]
```

Järjekord ei sõltu ID-st ega andmebaasi lisamise järjekorrast — sorteerimine tehakse päringus (nt `ORDER BY name`), mitte frontendis.

- `sportId` — `sport.id`
- `sportName` — `sport.name`

## Eesmärk

Vaade "Registreeri kasutajaks" (`RegisterView.vue`) kutsub selle teenuse vaate avamisel, et täita mitmikvaliku rippmenüü "Spordiala huvid". Kasutaja valib sealt ühe või mitu spordiala ja valitud `sportId`-d saadetakse massiivina `sportIds` edasi teenusele `POST /api/register`.

## Seotud andmebaasi tabelid

Vt `docs/database/2_create.sql`.

### `sport`

```sql
CREATE TABLE sport (
    id serial  NOT NULL,
    name varchar(255)  NOT NULL,
    CONSTRAINT sport_pk PRIMARY KEY (id)
);
```

Näidisandmed (`3_import.sql`):

| id | name |
|---|---|
| 1 | Tennis |
| 2 | Football |
| 3 | Basketball |
| 4 | Golf |

Tabelit `sport` kasutavad ka `user_sport`, `sport_facility`, `skill_level` ja `training_group`, kuid see teenus neid ei puuduta.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| Ootamatu serveri viga | 500 Internal Server Error | — |

Teenusel puuduvad sisendid ja rollipiirangud, seega ärilisi veaolukordi ei ole. Mockup näitab `Veateated: —`.

## Vastuvõtu kriteeriumid

- [ ] Endpoint `GET /api/sports` on olemas ja ei nõua sisendparameetreid
- [ ] Õnnestunud vastus on 200 ja JSON massiiv `SportDto` objektidega (`sportId`, `sportName`)
- [ ] Vastuses on kõik `sport` tabeli read, sorteeritud `sportName` järgi tähestikuliselt (Basketball, Football, Golf, Tennis)
- [ ] Sorteerimine toimub backendis ja ei sõltu ID-dest (nt uus spordiala "Athletics" ilmub nimekirja esimesena)
- [ ] Kui `sport` tabel on tühi, tagastatakse 200 ja tühi massiiv `[]`
- [ ] Endpoint on kättesaadav ka sisse logimata kasutajale (vaade on mõeldud külastajale)
- [ ] Kirjutatud on automaattestid: nimekiri mitme spordialaga, tühi nimekiri
