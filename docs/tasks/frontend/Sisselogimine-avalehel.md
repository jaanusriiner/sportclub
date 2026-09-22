# Sisselogimine avalehel

**Vaade:** `HomeView.vue`, route `/`

**Roll:** Kõik rollid (vaate ja sisselogimise nupu näeb igaüks, ka külastaja)

**Vaste balsamic mockupis:** "Avakuva koos sisselogimisega", lehekülg 1/1 (vt lisatud pilt `Sisselogimine-avalehel.png`)

> **Märkus:** Vastav pilt (`docs/balsamic/pdf-images/HomeView.png`) pole hetkel veel genereeritud — lisa see kausta ja seejärel siia taski kõrvale, kui see valmib.

## Kasutajavoog

Külastaja avab avalehe (`/`) ja klikib menüüs lingil "Logi Sisse", mis avab modaalakna "Sisselogimine" väljadega e-post ja salasõna. Kasutaja sisestab andmed ja vajutab nuppu "Logi Sisse" — päring saadetakse backendile. Õnnestumise korral logitakse kasutaja sisse ja ta suunatakse treeningute vaatesse. Nupp "Sulge" sulgeb modaalakna ilma serveriga suhtlemata.

Avalehe muu sisu (tutvustustekst, pildid, menüülingid "Tutvustus", "Treeningud", "Registreeru") on selle taski jaoks väljaspool skoopi — mockup ei sisalda nende jaoks eraldi API märget, seega on tegu staatilise sisuga, mis lahendatakse vajadusel eraldi taskiga.

## Kasutajaliidese elemendid

| Element | Tüüp | Kirjeldus/käitumine |
|---|---|---|
| "Logi Sisse" menüülink | nupp | Avab modaalakna "Sisselogimine" |
| "email" väli | tekstisisend | Kasutaja e-post, kohustuslik |
| "salasõna" väli | paroolisisend | Kasutaja salasõna, kohustuslik |
| "Logi Sisse" nupp (modaalaknas) | nupp | Saadab `POST /api/login` päringu praeguste väljaväärtustega |
| "Sulge" nupp | nupp | Sulgeb modaalakna, ei tee API kutset |
| Veateade | AlertDanger.vue | Kuvatakse ebaõnnestunud sisselogimise korral |

## Käitumine ja valideerimine

1. Menüülingi "Logi Sisse" klikkimine avab modaalakna. See on puhtalt kliendipoolne olek (nt `isLoginModalOpen`), API kutset ei tehta.
2. Mockup ei kirjelda tühjade väljade eraldi kontrolli frontendis (erinevalt nt registreerimisvormist) — väljad saadetakse backendile nii, nagu need parasjagu on täidetud. Tühja/vale `email`/`password` kombinatsiooni korral vastab backend niikuinii 403 `INCORRECT_CREDENTIALS` veaga (vt allpool), mida kuvatakse tavapärase veateatena.
3. Nupu "Logi Sisse" (modaalaknas) vajutus saadab `POST /api/login` väljadega `email` ja `password`.
4. **Õnnestumisel (200):** `userId` ja `roleName` salvestatakse `sessionStorage`'isse, kasutajale kuvatakse success message "login successful" ning ta suunatakse vaatele `/training`.
   - `/training` route't hetkel `src/router/index.js` failis ei ole — see tuleb kas selle taski käigus lisada (ajutise/placeholder view'na) või kokku leppida, et see luuakse koos vastava treeningute taskiga. Tooge see täpsustusvajadus taski tegijale esile.
5. **Ebaõnnestumisel (403, `INCORRECT_CREDENTIALS`):** kuvatakse `AlertDanger.vue` komponendiga backend'i `message` väli ("Vale e-post või parool"). Modaalaken jääb lahti, väljad ei tühjene.
6. Nupu "Sulge" vajutus sulgeb modaalakna ja lähtestab väljad, ilma API kutseta.

## API kutsed

### `POST /api/login`

**Backend task:** vt `docs/tasks/backend/Kasutaja-sisselogimine.md`

`LoginRequestDto.java` — request body:

```json
{
  "email": "admin@admin.ee",
  "password": "123"
}
```

`LoginResponseDto.java` — response (200 OK):

```json
{
  "userId": 1,
  "roleName": "admin"
}
```

- `roleName` võimalikud väärtused: `admin`, `trainer`, `customer` — see väärtus tuleb salvestada, kuna edaspidised vaated (nt admin-only lingid) sõltuvad sellest.

**Veateated:**

| Status code | errorCode | message | Frontend käitumine |
|---|---|---|---|
| 403 Forbidden | `INCORRECT_CREDENTIALS` | "Vale e-post või parool" | Kuva `AlertDanger.vue`'ga sõnum, modaalaken jääb avatuks |
| 500 Internal Server Error | — | — | Kuva üldine veateade / suuna veavaatele (vastavalt projekti üldisele veakäsitluse mustrile) |

Näidisandmed (`3_import.sql`, testimiseks): `admin@admin.ee` / `123` (admin), `trainer@trainer.ee` / `123` (trainer), `customer@customer.ee` / `123` (customer). Kasutaja `inactive@inactive.ee` (`status = 'D'`) annab samuti `INCORRECT_CREDENTIALS`.

## Komponendid ja failistruktuur

Vastavalt `docs/frontend/projekti-struktuur.md` konventsioonile:

- `src/views/HomeView.vue` — avaleht, sisaldab menüüd ja avab sisselogimise modaali (fail juba olemas, hetkel platsihoidja sisuga)
- `src/components/modals/LoginModal.vue` — sisselogimise modaalakna komponent (väljad, nupud, veateate kuvamine)
- `src/api-services/LoginService.js` — `POST /api/login` päringu saatmine (Axios), nt meetod `sendLoginRequest(email, password)`

Komponendi sisemine ülesehitus (data/methods struktuur, `.then()/.catch()/.finally()` muster, `event-` eesliitega emits) järgib `docs/frontend/vue-komponendi-struktuur.md` eeskuju.

## Vastuvõtu kriteeriumid

- [ ] Avalehel on nähtav "Logi Sisse" link, mis avab sisselogimise modaalakna
- [ ] Modaalaknas on väljad "email" ja "salasõna" ning nupud "Logi Sisse" ja "Sulge"
- [ ] Nupp "Logi Sisse" saadab `POST /api/login` päringu hetkel sisestatud väärtustega
- [ ] Õnnestunud sisselogimisel salvestatakse `userId` ja `roleName` `sessionStorage`'isse ning kasutaja suunatakse vaatele `/training`
- [ ] Õnnestunud sisselogimisel kuvatakse success message "login successful"
- [ ] Ebaõnnestunud sisselogimise korral (403 `INCORRECT_CREDENTIALS`) kuvatakse `AlertDanger.vue`'ga backend'i `message` väli, modaalaken jääb avatuks
- [ ] Nupp "Sulge" sulgeb modaalakna ilma API kutseta
- [ ] Route `/training` on olemas (või selle puudumine on eraldi täpsustatud/kokku lepitud enne implementeerimist)
