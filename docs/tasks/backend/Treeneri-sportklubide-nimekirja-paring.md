# Treeneri sportklubide nimekirja päring

**Teenus:** `GET /api/trainers/{trainerId}/sportclubs`

**Vaste balsamic mockupis:** "Loo uus treeninggrupp" modaal (avaneb vaatelt `ManageTrainingsView.vue`, route `/manage-trainings`, nupult "Loo uus Treeninggrupp"), Spordiklubi dropdown, lehekülg 1/1 failis `docs/balsamic/SportClub_Loo_treeninggrupp.pdf` (vt lisatud pilt `Treeneri-sportklubide-nimekirja-paring.png`). Vt ka vaate märkmeid failis `docs/balsamic/notes/ManageTrainingsView-markmed.md`.

![Mockup](./Treeneri-sportklubide-nimekirja-paring.png)

## Sisend

Path variable:

| Parameeter | Kirjeldus |
|---|---|
| `trainerId` | Treener, kelle endaga seotud spordiklubide nimekirja päritakse (`user.id`) |

Query parameetrid ja request body puuduvad.

## Väljund

**Response (200 OK):** treeneriga seotud spordiklubide nimekiri (`TrainerSportclubDto.java` massiiv). Kui treeneril pole ühtegi seotud spordiklubi, tagastatakse tühi massiiv `[]`.

```json
[
  {
    "sportclubId": 2,
    "sportclubName": "Alta Tenniseklubi"
  }
]
```

Väljade tähendus:

- `sportclubId` — `sportclub.id`
- `sportclubName` — `sportclub.name`

Näidisandmed (`3_import.sql`): `sportclub_trainer` tabelis on kirje (`sportclub_id = 2`, `user_id = 6`) — treener Jaanus Tubli (`user.id = 6`) on seotud spordiklubiga "Alta Tenniseklubi" (`sportclub.id = 2`).

## Eesmärk

"Loo uus treeninggrupp" modaal kutsub selle teenuse avanemisel, et täita Spordiklubi dropdowni valikud. Erinevalt teenusest `GET /api/trainers/{trainerId}/training-groups` (vt `Treeneri-treeninggruppide-nimekirja-paring.md`, kasutatakse haldusvaate filter-dropdownides) ei eelda see teenus, et treeneril juba on valitavas klubis treeninggrupp — see põhineb otse `sportclub_trainer` seosel, mitte olemasolevatel `training_group` kirjetel. See on tahtlik: treener, kellel pole veel ühtegi treeninggruppi, peab siiski nägema klubi, kuhu ta esimese grupi luua saab.

## Seotud andmebaasi tabelid

Vt `docs/database/2_create.sql`.

### `sportclub_trainer`

```sql
CREATE TABLE sportclub_trainer (
    id serial  NOT NULL,
    sportclub_id int  NOT NULL,
    user_id int  NOT NULL,
    CONSTRAINT sportclub_trainer_pk PRIMARY KEY (id)
);
```

Filtreerimine: `user_id = trainerId`.

### `sportclub`

```sql
CREATE TABLE sportclub (
    id serial  NOT NULL,
    name varchar(100)  NOT NULL,
    CONSTRAINT sportclub_pk PRIMARY KEY (id)
);
```

### `user`

Kasutatakse ainult `trainerId` olemasolu kontrollimiseks (vt Veaolukorrad, struktuur vt `Treeneri-treeninggruppide-nimekirja-paring.md`).

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| `trainerId` väärtusega kasutajat ei leitud | 404 Not Found | `{"errorCode": "PRIMARY_KEY_NOT_FOUND", "message": "Ei leidnud primary keyd 'trainerId' väärtusega: 99"}` |
| Ootamatu serveri viga | 500 Internal Server Error | — |

Kui `trainerId`-l on kasutaja olemas, aga tal pole ühtegi seotud spordiklubi, tagastatakse 200 ja tühi massiiv `[]` (mitte viga).

## Vastuvõtu kriteeriumid

- [ ] Endpoint `GET /api/trainers/{trainerId}/sportclubs` on olemas
- [ ] Õnnestunud vastus on 200 ja JSON massiiv `TrainerSportclubDto` objektidega, ainult `sportclub_trainer` kaudu antud `trainerId`-ga seotud klubidega
- [ ] Tulemus ei sõltu sellest, kas treeneril juba on selles klubis treeninggrupp
- [ ] Kui treeneril pole ühtegi seotud spordiklubi, tagastatakse 200 ja tühi massiiv `[]`
- [ ] Tundmatu `trainerId` korral tagastatakse 404 koos `errorCode: PRIMARY_KEY_NOT_FOUND`
- [ ] Kirjutatud on automaattestid: mitme klubiga vastus, tühi tulemus, tundmatu `trainerId`
