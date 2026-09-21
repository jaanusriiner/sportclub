# Kasutaja sisselogimine

**Teenus:** `POST /api/login`

**Vaste balsamic mockupis:** "Avakuva" (`HomeView.vue`, route `/`, modaalaken "Sisselogimine"), lehekülg 1/1 (vt lisatud pilt `Kasutaja-sisselogimine.png`)

![Mockup](./Kasutaja-sisselogimine.png)

## Sisend

Path variable'id ja query parameetrid puuduvad. Sisendiks on request body (`LoginRequestDto.java`):

| Väli | Tüüp | Kirjeldus |
|---|---|---|
| `email` | String | Kasutaja e-post, kasutajanimena (`user.email`), modaalakna väli "email" |
| `password` | String | Salasõna (`user.password`), modaalakna väli "salasõna" |

```json
{
  "email": "admin@admin.ee",
  "password": "123"
}
```

## Väljund

**Response (200 OK):** sisselogitud kasutaja ID ja rolli nimi (`LoginResponseDto.java`).

```json
{
  "userId": 1,
  "roleName": "admin"
}
```

- `userId` — kasutaja `user.id`
- `roleName` — kasutaja rolli nimi (`role.name`, seos `user.role_id` → `role.id`); võimalikud väärtused on `admin`, `trainer` ja `customer`

Näidisväärtused (`admin@admin.ee`, `userId` 1, `admin`) vastavad `3_import.sql` esimesele kasutajale. Import sisaldab kasutajaid `admin@admin.ee` (admin), `trainer@trainer.ee` (trainer), `customer@customer.ee` (customer) ja `inactive@inactive.ee` (customer, `status = 'D'`), kõigi salasõna on `123`.

Frontend salvestab `userId` ja `roleName` sessionStorage'isse, kuvab kasutajale success message "login successful" ja suunab ta vaatele `/training`.

## Eesmärk

Külastaja (pole sisse logitud) avab avalehel (`HomeView.vue`) menüülingi "Logi Sisse" kaudu modaalakna "Sisselogimine", sisestab e-posti ja salasõna ning vajutab nuppu "Logi Sisse". Teenus tuvastab aktiivse kasutaja ja tagastab tema ID ja rolli, mille põhjal frontend teab, kes on sisse logitud ja mida talle näidata. Nupp "Sulge" sulgeb modaalakna ilma API kutseta.

Teenuse loogika:

1. Otsi kasutajat (`user`), kelle `email` ja `password` on samad, mis requestis, ja kelle `status = 'A'` (aktiivne).
2. Kui sellist kasutajat ei leidu, visata `ForbiddenException` errorCode'iga `INCORRECT_CREDENTIALS`.
3. Tagasta leitud kasutaja `userId` ja tema rolli nimi (`roleName`).

Ebaõnnestumise põhjust (tundmatu e-post, vale salasõna, mitteaktiivne konto) väljapoole ei paljastata, kõigil kolmel juhul on vastus sama.

## Seotud andmebaasi tabelid

Vt `docs/database/2_create.sql`.

### `user`

Süsteemi kasutaja. Sisselogimisel otsitakse rida `email`, `password` ja `status` järgi.

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

### `role`

Kasutaja roll; `roleName` vastuses tuleb siit.

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

Tabelit `profile` (kasutaja isikuandmed) see teenus ei puuduta.

## Veaolukorrad

Vea korral on response body kujul `{ "message": "...", "errorCode": "..." }` (`ApiError`).

| Olukord | Status code | Response body |
|---|---|---|
| Aktiivset (`status = 'A'`) kasutajat sellise `email` ja `password` kombinatsiooniga ei leidu | 403 Forbidden | `{ "message": "Vale e-post või parool", "errorCode": "INCORRECT_CREDENTIALS" }` |
| Ootamatu serveri viga | 500 Internal Server Error | — |

Veateade põhineb olemasoleval `ForbiddenException`'il ja `RestExceptionHandler`'il paketis `ee.sportclub.infrastructure`. `INCORRECT_CREDENTIALS` on uus errorCode.

Mockup ei kirjelda request body eraldi valideerimist. Tühja või puuduva `email`/`password` väärtusega päringule ei leidu ühtegi aktiivset kasutajat, seega vastus on sama 403 `INCORRECT_CREDENTIALS`.

## Vastuvõtu kriteeriumid

- [ ] Endpoint `POST /api/login` on olemas ja võtab vastu `LoginRequestDto` JSON-i (`email`, `password`)
- [ ] Õnnestunud sisselogimisel tagastatakse 200 ja `LoginResponseDto` kujul `{ "userId": ..., "roleName": ... }`
- [ ] `userId` on kasutaja `user.id` ja `roleName` on kasutaja rolli `role.name`
- [ ] Mitteaktiivse (`status` ≠ `'A'`) kasutaja õige e-posti ja salasõnaga sisselogimine tagastab 403 ja `INCORRECT_CREDENTIALS`
- [ ] Vale salasõna ja tundmatu e-post tagastavad 403, errorCode'i `INCORRECT_CREDENTIALS` ja sõnumi "Vale e-post või parool"
- [ ] Endpoint on kättesaadav ka sisse logimata kasutajale (teenus ongi sisselogimiseks)
- [ ] Kirjutatud on automaattestid: edukas sisselogimine, vale salasõna, tundmatu e-post, mitteaktiivne kasutaja
