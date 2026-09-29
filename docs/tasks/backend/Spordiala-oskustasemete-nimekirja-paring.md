# Spordiala oskustasemete nimekirja päring

**Teenus:** `GET /api/sports/{sportId}/skill-levels`

**Vaste balsamic mockupis:** "Loo uus treeninggrupp" modaal (avaneb vaatelt `ManageTrainingsView.vue`, route `/manage-trainings`, nupult "Loo uus Treeninggrupp"), Skill-level dropdown, lehekülg 1/1 failis `docs/balsamic/SportClub_Loo_treeninggrupp.pdf` (vt lisatud pilt `Spordiala-oskustasemete-nimekirja-paring.png`). Vt ka vaate märkmeid failis `docs/balsamic/notes/ManageTrainingsView-markmed.md`.

![Mockup](./Spordiala-oskustasemete-nimekirja-paring.png)

## Sisend

Path variable:

| Parameeter | Kirjeldus |
|---|---|
| `sportId` | Spordiala, mille oskustasemeid päritakse (`sport.id`) |

Query parameetrid ja request body puuduvad.

## Väljund

**Response (200 OK):** antud spordiala oskustasemete nimekiri (`SkillLevelDto.java` massiiv). Kui spordialal pole ühtegi oskustaset, tagastatakse tühi massiiv `[]`.

```json
[
  {
    "skillLevelId": 1,
    "skillLevelName": "Algtase"
  },
  {
    "skillLevelId": 2,
    "skillLevelName": "Kesktase"
  },
  {
    "skillLevelId": 3,
    "skillLevelName": "Edasijõudnud"
  }
]
```

Väljade tähendus:

- `skillLevelId` — `skill_level.id`
- `skillLevelName` — `skill_level.name`

Näidisandmed (`3_import.sql`): `sport.id = 1` ("Tennis") oskustasemed on `skill_level.id` 1 ("Algtase"), 2 ("Kesktase"), 3 ("Edasijõudnud").

**Lahtine ots:** mockup näitab "Loo uus treeninggrupp" vormil Skill-level väärtust "Nõrgem kesktase", mida `3_import.sql`-s ei eksisteeri (Tennis'e reaalsed väärtused on Algtase/Kesktase/Edasijõudnud, vt eespool). Vastust see ei mõjuta — teenus tagastab lihtsalt kõik andmebaasis olevad `skill_level` kirjed antud spordiala kohta, treener saab valida ainult nende seast.

## Eesmärk

"Loo uus treeninggrupp" modaal kutsub selle teenuse iga kord, kui treener valib või muudab Spordiala dropdowni väärtust, et täita Skill-level dropdowni valikud vastavalt valitud spordialale. Kui Spordiala valikut muudetakse pärast Skill-level valimist, tuleb Skill-level valik lähtestada (varasem valik ei pruugi enam uue spordiala nimekirjas eksisteerida).

## Seotud andmebaasi tabelid

Vt `docs/database/2_create.sql`.

### `skill_level`

```sql
CREATE TABLE skill_level (
    id serial  NOT NULL,
    sport_id int  NOT NULL,
    name varchar(30)  NOT NULL,
    CONSTRAINT skilllevel_pk PRIMARY KEY (id)
);
```

Filtreerimine: `sport_id = sportId`.

### `sport`

```sql
CREATE TABLE sport (
    id serial  NOT NULL,
    name varchar(255)  NOT NULL,
    CONSTRAINT sport_pk PRIMARY KEY (id)
);
```

Kasutatakse ainult `sportId` olemasolu kontrollimiseks (vt Veaolukorrad).

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| `sportId` väärtusega spordiala ei leitud | 404 Not Found | `{"errorCode": "PRIMARY_KEY_NOT_FOUND", "message": "Ei leidnud primary keyd 'sportId' väärtusega: 99"}` |
| Ootamatu serveri viga | 500 Internal Server Error | — |

Kui spordialal pole ühtegi oskustaset, tagastatakse 200 ja tühi massiiv `[]` (mitte viga).

## Vastuvõtu kriteeriumid

- [ ] Endpoint `GET /api/sports/{sportId}/skill-levels` on olemas
- [ ] Õnnestunud vastus on 200 ja JSON massiiv `SkillLevelDto` objektidega, ainult antud `sportId` kohta
- [ ] Teise spordiala oskustasemed ei ole vastuses kunagi kaasas
- [ ] Kui spordialal pole ühtegi oskustaset, tagastatakse 200 ja tühi massiiv `[]`
- [ ] Tundmatu `sportId` korral tagastatakse 404 koos `errorCode: PRIMARY_KEY_NOT_FOUND`
- [ ] Kirjutatud on automaattestid: mitme oskustasemega vastus, tühi tulemus, tundmatu `sportId`
