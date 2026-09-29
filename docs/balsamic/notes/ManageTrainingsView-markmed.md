# ManageTrainingsView.vue — Balsamiq märkmed

Vaade, kus treener haldab **enda** treeninggruppe ja treeninguid: näeb oma treeningute (training_date) nimekirja, saab treeningut muuta/kustutada, loob uue treeninggrupi, ja kinnitab/lükkab tagasi enda treeninggruppidega liitumise taotlused. Lähtefailid: `docs/balsamic/SportClub_Halda_treeninggruppe.pdf` (põhivaade) ja `docs/balsamic/SportClub_Loo_treeninggrupp.pdf` (uue treeninggrupi loomise modaal, avaneb samalt lehelt). Struktuur vastab failile `balsamiq-markmete-struktuur.md`.

**Lahtised otsad (väikesed, ei blokeeri edasist tööd — vaata üle enne implementeerimist):**
- Mockup kasutab illustratiivseid näidisväärtusi "Alta Tenniseklubi" / "Algajate Grupp 1", millele `docs/database/3_import.sql` täpset vastet ei ole. JSON näidistes on allpool kasutatud reaalset seemet, mis struktuurilt (üks treeninggrupp, kolm eri kuupäeva/täituvusega training_date rida) mockup'iga kõige paremini sobib: treener Jaana Kask (user_id 5), Beeta Tenniseklubi (sportclub_id 1), treeninggrupp "Tennis - Algtase" (training_group_id 1).
- Eeldatud roll on Trainer, kes näeb ja haldab ainult neid treeninggruppe, mille `training_group.user_id` on tema enda kasutaja id. Kas Admin peaks samale vaatele pääsema (nt kõigi treenerite andmetega), vajab täpsustust — mockup näitab ainult ühe treeneri vaadet.
- "Loo uus Treening" nupp viib tõenäoliselt eraldi vaatele/vormile (uue treeningu, mitte treeninggrupi, loomiseks) — see ei kuulu selle mockup lehe API kutsete alla ja vajab oma eraldi taski/märget, kui vastav mockup lisandub.
- "i" (info) ikoon avab tõenäoliselt lihtsalt read-only infomodali sama rea andmetega, ilma täiendava API kutseta (andmed on juba tabeli reas olemas) — seetõttu pole sellele eraldi API märget lisatud.
- Kasutaja andmebaasi `join_application` tabelis pole veergu taotluse esitamise kuupäeva jaoks — "Taotluse Kuupäev" väli on kasutaja otsusel API märgetest **välja jäetud** (vt allpool GET liitumistaotluste kutse). Enne reaalset implementeerimist tuleb otsustada, kas ja kuidas seda kuvada.
- `join_application` tabelis pole hetkel `3_import.sql`-s ühtegi näidiskirjet (vt ka `docs/tasks/backend/Treeninggrupiga-liitumise-taotlemine.md`) — liitumistaotluste API näidis allpool on seetõttu illustratiivne (kasutab mockup'i enda nimesid Jaanus Riiner / Toomas Vara), mitte reaalsest seemest pärit.
- `Status.java` enumis on hetkel ainult `STATUS_ACTIVE("A")` / `STATUS_DELETED("D")`. Liitumistaotluste voog vajab täiendust väärtustega ootel/kinnitatud/tagasi lükatud (nt `PEN`/`ACC`/`REJ`) — `PEN` on juba kavandatud failis `Treeninggrupiga-liitumise-taotlemine-IMPLEMENTATSIOON.md`, `ACC`/`REJ` on uued, kavandatud käesolevas märkmes.
- Treeningu kustutamise kohta: mockup ei erista, kas tohib kustutada treeningut, millel on juba registreerunud kasutajaid (`user_training` kirjed). Praegu on eeldatud, et kustutamine on alati lubatud (kaskaadis kustuvad ka vastavad `user_training` kirjed) — vajab kinnitust.
- "Loo uus treeninggrupp" modaali "Skill-level" näidisväärtus "Nõrgem kesktase" ei vasta ühelegi `skill_level.name` väärtusele `3_import.sql`-s (reaalsed väärtused Tennis'e jaoks on "Algtase" / "Kesktase" / "Edasijõudnud"). API näidises allpool on kasutatud lähimat reaalset vastet "Kesktase" — treeneri valikuvõimalused sõltuvad ikkagi täielikult sellest, mis on andmebaasis `skill_level` tabelis olemas.
- Modaali "Spordiklubi" dropdown peab näitama treeneri **kõiki** endaga seotud spordiklubisid (`sportclub_trainer` tabeli kaudu), mitte ainult neid, kus tal juba on treeninggrupp (erinevalt ülal kirjeldatud filter-dropdownist `GET /api/trainers/{trainerId}/training-groups`) — vastasel juhul ei saaks treener, kellel pole veel ühtegi gruppi, oma esimest gruppi kunagi luua. Seetõttu on allpool defineeritud eraldi kutse `GET /api/trainers/{trainerId}/sportclubs`.
- "Spordiala" dropdown kasutab juba olemasolevat, implementeeritud teenust `GET /api/sports` (vt `backend/src/main/java/ee/sportclub/controller/sport/SportController.java`, `SportDto.java` väljadega `sportId`/`sportName`) — sellele ei looda uut task'i, kuna see on juba valmis.

---

## Vaate märkmed

```text
Roll: Trainer (treener näeb ja haldab ainult endale kuuluvaid treeninggruppe)
Failinimi: ManageTrainingsView.vue
Frontend rada: /manage-trainings

Vaatega seotud lisainfo:
Menüü link "Halda treeninguid" on nähtav ainult Trainer rollile.

Ülemine tabel kuvab sisse logitud treeneri treeningute (training_date) read, iga toimumiskord (training_date) eraldi reana. "Sportklubi" ja "Treeninggrupp" dropdown-filtrid täidetakse kutsega GET /api/trainers/{trainerId}/training-groups; valikus "Kõik" vastab väärtusele 0 (ei filtreerita). Tabeli sisu laetakse kutsega GET /api/trainers/{trainerId}/training-dates.

Iga rea "pliiats" (muuda) ikoon avab modaalakna "Muuda treeningut" (eeltäidetud kirjeldus, max osalejate arv ja kuupäev/kellaaeg), mille "Kinnitan" nupp saadab PUT /api/training-dates/{trainingDateId}. Iga rea "prügikast" (kustuta) ikoon avab kinnitusmodaali "Kinnitan kustutamise", mille "Kinnitan treeningu kustutamise" nupp saadab DELETE /api/training-dates/{trainingDateId}. Mõlema modaali "Sulge" nupp sulgeb akna ilma API kutseta. "i" (info) ikoon avab read-only infomodali, kasutades juba laetud rea andmeid, ilma täiendava API kutseta.

Nupp "Loo uus Treeninggrupp" avab samal lehel modaalakna "Loo uus treeninggrupp" väljadega Spordiklubi (dropdown, täidetakse kutsega GET /api/trainers/{trainerId}/sportclubs), Spordiala (dropdown, täidetakse kutsega GET /api/sports), Skill-level (dropdown, täidetakse valitud spordiala järgi kutsega GET /api/sports/{sportId}/skill-levels — Spordiala valiku muutumisel laetakse Skill-level nimekiri uuesti), Treeninggrupi nimi (tekstiväli) ja Kirjeldus (tekstiala, valikuline). Nupp "Loo" saadab POST /api/training-groups ja sulgeb seejärel modaali, laadides uuesti "Sportklubi"/"Treeninggrupp" filter-dropdownid ja tabeli; nupp "Sulge" sulgeb modaali ilma API kutseta. Nupp "Loo uus Treening" suunab uue treeningu loomise vaatele/vormile (väljaspool selle vaate API kutseid, vt Lahtised otsad).

Alumine sektsioon "Treeninggruppide liitumistaotlused" kuvab sisse logitud treeneri treeninggruppidele esitatud ootel liitumistaotlused, laetuna kutsega GET /api/trainers/{trainerId}/join-applications. Iga rea "linnuke" nupp kinnitab taotluse (PUT /api/join-applications/{joinApplicationId}/confirm), "rist" nupp lükkab taotluse tagasi (PUT /api/join-applications/{joinApplicationId}/reject) — mockup ei näita nende juures eraldi kinnitusmodaali, tegevus toimub kohe nupule vajutades. Pärast kinnitamist/tagasilükkamist eemaldub rida nimekirjast (ja kinnitamise korral lisandub kasutaja `user_training_group` liikmeks).
```

---

## API märkmed — GET /api/trainers/{trainerId}/training-groups

```text
API: GET /api/trainers/{trainerId}/training-groups

TrainerTrainingGroupDto.java
Response (200):
[
  {
    "trainingGroupId": 1,
    "trainingGroupName": "Tennis - Algtase",
    "sportclubId": 1,
    "sportclubName": "Beeta Tenniseklubi"
  }
]

API teenuse lisainfo:
Tagastab treeninggrupid, mille training_group.user_id võrdub trainerId-ga — kasutatakse vaate Sportklubi ja Treeninggrupp filter-dropdownide täitmiseks (Sportklubi nimekiri tuletatakse frontendis siit saadud ridade distinct sportclubId/sportclubName väärtustest).

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'trainerId' väärtusega: 99"
```

---

## API märkmed — GET /api/trainers/{trainerId}/training-dates

```text
API: GET /api/trainers/{trainerId}/training-dates

ManageTrainingDateDto.java
Response (200):
[
  {
    "trainingDateId": 1,
    "trainingGroupId": 1,
    "trainingGroupName": "Tennis - Algtase",
    "sportclubId": 1,
    "sportclubName": "Beeta Tenniseklubi",
    "facilityId": 1,
    "facilityName": "Laagri Tennisekeskus",
    "description": "Kõvakattega siseväljak",
    "trainingDate": "2026-10-20",
    "trainingTime": "19:30",
    "userCount": 2,
    "maxSize": 4
  },
  {
    "trainingDateId": 2,
    "trainingGroupId": 1,
    "trainingGroupName": "Tennis - Algtase",
    "sportclubId": 1,
    "sportclubName": "Beeta Tenniseklubi",
    "facilityId": 1,
    "facilityName": "Laagri Tennisekeskus",
    "description": "Kõvakattega siseväljak",
    "trainingDate": "2026-10-21",
    "trainingTime": "19:30",
    "userCount": 3,
    "maxSize": 4
  }
]

API teenuse lisainfo:
Kohustuslikud query parameetrid sportclubId ja trainingGroupId — väärtus 0 tähendab, et selle järgi ei filtreerita (sama muster mis GET /api/trainings juures). description väli pärineb training (mitte training_date) kirjelt ja on kõigil sama training_group_id.training_id training_date ridadel ühesugune — vajalik "Muuda treeningut" modaali eeltäitmiseks.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'trainerId' väärtusega: 99"
```

---

## API märkmed — PUT /api/training-dates/{trainingDateId}

```text
API: PUT /api/training-dates/{trainingDateId}

UpdateTrainingDateRequestDto.java
Request body:
{
  "description": "Kõvakattega siseväljak, kaasa oma reket",
  "maxSize": 4,
  "trainingDate": "2026-10-20",
  "trainingTime": "19:30"
}

Response (200): NONE

API teenuse lisainfo:
description kirjutatakse training_date.training_id kaudu leitud training kirje description väljale (mõjutab kõiki sama training kõrvalisi training_date ridu), maxSize/trainingDate/trainingTime uuendavad ainult antud training_date rida (max_size, start_date, start_time).

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'trainingDateId' väärtusega: 999"

HTTP: 403
errorCode: MAX_SIZE_TOO_LOW
message: "Maksimaalne osalejate arv ei tohi olla väiksem juba registreerunud kasutajate arvust"
```

---

## API märkmed — DELETE /api/training-dates/{trainingDateId}

```text
API: DELETE /api/training-dates/{trainingDateId}

Response (200): NONE

API teenuse lisainfo:
Kustutab training_date kirje. Vajalik täpsustus, kas see peaks olema keelatud, kui training_date.user_count > 0 (juba registreerunud kasutajatega) — vt Vaate märkmete lahtiste otste loetelu.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'trainingDateId' väärtusega: 999"
```

---

## API märkmed — GET /api/trainers/{trainerId}/join-applications

```text
API: GET /api/trainers/{trainerId}/join-applications

PendingJoinApplicationDto.java
Response (200):
[
  {
    "joinApplicationId": 1,
    "userId": 10,
    "userFullName": "Jaanus Riiner",
    "sportclubId": 1,
    "sportclubName": "Beeta Tenniseklubi",
    "trainingGroupId": 1,
    "trainingGroupName": "Tennis - Algtase",
    "trainingGroupMemberCount": 5
  },
  {
    "joinApplicationId": 2,
    "userId": 11,
    "userFullName": "Toomas Vara",
    "sportclubId": 1,
    "sportclubName": "Beeta Tenniseklubi",
    "trainingGroupId": 1,
    "trainingGroupName": "Tennis - Algtase",
    "trainingGroupMemberCount": 4
  }
]

API teenuse lisainfo:
Tagastab ainult status = 'PEN' kirjed nende treeninggruppide kohta, mille training_group.user_id on trainerId. trainingGroupMemberCount on training_group liikmete koguarv (COUNT user_training_group kirjeid antud training_group_id kohta) — mitte konkreetse training_date user_count/max_size, seega võib olla suurem kui ühegi üksiku treeningkorra max_size (grupp ei pea kõiki liikmeid korraga ühele treeningule mahutama). Taotluse esitamise kuupäev jääb hetkel kuvamata, kuna join_application tabelis pole selleks veergu (vt Vaate märkmete lahtiste otste loetelu).

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'trainerId' väärtusega: 99"
```

---

## API märkmed — PUT /api/join-applications/{joinApplicationId}/confirm

```text
API: PUT /api/join-applications/{joinApplicationId}/confirm

Response (200): NONE

API teenuse lisainfo:
Seab join_application.status väärtuseks 'ACC' ja lisab uue user_training_group kirje (user_id + training_group_id vastavalt taotluselt). Status.java enumi tuleb lisada STATUS_ACCEPTED("ACC").

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'joinApplicationId' väärtusega: 999"

HTTP: 403
errorCode: JOIN_APPLICATION_ALREADY_PROCESSED
message: "Taotlus on juba menetletud"
```

---

## API märkmed — PUT /api/join-applications/{joinApplicationId}/reject

```text
API: PUT /api/join-applications/{joinApplicationId}/reject

Response (200): NONE

API teenuse lisainfo:
Seab join_application.status väärtuseks 'REJ', user_training_group kirjet ei looda. Status.java enumi tuleb lisada STATUS_REJECTED("REJ").

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'joinApplicationId' väärtusega: 999"

HTTP: 403
errorCode: JOIN_APPLICATION_ALREADY_PROCESSED
message: "Taotlus on juba menetletud"
```

---

## API märkmed — GET /api/trainers/{trainerId}/sportclubs

```text
API: GET /api/trainers/{trainerId}/sportclubs

TrainerSportclubDto.java
Response (200):
[
  {
    "sportclubId": 2,
    "sportclubName": "Alta Tenniseklubi"
  }
]

API teenuse lisainfo:
Tagastab kõik spordiklubid, millega trainerId on seotud sportclub_trainer tabeli kaudu — erinevalt GET /api/trainers/{trainerId}/training-groups (vt eespool) ei eelda see, et treeneril juba on selles klubis treeninggrupp. Kasutatakse "Loo uus treeninggrupp" modaali Spordiklubi dropdowni täitmiseks.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'trainerId' väärtusega: 99"
```

---

## API märkmed — GET /api/sports/{sportId}/skill-levels

```text
API: GET /api/sports/{sportId}/skill-levels

SkillLevelDto.java
Response (200):
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

API teenuse lisainfo:
Tagastab kõik skill_level kirjed, mille sport_id võrdub sportId-ga. Kasutatakse "Loo uus treeninggrupp" modaali Skill-level dropdowni täitmiseks — laetakse uuesti iga kord, kui treener muudab Spordiala valikut.

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'sportId' väärtusega: 99"
```

---

## API märkmed — POST /api/training-groups

```text
API: POST /api/training-groups

TrainingGroupCreateRequestDto.java
Request body:
{
  "trainerId": 6,
  "sportclubId": 2,
  "sportId": 1,
  "skillLevelId": 2,
  "trainingGroupName": "Kesktase Grupp 2",
  "description": ""
}

Response (200): NONE

API teenuse lisainfo:
Loob uue training_group kirje (user_id = trainerId, ülejäänud väljad otse request body-st). description on valikuline (tühi string, kui kasutaja Kirjeldus välja ei täida). Pärast loomist laeb frontend "Sportklubi"/"Treeninggrupp" filter-dropdownid ja tabeli uuesti (vt Vaate märkmed).

Veateated:
HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'trainerId' väärtusega: 99"

HTTP: 403
errorCode: NOT_SPORTCLUB_TRAINER
message: "Treener ei ole selle spordiklubi treener"

HTTP: 403
errorCode: SKILL_LEVEL_SPORT_MISMATCH
message: "Valitud oskustase ei kuulu valitud spordiala alla"

HTTP: 400
errorCode: INCORRECT_INPUT
message: "trainingGroupName: ei tohi olla tühi"
```
