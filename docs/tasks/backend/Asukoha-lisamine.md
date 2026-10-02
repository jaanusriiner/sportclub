# Asukoha lisamine

**Teenus:** `POST /api/facilities`

**Vaste balsamic mockupis:** mockup puudub. Task põhineb kasutaja suulisel kirjeldusel ja taski koostamisel kokku lepitud otsustel. Vaate (Admini asukohtade haldus) mockup ja märkmed tuleb lisada, kui need valmivad.

## Sisend

Request body (`CreateFacilityRequestDto.java`):

```json
{
  "adminId": 1,
  "areaId": 1,
  "facilityName": "Kalevi Spordihall",
  "address": "Juhkentali 12, Tallinn",
  "description": "Sisehall, 2 korvpalliväljakut",
  "sportIds": [2, 3],
  "imageData": "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQ..."
}
```

> Näidis on illustratiivne, sest uut asukohta `3_import.sql`-is loomulikult veel pole. `adminId`, `areaId` ja `sportIds` väärtused vastavad reaalsetele seemneandmetele (vt "Näidisandmed" allpool).

| Väli | Tüüp | Kohustuslik | Kirjeldus |
|---|---|---|---|
| `adminId` | Integer | Jah | Sisse logitud kasutaja id. Backend kontrollib, et kasutaja roll on `admin`. |
| `areaId` | Integer | Jah | Maakond (`facility.area_id`). Dropdown täidetakse olemasoleva kutsega `GET /api/areas` (vt `AreaController.java`). |
| `facilityName` | String | Jah | Asukoha nimi (`facility.name`), max 255 märki, ei tohi olla tühi. Peab olema unikaalne (vt veaolukorrad). |
| `address` | String | Jah | Aadress (`facility.address`), max 255 märki, ei tohi olla tühi. |
| `description` | String | Ei | Kirjeldus (`facility.description`), max 255 märki. |
| `sportIds` | List&lt;Integer&gt; | Jah | Spordialad, mida asukohas saab harrastada. Iga id kohta luuakse üks `sport_facility` rida. Vähemalt üks spordiala on kohustuslik. Dropdown täidetakse olemasoleva kutsega `GET /api/sports`. |
| `imageData` | String | Ei | Asukoha pilt Base64 data URL-ina (nt `data:image/jpeg;base64,...`), nagu frontend selle `FileReader.readAsDataURL()`-iga loeb. Salvestatakse tabelisse `facility_image` (`data` veerg, `bytea`) projekti olemasoleva `StringBytesConverter.stringToBytes()` abil. Kui väli puudub, on `null` või tühi string, pildi kirjet ei looda. **Pildifaili maksimaalne suurus on 10 MB ja lubatud on ainult JPEG ja PNG** (vt "Pildi piirangud" allpool). |

### Pildi piirangud

#### Vorming: ainult JPEG ja PNG

`imageData` peab algama ühega järgmistest data URL eesliidetest:

| Lubatud | Näide |
|---|---|
| `data:image/jpeg;base64,` | `.jpg` ja `.jpeg` failid (brauser annab mõlemale MIME tüübi `image/jpeg`) |
| `data:image/png;base64,` | `.png` failid |

Muu eesliide (nt `data:image/webp;base64,`, `data:image/gif;base64,`, `data:application/pdf;base64,`) või ilma eesliiteta string tagastab **400 `INCORRECT_INPUT`**.

- Kontroll tehakse DTO valideerimisannotatsiooniga (`@Pattern`), sama mustriga nagu teiste vormingukontrollidega projektis.
- **Tühi string peab lubatud olema**, sest tühi `imageData` tähendab "pilti pole" (vt tabel ülal). `null`-i laseb `@Pattern` niikuinii läbi, aga `""` tuleb regexis eraldi lubada.
- Kontroll vaatab ainult **deklareeritud** tüüpi data URL-i eesliites, mitte faili tegelikku sisu. Kui keegi saadab käsitsi vale sisuga stringi õige eesliitega, see läbi läheb. Õpilasprojekti jaoks on see piisav. Päris rakenduses kontrollitaks ka faili "maagilisi baite" (JPEG: `FF D8 FF`, PNG: `89 50 4E 47`).
- Frontendis tasub faili valikut piirata juba `<input type="file" accept="image/jpeg,image/png">` atribuudiga.

#### Suurus: kuni 10 MB

Pildifail tohib olla kuni **10 MB**. Kuna Base64 kodeering suurendab andmemahtu umbes 4/3 korda, kontrollitakse backendis `imageData` **stringi pikkust**:

```
10 MB fail  = 10 × 1024 × 1024 baiti = 10 485 760 baiti
Base64 kujul ≈ 10 485 760 × 4/3      ≈ 13 981 014 märki
+ data URL eesliide (nt "data:image/jpeg;base64,") 
→ piirang: imageData max 14 000 000 märki
```

- Kontroll tehakse DTO valideerimisannotatsiooniga (`@Size(max = 14_000_000, message = "...")`). Ületamisel tagastatakse **400 `INCORRECT_INPUT`**, nagu teistel sisendivigadel.
- Projekti kasutatava Jackson 3 (Spring Boot 4) vaikimisi stringi pikkuse piirang (`StreamReadConstraints.DEFAULT_MAX_STRING_LEN` = 100 000 000 märki) on sellest suurem, seega 10 MB pilt jõuab valideerimiseni ilma lisaseadistuseta.
- Frontend peaks faili suurust kontrollima juba enne üleslaadimist (`file.size > 10 * 1024 * 1024`), et kasutaja ei peaks suurt faili asjata üles laadima. Backendi kontroll on siiski kohustuslik.

## Väljund

**Response (200 OK):** `NONE`, teenus ei tagasta body-d.

## Eesmärk

Admin saab lisada süsteemi uue treeningu toimumiskoha (spordihall, staadion, väljak) ja märkida, milliseid spordialasid seal harrastada saab. Asukohad on vajalikud treeningute loomisel (`POST /api/trainings`, vt `Treeningu-lisamine.md`), kus treener valib asukoha dropdownist. Spordialade sidumine (`sport_facility`) võimaldab hiljem pakkuda treenerile ainult neid asukohti, mis sobivad treeninggrupi spordialaga. See oli taskis `Treeningu-lisamine.md` lahtise otsana kirjas.

Teenuse loogika:

1. Kontrolli, et `adminId` viitab olemasolevale kasutajale ja tema roll on `admin` (`role.name = 'admin'`, vt `UserRole.ADMIN`). Kui ei ole, tagasta viga `NOT_ADMIN`.
2. Kontrolli, et `areaId` viitab olemasolevale maakonnale.
3. Kontrolli, et `sportIds` pole tühi (olemasolev viga `SPORT_MISSING`, sama nagu kasutaja registreerimisel) ja iga id viitab olemasolevale spordialale.
4. Kontrolli, et sama nimega asukohta pole juba olemas (võrdlus tõstutundetu, nt "kalevi spordihall" = "Kalevi Spordihall"). Kui on, tagasta viga `FACILITY_NAME_UNAVAILABLE`.
5. Loo uus `facility` kirje (`area_id`, `name`, `address`, `description`).
6. Loo iga `sportIds` elemendi kohta üks `sport_facility` kirje (`sport_id`, `facility_id` = äsja loodud asukoha id).
7. Kui `imageData` on olemas ja pole tühi, loo üks `facility_image` kirje (`facility_id` = äsja loodud asukoha id, `data` = `imageData` baitidena). Kui pilti pole, jäetakse see samm vahele.
8. Kogu teenus peab olema ühes transaktsioonis (`@Transactional`), et vea korral ei jääks andmebaasi asukohta ilma spordialade või pildita.

> **Pane tähele:** `RegisterService.getValidSports` kasutab `sportRepository.findAllById(...)`, mis jätab olematud id-d **vaikselt vahele**. Selles taskis peab olematu `sportId` andma 404 vea. Kontrolli seda nt tagastatud listi pikkust `sportIds` pikkusega võrreldes.

## Seotud andmebaasi tabelid

Vt `docs/database/2_create.sql`.

### `facility`

```sql
CREATE TABLE facility (
    id serial  NOT NULL,
    area_id int  NOT NULL,
    name varchar(255)  NOT NULL,
    address varchar(255)  NOT NULL,
    description varchar(255)  NULL,
    CONSTRAINT facility_pk PRIMARY KEY (id)
);
```

Siia luuakse uus kirje. `name` veerul pole andmebaasis unikaalsuspiirangut, seega kontroll tehakse teenuse kihis.

### `sport_facility`

```sql
CREATE TABLE sport_facility (
    id serial  NOT NULL,
    sport_Id int  NOT NULL,
    facility_id int  NOT NULL,
    CONSTRAINT sport_facility_pk PRIMARY KEY (id)
);
```

Liitetabel spordiala ja asukoha vahel. Siia luuakse iga valitud spordiala kohta üks kirje. `3_import.sql`-is selles tabelis hetkel ühtegi kirjet pole.

> Backendis pole veel `SportFacility` entity't ega repositooriumi. Need tuleb luua paketti `persistence/facility/` (nt JPA Buddy abil).

### `facility_image` (UUS TABEL, skeemi muudatus)

Seda tabelit `2_create.sql`-is **veel pole**. Enne implementeerimist tuleb see lisada andmemudelisse (Redgate Data Modeler) ja `2_create.sql` faili:

```sql
-- Table: facility_image
CREATE TABLE facility_image (
    id serial  NOT NULL,
    facility_id int  NOT NULL,
    data bytea  NOT NULL,
    CONSTRAINT facility_image_pk PRIMARY KEY (id)
);

-- Reference: facility_image_facility (table: facility_image)
ALTER TABLE facility_image ADD CONSTRAINT facility_image_facility
    FOREIGN KEY (facility_id)
        REFERENCES facility (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;
```

Pilt on eraldi tabelis, et asukohtade nimekirja päringud (dropdownid, treeningute nimekiri) ei peaks iga kord suurt `bytea` välja kaasa laadima. Struktuur võimaldab hiljem lisada ühele asukohale ka mitu pilti. Selles taskis luuakse aga maksimaalselt üks pilt asukoha kohta.

> Backendis tuleb luua ka `FacilityImage` entity (väli `data` tüübiga `byte[]`) ja repositoorium paketti `persistence/facility/`.

### `area`, `sport`

Kasutatakse `areaId` ja `sportIds` olemasolu kontrollimiseks.

### `user`, `role`

Kasutatakse `adminId` olemasolu ja rolli kontrollimiseks (`user.role_id` → `role.name`).

### Näidisandmed (`3_import.sql`)

| Tabel | Kirjed |
|---|---|
| `role` | 1 = `admin`, 2 = `trainer`, 3 = `customer` |
| `user` | id 1 = `admin@admin.ee` (role 1), id 2 = `trainer@trainer.ee` (role 2), id 3 = `customer@customer.ee` (role 3) |
| `area` | 1 = Harjumaa, 2 = Läänemaa, 3 = Saaremaa, 4 = Hiiumaa, 5 = Pärnumaa |
| `sport` | 1 = Tennis, 2 = Jalgpall, 3 = Korvpall, 4 = Golf |
| `facility` | 1 = Laagri Tennisekeskus, 2 = Pärnu Tennise- ja Padelikeskus, 3 = Tallink Tennisekeskus, 4 = Hiiu Staadion, 5 = Niitvälja Golf |

Testimiseks:
- `adminId = 1`: lubatud
- `adminId = 2` (treener) või `3` (klient): `NOT_ADMIN`
- `facilityName = "Laagri Tennisekeskus"` (ka `"laagri tennisekeskus"`): `FACILITY_NAME_UNAVAILABLE`

Teenus **ei puuduta** tabeleid `training`, `training_date` ega `profile`.

`facility_image` tabelis pole (uue tabelina) näidisandmeid.

## Veaolukorrad

| Olukord | Status code | Response body |
|---|---|---|
| `adminId` väärtusega kasutajat ei leitud | 404 Not Found | `{"errorCode": "PRIMARY_KEY_NOT_FOUND", "message": "Ei leidnud primary keyd 'adminId' väärtusega: 99"}` |
| `areaId` väärtusega maakonda ei leitud | 404 Not Found | `{"errorCode": "PRIMARY_KEY_NOT_FOUND", "message": "Ei leidnud primary keyd 'areaId' väärtusega: 99"}` |
| Mõni `sportIds` väärtus ei viita olemasolevale spordialale | 404 Not Found | `{"errorCode": "PRIMARY_KEY_NOT_FOUND", "message": "Ei leidnud primary keyd 'sportId' väärtusega: 99"}` |
| Kasutaja roll ei ole `admin` | 403 Forbidden | `{"errorCode": "NOT_ADMIN", "message": "Selle toimingu jaoks on vaja administraatori õigusi"}` |
| `sportIds` on tühi list | 403 Forbidden | `{"errorCode": "SPORT_MISSING", "message": "sportIds: Vali vähemalt üks spordiala"}` |
| Sama nimega asukoht on juba olemas | 403 Forbidden | `{"errorCode": "FACILITY_NAME_UNAVAILABLE", "message": "Sellise nimega asukoht on juba olemas"}` |
| `facilityName` või `address` on tühi | 400 Bad Request | `{"errorCode": "INCORRECT_INPUT", "message": "facilityName: ei tohi olla tühi"}` |
| Kohustuslik väli puudub (`adminId`, `areaId`, `sportIds`) | 400 Bad Request | `{"errorCode": "INCORRECT_INPUT", "message": "<väli>: ei tohi olla tühi"}` |
| `imageData` ei ole JPEG ega PNG (vale või puuduv data URL eesliide) | 400 Bad Request | `{"errorCode": "INCORRECT_INPUT", "message": "imageData: lubatud on ainult JPEG ja PNG pildid"}` |
| `imageData` on pikem kui 14 000 000 märki (pilt > 10 MB) | 400 Bad Request | `{"errorCode": "INCORRECT_INPUT", "message": "imageData: pildi maksimaalne suurus on 10 MB"}` |
| Mõni tekstiväli on pikem kui 255 märki | 400 Bad Request | `{"errorCode": "INCORRECT_INPUT", "message": "<väli>: size must be between 0 and 255"}` |
| Ootamatu serveri viga | 500 Internal Server Error | — |

`NOT_ADMIN` ja `FACILITY_NAME_UNAVAILABLE` on uued väärtused, mis tuleb lisada `Error.java` enumisse. `SPORT_MISSING` on juba olemas.

## Lahtised otsad

- **Mockup puudub:** Admini asukohtade halduse vaade (kus nupp "Lisa asukoht" asub, kuidas vorm välja näeb) tuleb kirjeldada eraldi frontendi taskis või Balsamiqi märkmetes.
- **Autentimine:** projektis pole veel päris autentimist, seega `adminId` tuleb request body-s, sama mustriga nagu `trainerId` treeningu lisamisel. Kui projekti lisandub sessioon/JWT, tuleb rollikontroll sinna üle viia.
- **Skeemi muudatus:** `facility_image` tabel tuleb lisada andmemudelisse ja `2_create.sql`-i. Kuna andmebaas on kogu meeskonnale ühine, tuleb sellest meeskonda teavitada, et kõik jooksutaksid skriptid uuesti.
- **Tühikud nimes:** taskis ei nõuta nime `trim()`-imist enne unikaalsuse kontrolli. Kui `"Kalevi Spordihall "` (tühikuga lõpus) ei tohiks olla erinev nimi, vajab see täpsustust.

## Vastuvõtu kriteeriumid

- [ ] Endpoint `POST /api/facilities` on olemas ja võtab vastu `CreateFacilityRequestDto` JSON-i
- [ ] Õnnestunud loomisel tagastatakse 200 ilma response body-ta
- [ ] Andmebaasi luuakse üks `facility` kirje õigete väljadega ja iga `sportIds` elemendi kohta üks `sport_facility` kirje, mis viitab uuele asukohale
- [ ] Kui `imageData` on antud, luuakse üks `facility_image` kirje, mille `data` sisaldab saadetud stringi baitidena. Kui `imageData` puudub või on tühi, `facility_image` kirjet ei looda ja asukoht luuakse ikkagi edukalt.
- [ ] `imageData`, mis on pikem kui 14 000 000 märki (pilt > 10 MB), tagastab 400 ja errorCode `INCORRECT_INPUT`, ühtegi kirjet ei looda. Kuni 10 MB pilt salvestatakse edukalt.
- [ ] `imageData`, mis algab `data:image/jpeg;base64,` või `data:image/png;base64,`, salvestatakse edukalt. Muu eesliitega (nt webp, gif, pdf) või eesliiteta string tagastab 400 ja errorCode `INCORRECT_INPUT`, ühtegi kirjet ei looda. Tühi string on lubatud ja tähendab "pilti pole".
- [ ] `facility_image` tabel on lisatud `2_create.sql`-i koos foreign key'ga `facility` tabelile
- [ ] Kui kasutaja roll pole `admin`, tagastatakse 403 ja errorCode `NOT_ADMIN`, ühtegi kirjet ei looda
- [ ] Kui sama nimega asukoht on olemas (tõstutundetult), tagastatakse 403 ja errorCode `FACILITY_NAME_UNAVAILABLE`, ühtegi kirjet ei looda
- [ ] Tühi `sportIds` tagastab 403 ja errorCode `SPORT_MISSING`
- [ ] Olematu `adminId`/`areaId`/`sportId` tagastab 404 ja `PRIMARY_KEY_NOT_FOUND` sõnumiga, milles on õige välja nimi. Olematu `sportId` ei tohi jääda vaikselt vahele.
- [ ] Vigane sisend (tühi nimi/aadress, puuduv kohustuslik väli, liiga pikk tekst) tagastab 400 ja errorCode `INCORRECT_INPUT`
- [ ] Vea korral ei jää andmebaasi `facility` kirjet ilma `sport_facility` kirjeteta (transaktsioon)
- [ ] Kirjutatud on automaattestid: edukas loomine (sh `sport_facility` ridade arv), loomine pildiga ja ilma pildita, liiga suur pilt, PNG pilt, keelatud vorming (nt webp), mitte-admin kasutaja, korduv nimi (ka erineva tõstuga), tühi `sportIds`, olematu `sportId`, iga valideerimisviga ja iga olematu FK-välja juhtum
