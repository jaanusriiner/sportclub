# Piirkondade nimekirja päring

**Teenus:** `GET /api/areas`

**Vaste balsamic mockupis:** "Registreeri kasutajaks" vaade (`RegisterView.vue`, route `/register`), lehekülg 1/1 (vt lisatud pilt `Piirkondade-nimekirja-paring.png`)

![Mockup](./Piirkondade-nimekirja-paring.png)

## Sisend

Teenusel puuduvad sisendid.

## Väljund

**Response (200 OK):** kõikide piirkondade nimekiri (`AreaDto.java` massiiv), sorteeritud piirkonna nime (`area.name`) järgi tähestikuliselt (A–Z).

```json
[
  {
    "areaId": 1,
    "areaName": "Harjumaa"
  },
  {
    "areaId": 4,
    "areaName": "Hiiumaa"
  },
  {
    "areaId": 2,
    "areaName": "Läänemaa"
  },
  {
    "areaId": 5,
    "areaName": "Pärnumaa"
  },
  {
    "areaId": 3,
    "areaName": "Saaremaa"
  }
]
```

Järjekord ei sõltu ID-st ega andmebaasi lisamise järjekorrast — sorteerimine tehakse päringus (nt `ORDER BY name`), mitte frontendis.

- `areaId` — `area.id`
- `areaName` — `area.name`

## Eesmärk

Vaade "Registreeri kasutajaks" (`RegisterView.vue`) kutsub selle teenuse vaate avamisel, et täita rippmenüü "Piirkond". Kasutaja valib sealt oma piirkonna ja valitud `areaId` saadetakse edasi teenusele `POST /api/register`.

## Seotud andmebaasi tabelid

Vt `docs/database/2_create.sql`.

### `area`

```sql
CREATE TABLE area (
    id serial  NOT NULL,
    name varchar(255)  NOT NULL,
    CONSTRAINT area_pk PRIMARY KEY (id)
);
```

Näidisandmed (`3_import.sql`):

| id | name |
|---|---|
| 1 | Harjumaa |
| 2 | Läänemaa |
| 3 | Saaremaa |
| 4 | Hiiumaa |
| 5 | Pärnumaa |

Tabelit `area` kasutavad ka `facility.area_id` ja `profile.area_id`, kuid see teenus neid ei puuduta.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| Ootamatu serveri viga | 500 Internal Server Error | — |

Teenusel puuduvad sisendid ja rollipiirangud, seega ärilisi veaolukordi ei ole. Mockup näitab `Veateated: —`.

## Vastuvõtu kriteeriumid

- [ ] Endpoint `GET /api/areas` on olemas ja ei nõua sisendparameetreid
- [ ] Õnnestunud vastus on 200 ja JSON massiiv `AreaDto` objektidega (`areaId`, `areaName`)
- [ ] Vastuses on kõik `area` tabeli read, sorteeritud `areaName` järgi tähestikuliselt (Harjumaa, Hiiumaa, Läänemaa, Pärnumaa, Saaremaa)
- [ ] Sorteerimine toimub backendis ja ei sõltu ID-dest (nt uus piirkond "Alfamaa" ilmub nimekirja esimesena)
- [ ] Kui `area` tabel on tühi, tagastatakse 200 ja tühi massiiv `[]`
- [ ] Endpoint on kättesaadav ka sisse logimata kasutajale (vaade on mõeldud külastajale)
- [ ] Kirjutatud on automaattestid: nimekiri mitme piirkonnaga, tühi nimekiri
