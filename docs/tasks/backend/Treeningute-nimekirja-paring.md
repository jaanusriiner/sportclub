# Treeningute nimekirja päring

**Teenus:** `GET /api/trainings`

**Vaste balsamic mockupis:** "Treeningud" vaade (`TrainingsView.vue`, route `/trainings`), lehekülg 1/1. (Lehekülje pilti `docs/balsamic/pdf-images/` kaustas veel ei ole — vt vaate märkmeid failis `docs/balsamic/notes/TrainingsView-markmed.md`.)

## Sisend

Query parameetrid:

| Parameeter | Kohustuslik | Kirjeldus |
|---|---|---|
| `requestUserId` | Jah | Päringu tegija. Selle järgi arvutatakse iga rea `userIsRegistered` (`user_training` olemasolu) ja `userIsTrainingGroupMember` (`user_training_group` olemasolu). Väärtuse `0` korral on mõlemad `false`. |
| `areaId` | Jah | Filtreerib toimumiskoha piirkonna (`facility.area_id`) järgi. `0` = ei filtreeri. |
| `sportId` | Jah | Filtreerib spordiala (`training_group.sport_id`) järgi. `0` = ei filtreeri. |
| `trainerId` | Jah | Filtreerib treeneri (`training_group.user_id`) järgi. `0` = ei filtreeri. |
| `dateFrom` | Jah | Tagastab treeningud **alates** sellest kuupäevast (`training_date.start_date >= dateFrom`). Formaat `YYYY-MM-DD`. |
| `timeFrom` | Jah | Tagastab **igal päeval** treeningud alates sellest kellaajast (`training_date.start_time >= timeFrom`). Formaat `HH:mm`. |
| `page` | Ei | Lehekülje number, algab `0`-st. Vaikimisi `0`. |
| `size` | Ei | Ridade arv ühel leheküljel. Vaikimisi `7`. |

Kõik filtrid kombineeritakse AND-loogikaga.

Näide:

```
GET /api/trainings?requestUserId=3&areaId=0&sportId=0&trainerId=0&dateFrom=2026-09-24&timeFrom=00:00&page=0&size=7
```

## Väljund

**Response (200 OK):** üks lehekülg treeningute nimekirjast (`TrainingGroupOverviewPageDto.java`), mille `trainings` väljas on `TrainingGroupOverviewDto.java` objektid ja ülejäänud väljad kirjeldavad lehekülge.

Iga `training_date` kirje on vastuses eraldi rida — treeninggruppide kaupa grupeerimist ei toimu. Read on sorteeritud `trainingDate` ja seejärel `trainingTime` järgi kasvavalt (sorteerimist frontend muuta ei saa).

Näide (näidisandmetega, `requestUserId=3`, ilma filtriteta, `dateFrom=2026-09-24`; lühiduse mõttes on näidatud ainult 2 esimest rida):

```json
{
  "trainings": [
    {
      "trainingGroupId": 2,
      "sportId": 1,
      "sportName": "Tennis",
      "facilityId": 2,
      "facilityName": "Pärnu Tennise- ja Padelikeskus",
      "areaId": 5,
      "trainerId": 6,
      "trainerName": "Jaanus Tubli",
      "sportclubId": 2,
      "sportclubName": "Alta Tenniseklubi",
      "skillLevelId": 2,
      "skillLevelName": "Kesktase",
      "trainingDateId": 4,
      "trainingDate": "2026-10-19",
      "trainingTime": "18:30:00",
      "status": "A",
      "userCount": 4,
      "maxSize": 4,
      "userIsRegistered": false,
      "userIsTrainingGroupMember": true
    },
    {
      "trainingGroupId": 1,
      "sportId": 1,
      "sportName": "Tennis",
      "facilityId": 1,
      "facilityName": "Laagri Tennisekeskus",
      "areaId": 1,
      "trainerId": 5,
      "trainerName": "Jaana Kask",
      "sportclubId": 1,
      "sportclubName": "Beeta Tenniseklubi",
      "skillLevelId": 1,
      "skillLevelName": "Algtase",
      "trainingDateId": 1,
      "trainingDate": "2026-10-20",
      "trainingTime": "19:30:00",
      "status": "A",
      "userCount": 2,
      "maxSize": 4,
      "userIsRegistered": true,
      "userIsTrainingGroupMember": true
    }
  ],
  "pageNumber": 0,
  "pageSize": 7,
  "totalElements": 6,
  "totalPages": 1
}
```

### Lehekülje väljad (`TrainingGroupOverviewPageDto`)

- `trainings` — antud lehekülje read (`List<TrainingGroupOverviewDto>`)
- `pageNumber` — päritud lehekülje number (algab `0`-st)
- `pageSize` — ridade arv leheküljel (päritud `size`)
- `totalElements` — filtritele vastavate ridade koguarv üle kõigi lehekülgede
- `totalPages` — lehekülgede koguarv

### Rea väljad (`TrainingGroupOverviewDto`)

- `trainingGroupId` — `training_group.id`
- `sportId` / `sportName` — `sport.id` / `sport.name` (`training_group.sport_id` kaudu)
- `facilityId` / `facilityName` — `training_date.facility_id` / `facility.name` (treeningu tegelik toimumiskoht)
- `areaId` — `facility.area_id`
- `trainerId` / `trainerName` — `training_group.user_id` ja selle kasutaja `profile.first_name` + `' '` + `profile.last_name` (profiili puudumisel `null`)
- `sportclubId` / `sportclubName` — `sportclub.id` / `sportclub.name` (`training_group.sportclub_id` kaudu)
- `skillLevelId` / `skillLevelName` — `skill_level.id` / `skill_level.name` (`training_group.skill_level_id` kaudu)
- `trainingDateId` — `training_date.id`
- `trainingDate` / `trainingTime` — sama kirje `start_date` / `start_time`
- `status` — sama kirje `status` (vt `Status` enum: `A` = aktiivne, `D` = kustutatud)
- `userCount` / `maxSize` — sama kirje `user_count` / `max_size`
- `userIsRegistered` — kas `requestUserId` + rea `trainingDateId` kombinatsioon on `user_training` tabelis olemas
- `userIsTrainingGroupMember` — kas `requestUserId` + rea `trainingGroupId` kombinatsioon on `user_training_group` tabelis olemas

Kui kasutaja ei ole treeninggrupi liige (`userIsTrainingGroupMember: false`), ei kuva frontend `userCount`/`maxSize` väärtusi (vt `TrainingsView-markmed.md`), kuid backend tagastab need väljad ikkagi.

## Eesmärk

`TrainingsView.vue` kutsub selle teenuse vaate avamisel, iga kord kui kasutaja muudab tabeli kohal olevaid filtreid (Piirkond, Spordiala, Treener, Kuupäev, Kellaaeg) ning kui kasutaja liigub tabeli all lehekülgede vahel. Filtri muutmisel alustab frontend uuesti leheküljelt `0`. `totalPages` põhjal kuvatakse lehekülgede navigatsioon.

Rea tegevus sõltub väljadest:

- `userIsTrainingGroupMember: false` → nupp "Taotle Liitumist"
- `userIsTrainingGroupMember: true` ja `userIsRegistered: true` → kasutaja on sellele treeningule juba registreerunud
- `userIsTrainingGroupMember: true`, `userIsRegistered: false` ja `userCount < maxSize` → nupp "Registreeru"
- `userIsTrainingGroupMember: true`, `userIsRegistered: false` ja `userCount >= maxSize` → tekst "Kohad on täis"

## Andmeallikas

Teenus loeb andmeid andmebaasi view'st `v_training_date_overview` (loomise skript `docs/database/2_create.sql` lõpus), mis ühendab tabelid:

```
training_date → training → training_group → sport / sportclub / skill_level
training_date → facility (area_id)
training_group.user_id → profile (LEFT JOIN, treeneri nimi)
```

View's on üks rida iga `training_date` kirje kohta. View'l on JPA entity `TrainingDateOverview` (`@Immutable`).

`userIsRegistered` ja `userIsTrainingGroupMember` ei ole view's, sest view ei saa parameetreid vastu võtta — need arvutatakse repository päringus `EXISTS` alampäringutega tabelitest `user_training` (entity `UserTraining`) ja `user_training_group` (entity `UserTrainingGroup`).

Kasutatavad tabelid on kirjeldatud failis `docs/database/2_create.sql`: `training_date`, `training`, `training_group`, `sport`, `sportclub`, `skill_level`, `facility`, `profile`, `user_training`, `user_training_group`.

### Näidisandmed (`3_import.sql`)

| training_date_id | training_group_id | sport | facility (area) | trainer | kuupäev/kellaaeg | user_count/max_size |
|---|---|---|---|---|---|---|
| 1 | 1 | Tennis | Laagri Tennisekeskus (1) | Jaana Kask | 2026-10-20 19:30 | 2/4 |
| 2 | 1 | Tennis | Laagri Tennisekeskus (1) | Jaana Kask | 2026-10-21 19:30 | 3/4 |
| 3 | 1 | Tennis | Laagri Tennisekeskus (1) | Jaana Kask | 2026-10-22 19:30 | 3/4 |
| 4 | 2 | Tennis | Pärnu Tennise- ja Padelikeskus (5) | Jaanus Tubli | 2026-10-19 18:30 | 4/4 |
| 5 | 3 | Tennis | Tallink Tennisekeskus (1) | Aivar Lahe | 2026-10-21 18:00 | 2/4 |
| 6 | 4 | Football | Hiiu Staadion (1) | Mihkel Maru | 2026-10-22 19:00 | 16/22 |
| 7 | 5 | Golf | Niitvälja Golf (1) | Reena Sibul | 2026-09-21 14:00 | 0/12 |

Kasutaja `customer@customer.ee` (`user.id = 3`) on gruppide 1 ja 2 liige ning registreerunud treeningule `training_date_id = 1`. `dateFrom=2026-09-24` korral jääb `training_date_id = 7` (Golf, 2026-09-21) vastusest välja, seega on `totalElements = 6`.

## Pagination

- Lehekülgede numeratsioon algab `0`-st.
- `size` vaikeväärtus on `7`.
- `totalElements` ja `totalPages` arvestavad kõiki rakendatud filtreid.
- Sorteerimine on fikseeritud (`trainingDate`, `trainingTime` kasvavalt) — `sort` query parameetrit ei toetata.
- Kui `page` on suurem kui viimane olemasolev lehekülg, tagastatakse 200 ja tühi `trainings` massiiv (`totalElements`/`totalPages` on ikkagi õiged).

Tehnilised märkused:

- Repository meetod võtab lisaks filtritele `Pageable` parameetri ja tagastab `Page<TrainingGroupOverviewDto>`. Service koostab `Pageable` objekti `PageRequest.of(page, size)` abil (ilma sorteerimiseta, sest järjekord on päringus) ja teisendab `Page` objekti `TrainingGroupOverviewPageDto`-ks.
- Kuna päring kasutab konstruktori avaldist (`select new ...`) ja `EXISTS` alampäringuid, tuleb `@Query` annotatsioonile lisada eraldi `countQuery` (sama `from` ja `where` osaga, `select count(v)`), et Spring ei peaks count-päringut ise tuletama.
- Controller tagastab `TrainingGroupOverviewPageDto`, mitte Springi `Page` objekti.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| Kohustuslik parameeter puudub või on vales formaadis | 400 Bad Request | Springi vaikimisi veavastus |
| Ootamatu serveri viga | 500 Internal Server Error | — |

Tundmatu `requestUserId` ei ole viga — kasutajat ei valideerita, sellisel juhul on kõigil ridadel `userIsRegistered` ja `userIsTrainingGroupMember` väärtus `false`.

Filtrite puhul, mis ei anna ühtegi vastet, tagastatakse 200, tühi `trainings` massiiv, `totalElements = 0` ja `totalPages = 0` (mitte viga).

`page < 0` või `size < 1` valideerimine ei ole selle taski skoobis.

## Vastuvõtu kriteeriumid

- [ ] Endpoint `GET /api/trainings` on olemas ja nõuab parameetreid `requestUserId`, `areaId`, `sportId`, `trainerId`, `dateFrom`, `timeFrom`
- [ ] Õnnestunud vastus on 200 ja JSON objekt `TrainingGroupOverviewPageDto` väljadega `trainings`, `pageNumber`, `pageSize`, `totalElements`, `totalPages`
- [ ] Iga `training_date` kirje on eraldi rida (grupeerimist ei toimu)
- [ ] Filtri väärtus `0` jätab vastava filtri (`areaId`, `sportId`, `trainerId`) rakendamata; muul juhul filtreeritakse vastava veeru järgi
- [ ] `dateFrom` tagastab treeningud alates antud kuupäevast ja `timeFrom` iga päeva treeningud alates antud kellaajast
- [ ] `userIsRegistered` kajastab õigesti `user_training` tabeli sisu antud `requestUserId` kohta
- [ ] `userIsTrainingGroupMember` kajastab õigesti `user_training_group` tabeli sisu antud `requestUserId` kohta
- [ ] `requestUserId=0` korral on mõlemad booleanid kõigil ridadel `false`
- [ ] Read on sorteeritud `trainingDate`, `trainingTime` järgi kasvavalt
- [ ] `page` ja `size` puudumisel tagastatakse esimene lehekülg 7 reaga (või vähem, kui ridu on vähem)
- [ ] `size` määrab ridade arvu leheküljel ja `page` valib õige lehekülje
- [ ] `totalElements` ja `totalPages` arvestavad filtreid
- [ ] Viimasest leheküljest suurema `page` korral tagastatakse 200 ja tühi `trainings` massiiv
- [ ] Kui filtrile ei vasta ükski rida, tagastatakse 200, tühi `trainings` massiiv ja `totalElements = 0`
- [ ] Kirjutatud on automaattestid: mitme leheküljega vastus (nt `size=2`), tühi tulemus filtri korral, booleanide tõene/väär juhtum, `requestUserId=0`, lehekülg väljaspool vahemikku
