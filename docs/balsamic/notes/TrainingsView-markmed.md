# TrainingsView.vue — Balsamiq märkmed

Vaade, kuhu kasutaja maandub pärast edukat sisselogimist ja/või kasutajaks registreerumist. Struktuur vastab failile `balsamiq-markmete-struktuur.md`.

JSON näidiste ID-d ja väärtused vastavad `docs/database/3_import.sql` faili lisatud kannetele (treenerid, profile, facility, sportclub, skill_level, training_group, training, training_date, user_training_group, user_training) — vt sealt täpsed INSERT read.

---

## Vaate märkmed

```text
Roll: Kõik sisse logitud rollid (Admin / Trainer / Customer)
Failinimi: TrainingsView.vue
Frontend rada: /trainings

Vaatega seotud lisainfo:
Kui kasutaja saabub vaatele vahetult pärast edukat registreerumist, kuvatakse roheline õnnestumisteade "Oled edukalt Kasutajaks registreerunud !!" (AlertSuccess.vue vms).

Sektsioon "Minu Grupid ja Treeningud" näitab kasutaja enda tulevasi registreeritud treeninguid (GET /api/users/{userId}/trainings). Kui nimekiri on tühi, kuvatakse tekst "Hetkel pole ühelegi treeningule registreeritud."

Sektsioon "Treeningud" on filtreeritav nimekiri kõigist treeninggruppidest (GET /api/trainings) koos filtritega Piirkond, Spordiala, Treener, Kuupäev ja Kellaaeg (kõik valikulised, AND-loogikaga kombineeritavad).

Tabeli viimases veerus kuvatava nupu/teksti valik sõltub kasutaja liikmelisusest treeninggrupis ja järgmise treeningu täituvusest:
- Kui kasutaja EI OLE treeninggrupi liige (isTrainingGroupMember: false) -> kuvatakse nupp "Taotle Liitumist", Täituvus veergu ei näidata.
- Kui kasutaja ON liige JA järgmisel treeningul on vabu kohti (userCount < maxSize) -> kuvatakse Täituvus (nt "2/4") ja nupp "Registreeru".
- Kui kasutaja ON liige JA järgmine treening on täis (userCount >= maxSize) -> kuvatakse Täituvus (nt "4/4") ja tekst "Kohad on täis" nupu asemel.

Nupule "Registreeru" vajutades avaneb modaalaken "Treeningule registreerimine" (treeningu info + nupud "Registreeru" ja "Sulge"); "Registreeru" saadab POST /api/training-dates/{trainingDateId}/register.

Nupule "Taotle Liitumist" vajutades avaneb modaalaken "Treeninggrupiga liitumise taotlemine" (treeninggrupi info + nupud "Taotle" ja "Sulge"); "Taotle" saadab POST /api/training-groups/{trainingGroupId}/join-applications.

Mockupil on autori märkusena lisatud, et see tabel on hetkel ajutine/WIP kujundus — osa infost (konkreetse treeningu detailid, treeneri poolt muudetavad väljad) plaanitakse hiljem eraldi treeningu-vaatesse, mille peale terve rida või mõni link muutub klõpsitavaks. Praegused märkmed katavad ainult praeguse mockup'i kujul nähtavat funktsionaalsust.
```

---

## API märkmed — GET /api/users/{userId}/trainings

```text
API: GET /api/users/{userId}/trainings

MyTrainingDto.java
Response (200):
[
  {
    "trainingDateId": 1,
    "trainingGroupId": 1,
    "sportName": "Tennis",
    "facilityName": "Laagri Tennisekeskus",
    "trainerName": "Jaana Kask",
    "nextTrainingDate": "2026-09-20",
    "nextTrainingTime": "19:30",
    "userCount": 2,
    "maxSize": 4
  }
]

API teenuse lisainfo:
Tagastab treeningud, millele kasutaja on juba registreerunud (user_training) ja mille toimumisaeg pole veel möödas. Kui kasutajal pole ühtegi registreeringut, tagastatakse tühi list — frontend kuvab siis teksti "Hetkel pole ühelegi treeningule registreeritud."

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'userId' väärtusega: 99"
```

---

## API märkmed — GET /api/trainings

```text
API: GET /api/trainings

TrainingGroupOverviewDto.java
Response (200):
[
  {
    "trainingGroupId": 1,
    "sportId": 1,
    "sportName": "Tennis",
    "facilityId": 1,
    "facilityName": "Laagri Tennisekeskus",
    "trainerId": 5,
    "trainerName": "Jaana Kask",
    "sportclubId": 1,
    "sportclubName": "Beeta Tenniseklubi",
    "skillLevelId": 1,
    "skillLevelName": "Algtase",
    "trainingDateId": 1,
    "nextTrainingDate": "2026-09-20",
    "nextTrainingTime": "19:30",
    "userCount": 2,
    "maxSize": 4,
    "isTrainingGroupMember": true
  },
  {
    "trainingGroupId": 2,
    "sportId": 1,
    "sportName": "Tennis",
    "facilityId": 2,
    "facilityName": "Pärnu Tennise- ja Padelikeskus",
    "trainerId": 6,
    "trainerName": "Jaanus Tubli",
    "sportclubId": 2,
    "sportclubName": "Alta Tenniseklubi",
    "skillLevelId": 2,
    "skillLevelName": "Kesktase",
    "trainingDateId": 2,
    "nextTrainingDate": "2026-09-19",
    "nextTrainingTime": "18:30",
    "userCount": 4,
    "maxSize": 4,
    "isTrainingGroupMember": true
  },
  {
    "trainingGroupId": 5,
    "sportId": 4,
    "sportName": "Golf",
    "facilityId": 5,
    "facilityName": "Niitvälja Golf",
    "trainerId": 9,
    "trainerName": "Reena Sibul",
    "sportclubId": 5,
    "sportclubName": "Tore Golfklubi",
    "skillLevelId": 5,
    "skillLevelName": "Edasijõudnud",
    "trainingDateId": 5,
    "nextTrainingDate": "2026-09-20",
    "nextTrainingTime": "14:00",
    "userCount": 0,
    "maxSize": 12,
    "isTrainingGroupMember": false
  }
]

API teenuse lisainfo:
Valikulised query parameetrid filtreerimiseks: areaId (facility.area_id järgi), sportId, trainerId (training_group.user_id järgi), date (toimumiskuupäev), time (toimumiskellaaeg). Lisaks kohustuslik query parameeter userId, mille alusel arvutatakse iga rea isTrainingGroupMember väärtus (user_training_group olemasolu järgi). userCount ja maxSize pärinevad treeninggrupi järgmise toimuva training_date kirje väljadelt user_count/max_size; kui isTrainingGroupMember on false, neid väärtusi tabelis kasutajale ei näidata (vt Vaate märkmed).

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'userId' väärtusega: 99"
```

---

## API märkmed — POST /api/training-dates/{trainingDateId}/register

```text
API: POST /api/training-dates/{trainingDateId}/register

TrainingDateRegisterRequestDto.java
Request body:
{
  "userId": 3
}

TrainingRegisterResponseDto.java
Response (200):
{
  "message": "Oled edukalt treeningule registreerinud"
}

API teenuse lisainfo:
Lisab user_training kirje (user_id + training_date_id) ja kasvatab training_date.user_count väärtust ühe võrra. Eeldab, et kasutaja on juba training_group liige (vt POST .../join-applications).

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'trainingDateId' väärtusega: 999"

HTTP: 403
errorCode: NOT_TRAINING_GROUP_MEMBER
message: "Registreerumiseks pead olema treeninggrupi liige"

HTTP: 403
errorCode: TRAINING_FULL
message: "Sellel treeningul pole enam vabu kohti"

HTTP: 403
errorCode: ALREADY_REGISTERED
message: "Oled juba sellele treeningule registreerunud"
```

---

## API märkmed — POST /api/training-groups/{trainingGroupId}/join-applications

```text
API: POST /api/training-groups/{trainingGroupId}/join-applications

JoinApplicationRequestDto.java
Request body:
{
  "userId": 3
}

JoinApplicationResponseDto.java
Response (200):
{
  "message": "Taotlus esitatud"
}

API teenuse lisainfo:
Lisab join_application kirje staatusega 'PEN' (ootel). Treeneri/admini kinnitamise vooga liikmeks (user_training_group) saamine ei kuulu selle kutse alla.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'trainingGroupId' väärtusega: 999"

HTTP: 403
errorCode: JOIN_APPLICATION_UNAVAILABLE
message: "Oled selle treeninggrupiga juba liitunud või taotlus on juba esitatud"
```
