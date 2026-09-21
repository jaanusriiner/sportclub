# Uue kasutaja registreerimine

**Teenus:** `POST /api/register`

**Vaste balsamic mockupis:** "Registreeri kasutajaks" vaade (`RegisterView.vue`, route `/register`), lehekülg 1/1 (vt lisatud pilt `Uue-kasutaja-registreerimine.png`)

![Mockup](./Uue-kasutaja-registreerimine.png)

## Sisend

Path variable'id ja query parameetrid puuduvad. Sisendiks on request body (`RegisterRequestDto.java`):

| Väli | Tüüp | Kirjeldus |
|---|---|---|
| `firstName` | String | Eesnimi (`profile.first_name`) |
| `lastName` | String | Perenimi (`profile.last_name`) |
| `areaId` | Integer | Piirkonna ID (`area.id`), vormil rippmenüü "Piirkond" |
| `phoneNumber` | Integer | Kontakttelefon (`profile.phone_number`, andmebaasis `int`) |
| `email` | String | E-post, kasutajanimena (`user.email`) |
| `password` | String | Salasõna (`user.password`) |
| `sportIds` | List&lt;Integer&gt; | Valitud spordialade ID-d (`sport.id`), vormil mitmikvalikuga rippmenüü "Spordiala huvid". Kohustuslik, peab sisaldama vähemalt ühte spordiala (tühi massiiv või `null` ei kõlba). |

```json
{
  "firstName": "Mari",
  "lastName": "Maasikas",
  "areaId": 1,
  "phoneNumber": 55512345,
  "email": "mari.maasikas@gmail.com",
  "password": "parool123",
  "sportIds": [1, 3]
}
```

Salasõna kordust ("korda salasõna") ja "Nõustun Tingimustega" märkeruudu olekut kontrollitakse ainult frontendis — backendile neid ei saadeta.

## Väljund

**Response (200 OK):** loodud kasutaja ID ja rolli nimi (`RegisterResponseDto.java`).

```json
{
  "userId": 1,
  "roleName": "customer"
}
```

- `userId` — äsja loodud kasutaja `user.id`. Näites on 1, sest `3_import.sql` ei lisa ühtegi kasutajat, seega esimene registreeritud kasutaja saab ID 1.
- `roleName` — kasutaja rolli nimi (`role.name`); registreerimisel alati `customer`.

Frontend salvestab need sessionStorage'isse ja suunab kasutaja avalehele (Kodu).

## Eesmärk

Külastaja (pole sisse logitud) täidab vaates "Registreeri kasutajaks" oma andmed ja spordihuvid ning loob endale süsteemi konto. Teenus loob uue kasutaja rolliga `customer` ja staatusega `'A'` (aktiivne), sellele profiili (`profile`) ning valitud spordialade seosed (`user_sport`). Pärast edukat registreerimist on kasutaja kohe sisse logitud, kuna vastus sisaldab samu andmeid, mis sisselogimise vastus (`userId`, `roleName`).

Teenuse loogika:

1. Kontrolli, et `areaId` viitab olemasolevale piirkonnale (`area`). Request body valideerimine (sh `sportIds` sisaldab vähemalt ühte elementi) toimub enne teenuse loogikat.
2. Kontrolli, et iga `sportIds` element viitab olemasolevale spordialale (`sport`).
3. Kontrolli, et aktiivse staatusega (`status = 'A'`) kasutajat samasuguse e-postiga veel ei ole.
4. Loo `user` rida (`role_id` = rolli `customer` ID, `status = 'A'`).
5. Loo `profile` rida (seotud loodud kasutajaga).
6. Loo iga valitud spordiala kohta üks `user_sport` rida.
7. Tagasta `userId` ja `roleName`.

Kõik kirjutamised peavad toimuma ühes tehingus (`@Transactional`), et vea korral ei jääks pooleldi loodud kasutajat.

## Seotud andmebaasi tabelid

Vt `docs/database/2_create.sql`.

### `user`

Süsteemi kasutaja. `email` peab vastama regexile (CHECK piirang), unikaalsuse piirangut andmebaasis ei ole — aktiivse e-posti unikaalsust peab kontrollima teenus.

```sql
CREATE TABLE "user" (
    id serial  NOT NULL,
    role_id int  NOT NULL,
    email varchar(255)  NOT NULL,
    password varchar(255)  NOT NULL,
    status varchar(3)  NOT NULL,
    CONSTRAINT CHECK_0 CHECK (( email ~ '^[^@\s]+@[^@\s]+\.[^@\s]+$' )) NOT DEFERRABLE INITIALLY IMMEDIATE,
    CONSTRAINT user_pk PRIMARY KEY (id)
);
-- FK: user_role (role_id -> role.id)
```

### `profile`

Kasutaja isikuandmed. `phone_number` on `int`, seega ei mahu sinna `+372` prefiksit ega tähemärke.

```sql
CREATE TABLE profile (
    id serial  NOT NULL,
    user_id int  NOT NULL,
    first_name varchar(255)  NOT NULL,
    last_name varchar(255)  NOT NULL,
    phone_number int  NOT NULL,
    area_id int  NOT NULL,
    CONSTRAINT profile_pk PRIMARY KEY (id)
);
-- FK: profile_user (user_id -> user.id), profile_area (area_id -> area.id)
```

### `user_sport`

Liitetabel kasutaja ja spordialade vahel (mitu-mitmele).

```sql
CREATE TABLE user_sport (
    id serial  NOT NULL,
    sport_id int  NOT NULL,
    user_id int  NOT NULL,
    CONSTRAINT user_sport_pk PRIMARY KEY (id)
);
-- FK: user_sport_sport (sport_id -> sport.id), user_sport_user (user_id -> user.id)
```

### `role`

```sql
CREATE TABLE role (
    id serial  NOT NULL,
    name varchar(255)  NOT NULL,
    CONSTRAINT role_pk PRIMARY KEY (id)
);
```

Näidisandmed (`3_import.sql`):

| id | name |
|---|---|
| 1 | admin |
| 2 | trainer |
| 3 | customer |

### `area` ja `sport`

Kasutatakse ainult `areaId` ja `sportIds` olemasolu kontrolliks. Struktuur ja näidisandmed on kirjeldatud taskides "Piirkondade nimekirja päring" ja "Spordialade nimekirja päring".

## Veaolukorrad

Vea korral on response body kujul `{ "message": "...", "errorCode": "..." }` (`ApiError`).

| Olukord | Status code | Response body |
|---|---|---|
| Aktiivse staatusega (`status = 'A'`) kasutaja samasuguse e-postiga on juba olemas | 403 Forbidden | `{ "message": "Sellise e-postiga aktiivne kasutaja on süsteemis juba olemas", "errorCode": "USER_UNAVAILABLE" }` |
| `areaId` ei viita ühelegi piirkonnale | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'areaId' väärtusega: 99", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| Mõni `sportIds` element ei viita ühelegi spordialale | 404 Not Found | `{ "message": "Ei leidnud primary keyd 'sportId' väärtusega: 99", "errorCode": "PRIMARY_KEY_NOT_FOUND" }` |
| Request body valideerimine ebaõnnestub (nt e-post ei ole korrektse kujuga) | 400 Bad Request | `{ "message": "email: Sisesta korrektne e-posti aadress", "errorCode": "INCORRECT_INPUT" }` |
| `sportIds` on tühi massiiv või puudub (vähemalt üks spordiala peab olema valitud) | 400 Bad Request | `{ "message": "sportIds: Vali vähemalt üks spordiala", "errorCode": "INCORRECT_INPUT" }` |
| Ootamatu serveri viga | 500 Internal Server Error | — |

Veateated ja errorCode'id põhinevad olemasolevatel erinditel (`ForbiddenException`, `PrimaryKeyNotFoundException`) ja `RestExceptionHandler`'il paketis `ee.sportclub.infrastructure`. `USER_UNAVAILABLE` on uus errorCode.

## Vastuvõtu kriteeriumid

- [ ] Endpoint `POST /api/register` on olemas ja võtab vastu `RegisterRequestDto` JSON-i
- [ ] Õnnestunud registreerimisel tagastatakse 200 ja `RegisterResponseDto` kujul `{ "userId": ..., "roleName": "customer" }`
- [ ] Andmebaasi luuakse üks `user` rida (rolliga `customer`, `status = 'A'`), üks `profile` rida ja iga `sportIds` elemendi kohta üks `user_sport` rida
- [ ] Kirjutamised toimuvad ühes tehingus — vea korral ei jää andmebaasi pooleldi loodud kasutajat
- [ ] Aktiivse kasutaja sama e-postiga registreerimine tagastab 403 ja errorCode'i `USER_UNAVAILABLE`
- [ ] Olematu `areaId` tagastab 404 ja `PRIMARY_KEY_NOT_FOUND` sõnumiga `'areaId'`
- [ ] Olematu `sportId` tagastab 404 ja `PRIMARY_KEY_NOT_FOUND` sõnumiga `'sportId'`
- [ ] Valideerimisviga tagastab 400 ja errorCode'i `INCORRECT_INPUT`
- [ ] Tühi või puuduv `sportIds` tagastab 400 ja `INCORRECT_INPUT`; kasutajat ega profiili ei looda
- [ ] Kirjutatud on automaattestid: edukas registreerimine, e-post juba kasutusel, olematu `areaId`, olematu `sportId`, tühi `sportIds`, valideerimisviga
