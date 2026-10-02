# Juhend: POST /api/facilities

**Taski fail:** `Asukoha-lisamine.md`
**Kontroller:** `FacilityController.java` (olemas, pakett `ee.sportclub.controller.facility`)
**Implementeerimise voog:** RestController → Service → Mapper → Repository → Service → RestController

---

## Sissejuhatus

Selle endpointiga lisab Admin süsteemi uue treeningu toimumiskoha. Ühe päringuga tekib üks `facility` kirje, iga valitud spordiala kohta üks `sport_facility` kirje ja soovi korral üks `facility_image` kirje pildiga. Selle harjutuse käigus õpid:
- kontrollima kasutaja rolli
- kontrollima unikaalsust tõstutundetult
- salvestama mitu seotud kirjet ühes transaktsioonis
- valideerima valikulist välja (pilt) nii vormingu kui suuruse järgi

---

## Samm 0 — Eeltöö: andmebaas ja entity'd

### Mida teha?

Tabelit `facility_image` andmebaasis **veel pole**. Enne koodi kirjutamist tuleb see luua, muidu pole pilti kuhugi salvestada.

1. Lisa taskifailis olev `CREATE TABLE facility_image` ja foreign key `2_create.sql`-i (vaata, kuhu failis teised tabelid ja viited on paigutatud, ja järgi sama järjekorda).
2. Jooksuta andmebaasi skriptid uuesti (`1_reset_database.sql` → `2_create.sql` → `3_import.sql`).
3. Anna meeskonnale teada, et skeem muutus.

Seejärel kontrolli entity'sid paketis `persistence/facility/`:
- `Facility` on olemas.
- `SportFacility` ja `FacilityImage` **puuduvad**. Loo need JPA Buddy abil (paremklõps paketil → **New → JPA Entity → From DB**) või käsitsi, teiste entity'de eeskujul.

> **Mõtle:** milline Java tüüp sobib andmebaasi `bytea` veerule? Vaata taskifailist.

---

## Samm 1 — RestController

### Mida teha?

`FacilityController.java` on juba olemas ja seal on `GET /api/facilities`. Lisa samasse klassi uus meetod.

> **Pane tähele:** Selles projektis on igal meetodil kogu tee koos `/api` eesliitega, klassitasemel `@RequestMapping`-ut pole.

Alusta meetodist **ilma mappingannotatsioonideta**:

```java
public void meetodiNimi(SisendDtoTüüp sisendDtoTüüp) {
    // tühi meetod esialgu
}
```

> **Mõtle:** Mis on hea nimi? Vaata, kuidas on nimetatud teiste kontrollerite loomise meetodid.

Request body DTO-d (`CreateFacilityRequestDto`) veel pole. Kirjuta tüüp parameetrisse, siis **Alt+Enter** → **Create class**. Kontrolli, et klass läheks paketti `controller.facility.dto`.

DTO väljad leiad taskifaili "Sisend" tabelist. Mõtle iga välja juures:
- **Tüüp:** mis tüüpi on spordialade id-de kogum?
- **Valideerimine:** millised väljad on kohustuslikud? Millistel on pikkuspiirang? Vaata taskifaili jaotist "Pildi piirangud". Seal on kaks reeglit, mis tuleb `imageData` peale panna.

> **Vihje `imageData` vormingu kohta:** `@Pattern` laseb `null`-i läbi, aga tühja stringi `""` mitte. Taski järgi tähendab tühi string "pilti pole". Kuidas kirjutada regex nii, et ka tühi string sobiks?

Seejärel lisa meetodile:
1. **Mappingannotatsioon:** `@PostMapping`
2. **Parameetri annotatsioonid:** `@RequestBody` ja `@Valid`
3. **Swagger annotatsioonid:** `@Operation` ja `@ApiResponses`. Vaata taskist "Veaolukorrad" tabelit: milliseid koode tuleb dokumenteerida?

> **Mõtle enne tagastustüübi valimist:** Taski järgi on vastus `200 OK` **ilma body-ta**.

### Service klassi ettevalmistus

`FacilityService` on juba olemas ja kontrolleris on ka väli, mis sellele viitab. Kutsu service meetod välja:

```java
public void meetodiNimi(SisendDtoTüüp sisendDtoTüüp) {
    teenuseMuutuja.meetodiNimi(sisendDtoTüüp);
}
```

> **IntelliJ vihje:** Kui meetodi nimi on punane, vajuta **Alt+Enter** → **"Create method in FacilityService"**.

---

## Samm 2 — Service: kontrollid

### Mida teha?

Ava `FacilityService` ja mine äsja loodud meetodisse. Enne midagi loomist tuleb kontrollida taskifaili "Teenuse loogika" sammud 1–4.

**Kasutaja ja roll.** Kas projektis on juba meetod, mis leiab kasutaja id järgi või viskab `PrimaryKeyNotFoundException`-i? Otsi `findValidAdminUser`. Kui see asub teises service'is, mõtle, kas kutsud seda sealt või teed oma.

> **Mõtle:** Kuidas jõuda kasutaja entity'lt tema rolli nimeni? Ava `User.java`. Ja kust tuleb võrdlemiseks väärtus `"admin"`? Projektis on selle jaoks enum, vaata, kuidas `RegisterService` seda kasutab.

**Maakond.** Kas `getValidArea` on juba kuskil olemas?

**Spordialad.** Taskis on hoiatus: `findAllById` jätab olematud id-d **vaikselt vahele**. Mõtle, kuidas avastada, et mõni id jäi leidmata, ja kuidas teada saada, **milline** id see oli (veateates peab olema konkreetne väärtus).

**Unikaalne nimi.** Siin on vaja uut repository päringut. Kirjuta `facilityRepository.` ja vaata, kas sobiv meetod on olemas. Kui ei ole, kasuta JPA Buddyt:

1. Paremklõps repository interface'is → **JPA Buddy** → **Query**
2. Meetodi tüüp: mõtle, kas tahad tagasi kirjet või ainult vastust "jah/ei". Vaata **Exists**
3. Query condition: nimi. Kuidas teha võrdlus **tõstutundetuks**? JPA Buddy pakub tingimuse juures valikut **Ignore case**
4. **Advanced** → **Named parameters**

Iga kontrolli jaoks tee eraldi `validate...` helper, nagu `TrainingService`-s. Vead viska `ForbiddenException`-iga. Uued veakoodid `NOT_ADMIN` ja `FACILITY_NAME_UNAVAILABLE` tuleb enne lisada `Error.java`-sse.

> **Meetodi palve:** Kui kutsud välja meetodi, mis midagi tagastab, ja tahad selle infoga edasi töötada, pane tulemus kohe muutujasse. Maakonda ja spordialasid läheb hiljem vaja.

---

## Samm 3 — Mapper (sisendi teisendamine)

### Mida teha?

Enne salvestamist tuleb request DTO teisendada `Facility` entity-ks.

`FacilityMapper` on juba olemas, sest selles on `GET`-i jaoks `toFacilityDto`. Lisa samasse mapperisse uus meetod, mis teeb vastupidise teisenduse:

```java
EntiteetTüüp toEntiteetTüüp(SisendDtoTüüp sisendDtoTüüp);
```

> **IntelliJ vihje:** Lisa üks tühi rida `@Mapping(source = "", target = "")`, kliki `target = ""` jutumärkide vahele ja vajuta **Ctrl+Space**. IntelliJ näitab kõiki `Facility` välju. Nii näed kohe, mitu rida vaja on.

Lisa `@Mapping` **iga** entity välja kohta:

```java
@Mapping(ignore = true, target = "id")
@Mapping(source = "dtoVäli", target = "entiteediVäli")
@Mapping(ignore = true, target = "seotudObjekt")
EntiteetTüüp toEntiteetTüüp(SisendDtoTüüp sisendDtoTüüp);
```

> **Mõtle iga välja juures:**
> - `id`: loomisel alati `ignore = true`.
> - **Seotud objekt (maakond):** DTO-s on ainult id. Mapper ei pääse andmebaasi ligi. Sul on aga service'is juba päris entity olemas.
> - **Nimed, mis ei kattu:** kirjuta `source` ka siis, kui nimi kattub.
> - Kas `sportIds` ja `imageData` on üldse `Facility` väljad?

---

## Samm 4 — Repository

### Mida teha?

Salvestada on vaja **kolme** sorti kirjeid:

| Kirje | Repository |
|---|---|
| `Facility` | `FacilityRepository` on olemas |
| `SportFacility` | puudub, tuleb luua |
| `FacilityImage` | puudub, tuleb luua |

Loo puuduvad repositooriumid paketti `persistence/facility/`, `FacilityRepository` eeskujul.

> **Rusikareegel:** Lihtsa loomise puhul piisab `JpaRepository` baasmeetoditest. `SportFacility` kirjeid on mitu. Mäletad, mis baasmeetod salvestab terve listi korraga?

---

## Samm 5 — tagasi Service'i

### Mida teha?

Service meetod paneb nüüd kõik tükid kokku.

**5a. Facility:** kutsu mapper välja ja määra service'is need väljad, mis mapperis jäid `ignore = true`.

**5b. SportFacility kirjed:** iga spordiala kohta üks objekt, mis seob spordiala ja **uue** asukoha. Kogu need listi.

> **Mõtle:** kas `SportFacility` objekt vajab viidet **salvestatud** asukohale? Mis järjekorras peavad `save` ja `saveAll` olema?

**5c. FacilityImage (valikuline):** pildikirje luuakse ainult siis, kui pilt on olemas.

> **Mõtle:** milliseid kolme olukorda tuleb `imageData` puhul eristada? Vihje: `null`, `""` ja päris väärtus. Kas mõni `String`-i meetod katab kaks esimest korraga?

Teksti baitideks teisendamiseks on projektis olemas `StringBytesConverter`, vaata paketti `infrastructure/util`.

**5d. Transaktsioon:** kuidas tagada, et kõik salvestused kas õnnestuvad või ebaõnnestuvad koos? Vaata, mis annotatsioon on teistel kirjutavatel service-meetoditel.

---

## Samm 6 — tagasi RestController'isse

### Mida teha?

Mine tagasi kontrolleri meetodisse ja kontrolli:

- Kas tagastustüüp vastab Samm 1 otsusele? (200, body puudub)
- Kas `@ApiResponses` kirjeldab kõiki veaolukordi, mis service'is ja DTO-s tegelikult tekivad (404, 403, 400)?
- Kas `@Operation` summary kirjeldab täpselt, mida endpoint teeb, sh spordialade ja pildi salvestamist?

---

## Samm 7 — kood ilusaks (refactor)

### Make it work → Make it beautiful

Kui kood töötab, vaata, kas seda saab puhtamaks muuta.

**Extract Method IntelliJ'ga:**

Märgi service meetodis koodilõik, mida soovid eraldada helper meetodiks → paremklõps → Refactor → Extract Method.

Head kandidaadid: rollikontroll, spordialade leidmine koos olematu id kontrolliga, `SportFacility` listi loomine, pildi salvestamine.

> **Tähelepanu:** IntelliJ annab ekstraktimisel parameetriks sageli kogu objekti. Vaata üle, kas helper meetod vajab tegelikult kogu objekti või ainult üht välja, ja tee vajadusel korrektuur.

Enne:
```java
kontrolliMidagiHelper(dtoObjekt);

private void kontrolliMidagiHelper(DtoTüüp dto) {
    boolean onProbleem = repositoorium.kontrollimeetod(dto.getMingiVäli());
    if (onProbleem) {
        throw new MingiException(...);
    }
}
```

Pärast (parem, sest edasi antakse ainult vajalik väli):
```java
kontrolliMidagiHelper(dtoObjekt.getMingiVäli());

private void kontrolliMidagiHelper(VäljaTüüp väljaNimi) {
    boolean onProbleem = repositoorium.kontrollimeetod(väljaNimi);
    if (onProbleem) {
        throw new MingiException(...);
    }
}
```

### Meetodite järjekord

Kontrolli meetodite järjekorda vastavalt Java konventsioonile:
1. `public` meetodid enne
2. `private` meetodid pärast
3. Järjesta ka väljakutsumise hierarhia järgi: peameetod üleval, helper meetodid all. Selleks saab kasutada skilli `/skill-jarjesta-meetodid`.

---

## Kokkuvõte ja kontrollnimekiri

Enne kui koodi valmis kuulutad, kontrolli läbi:

- [ ] `facility_image` tabel on `2_create.sql`-is ja andmebaas on uuesti loodud
- [ ] `SportFacility` ja `FacilityImage` entity'd ning repositooriumid on olemas paketis `persistence/facility/`
- [ ] `FacilityController`-isse on lisatud `@PostMapping("/api/facilities")` meetod koos `@Operation` ja `@ApiResponses` annotatsioonidega
- [ ] `CreateFacilityRequestDto` asub paketis `controller.facility.dto` ja sellel on valideerimisannotatsioonid, sh `imageData` vorming (JPEG/PNG, tühi lubatud) ja suurus
- [ ] `Error.java` sisaldab uusi väärtusi `NOT_ADMIN` ja `FACILITY_NAME_UNAVAILABLE`
- [ ] Nime unikaalsuse päring on tõstutundetu ja kasutab Named parameters stiili
- [ ] Olematu `sportId` annab 404, mitte ei jää vaikselt vahele
- [ ] `FacilityMapper`-i uues meetodis on iga entity väli kas `source` või `ignore = true`
- [ ] Service meetod on `@Transactional`
- [ ] Meetodite järjekord: `public` enne, `private` pärast, järjestatud väljakutsumise hierarhia järgi
- [ ] Kood kompileerub ja endpoint on Swagger UI-s nähtav

---

> **Järgmine samm:** Testi endpointi Swagger UI kaudu (`http://localhost:8080/swagger-ui/index.html`) taskifaili näidispäringuga. Kontrolli `psql`-iga, et tekkis üks `facility`, kaks `sport_facility` ja (pildiga päringu korral) üks `facility_image` rida.
> Proovi ka veaolukordi: `adminId = 2`, `facilityName = "laagri tennisekeskus"`, `sportIds = [99]`, `sportIds = []` ja `imageData` webp eesliitega.
