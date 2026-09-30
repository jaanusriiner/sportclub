# Juhend: POST /api/training-dates/{trainingDateId}/register

**Taski fail:** `Treeningule-registreerimine.md`
**Kontroller:** `TrainingController.java` (olemasolev, pakett `controller/training/`)
**Implementeerimise voog:** RestController → Service → Mapper → Repository → Service → RestController

---

## Sissejuhatus

See endpoint registreerib kasutaja ühele konkreetsele treeningu toimumisajale (`training_date`). Enne kirjutamist tehakse mitu ärikontrolli: kas treening on olemas, kas kasutaja on grupi liige, kas ta on juba registreerunud ja kas treeningul on vabu kohti. Õnnestumise korral luuakse uus `user_training` kirje ja suurendatakse `training_date.user_count` väärtust.

Selle harjutuse käigus õpid:
- kuidas POST endpoint võtab vastu nii `@PathVariable`-i kui ka `@RequestBody`-t
- kuidas mitu valideerimist service kihis loogiliselt järjestada ja iga vea jaoks õige exception visata
- miks peab kontrollimine ja kirjutamine toimuma **ühes tehingus** ning kuidas vältida race condition'it

---

## Samm 0 — Mis on juba olemas, mis puudub?

### Mida teha?

Enne alustamist vaata üle, millised klassid juba on:

- `persistence/training/TrainingDate.java` on olemas
- `persistence/training/TrainingDateRepository.java` on olemas
- `persistence/user/UserTraining.java` on olemas (entiteet on olemas)
- `persistence/user/UserTrainingGroup.java` ja `UserTrainingGroupRepository.java` on olemas
- `persistence/user/UserRepository.java` on olemas

> **Mõtle:** Kas `UserTraining` entiteedi jaoks on juba ka **repository** olemas? Kui ei, läheb sul seda Samm 4 juures vaja.

Vaata ka `Error.java` enumit (`ee/sportclub/Error.java`). Siia koondatakse projekti äriveateated. Taskifaili "Veaolukorrad" tabelis on kolm uut veakoodi. Kas need on enumis juba olemas?

---

## Samm 1 — RestController

### Mida teha?

Kontroller `TrainingController` on juba olemas (`controller/training/`). Lisa sinna uus meetod.

> **Tähelepanu:** Selles projektis ei kasutata klassi tasemel `@RequestMapping("/api")`, vaid **kogu tee** kirjutatakse mappingannotatsiooni sisse (vaata olemasolevat `getUpcomingTrainingsByUserId` meetodit). Järgi sama mustrit.

### Meetodi loomine

Alusta meetodist **ilma mappingannotatsioonideta**:

```java
public void meetodiNimi(TeeMuutujaTüüp teeMuutuja, SisendDtoTüüp sisendDto) {
    // tühi meetod esialgu
}
```

> **Mõtle:** Mis on selle meetodi hea nimi? Nimi peaks ütlema, **mida** meetod teeb.

Seejärel lisa:
1. **Mappingannotatsioon** `@PostMapping` koos taskifailis toodud teega
2. **Parameetrite annotatsioonid**: üks parameeter tuleb URL-ist (`@PathVariable`), teine request body'st (`@RequestBody`)
3. **Swagger annotatsioonid** `@Operation` ja `@ApiResponses`. Lisa kõik taskifaili vastusekoodid (200, 403, 404) ja vigade juurde `content = @Content(schema = @Schema(implementation = ApiError.class))`

```java
@PostMapping("/mingi/{teeMuutuja}/tegevus")
@Operation(summary = "Lühikokkuvõte")
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "403", description = "Millal see tekib",
                content = @Content(schema = @Schema(implementation = VeaKlass.class))),
        @ApiResponse(responseCode = "404", description = "Millal see tekib",
                content = @Content(schema = @Schema(implementation = VeaKlass.class)))})
public void meetodiNimi(@PathVariable TeeMuutujaTüüp teeMuutuja, @RequestBody SisendDtoTüüp sisendDto) {
}
```

### Sisendi DTO

Request body jaoks on vaja DTO klassi. Taskifail annab nime ja välja (`TrainingDateRegisterRequestDto`, üks väli).

- Kaust: `controller/training/dto/`
- Vaata olemasolevaid DTO-sid (nt `TrainingGroupOverviewDto`), milliseid Lomboki annotatsioone siin projektis kasutatakse

> **Mõtle:** Kas sisendiväli tohib olla `null`? Kui ei, siis milline valideerimisannotatsioon ja milline annotatsioon kontrolleri parameetril paneb selle tööle? Vaata `RegisterController`-it.

> **Mõtle enne tagastustüübi valimist:** Vaata taskifailist **Väljund** sektsiooni. Siin tuleb tagastada objekt, milles on üks tekstiväli. Pea seda meeles Samm 5 juurde jõudes.

### Service'i väljakutse

Service `TrainingService` on juba olemas ja kontrolleris juba väljana kasutusel. Kutsu kontrolleri meetodist välja uus service meetod:

```java
teenuseMuutuja.meetodiNimi(teeMuutuja, sisendDto);
```

> **IntelliJ vihje:** Kui meetod on punasega alla joonitud, vajuta **Alt+Enter** → **"Create method in TeenusKlass"**.

---

## Samm 2 — Service: esimene kontroll

### Mida teha?

Nüüd liigud `TrainingService` klassi äsja loodud meetodisse. Sisse tuleb treeningu toimumisaja id ja kasutaja id. Vaata taskifaili "Teenuse loogika" nimekirja, sest seal on sammud täpselt järjekorras.

### Treeningu toimumisaja leidmine

Mõtle: **millisest tabelist** on vaja esimesena andmeid pärida? Kas JPA pakub selle jaoks valmis meetodit?

```java
public void meetodiNimi(TeeMuutujaTüüp teeMuutuja, SisendDtoTüüp sisendDto) {
    entiteetRep  // <- kirjuta algus siia
}
```

> **IntelliJ vihje:** Kirjuta repositooriumi muutuja nime algus ja vajuta **Tab**. IntelliJ lisab repositooriumi klassiväljana!

Valmismeetod tagastab `Optional`-i. Ära lase sel lihtsalt seista:

> **Mõtle:** Kas treeningu toimumisaeg on selle teenuse jaoks kohustuslik? Kui jah, siis mis juhtub, kui seda ei leita? Vaata taskifailist veateadet. Milline exception klass annab täpselt selle `message`/`errorCode` kuju? Vaata ka, kuidas `RegisterService.getValidArea` sama asja teeb.

Kui meetod tagastab midagi, **pane tulemus kohe muutujasse**.

---

## Samm 3 — Kas mapperit on vaja?

### Mida teha?

POST voos teisendatakse sisendi DTO tavaliselt mapperiga entiteediks. Siin tasub aga enne järele mõelda.

Vaata `UserTraining` entiteeti: millised väljad sellel on ja mis **tüüpi** need on? Võrdle seda sisendi DTO-ga.

> **Mõtle:**
> - `id` genereeritakse andmebaasis, seega see jääb ignoreerituks.
> - Ülejäänud väljad on **entiteedid**, mitte `Integer`-id. Kas mapper oskab sisendi DTO ühest `Integer` väljast teha terve entiteedi?
> - Kust sa need entiteedid tegelikult saad? Üks on sul Samm 2 lõpuks juba muutujas olemas.

Siit on kaks võimalust:
- **Mapperiga:** tee mapper meetod, kus iga target-väli on eksplitsiitselt `source` või `ignore = true`, ja täida entiteedid pärast mappimist service'is setter'itega
- **Ilma mapperita:** kui mapperi kõik väljad jääksid nagunii `ignore = true`, loo uus entiteet service'is `new` + setter'itega

Kui valid mapperi, kehtib reegel: kui meetodi signatuur on olemas, klikka `target = ""` vahele ja vajuta **Ctrl+Space**. Nii näed, millised target-väljad on olemas. **Ükski target-väli ei tohi jääda kaardistamata.**

```java
@Mapping(ignore = true, target = "id")
@Mapping(ignore = true, target = "seotudEntiteet")
EntiteetTüüp toEntiteetKlassiNimi(SisendDtoTüüp dto);
```

---

## Samm 4 — Repository: kontrollpäringud ja salvestamine

### Mida teha?

Taskifaili loogika sammud 2–5 vajavad andmeid mitmest kohast. Iga kontrolli puhul küsi endalt:
1. Kas mul on see info **juba olemas** (nt entiteedi seoste kaudu)?
2. Kui ei, kas JPA pakub valmismeetodit või on vaja **uut päringut**?
3. Mida päring tagastama peab: entiteedi, listi või lihtsalt **jah/ei**?

> **Rusikareegel:** Kui on vaja teada ainult, **kas** mingi kirje on olemas, on `exists`-tüüpi päring puhtam kui terve entiteedi pärimine.

### Kontrollid, mida on vaja

- **Grupi liikmelisus (samm 3):** Kuidas jõuad treeningu toimumisajast grupini? Vaata `TrainingDate` → `Training` → `TrainingGroup` seoseid. Millisest tabelist liikmelisust kontrollitakse ja millise repositooriumi juurde päring kuulub?
- **Juba registreerunud (samm 4):** Milline tabel seda infot hoiab? Kas selle tabeli repository on olemas? Kui ei, loo see: `JpaRepository<Entiteet, Integer>`, samas paketis entiteediga.
- **Vabad kohad (samm 5):** Kas selleks on üldse repository päringut vaja, või on info juba Samm 2-s leitud objektis olemas?

### Uue meetodi loomine JPA Buddy abil

1. Ava repository interface ja kasuta JPA Buddy paneeli
2. Vali **Query**
3. Meetodi tüüp: **Exists** (olemasolu kontrolliks)
4. Lisa **query conditionid**: milliste väljade järgi filtreeritakse?
5. **Advanced** sektsioonis vali **Named parameters**

Peale loomist:
- Asenda ebamäärane parameetrinimi `id` konkreetsemaga ja tee sama muudatus ka `@Query`-s
- Vaata, kuidas olemasolevad repositooriumid projektis meetodeid nimetavad, ja järgi sama stiili

### Race condition

Taskifail nõuab, et kaks samaaegset registreerumist viimasele vabale kohale ei tohi tekitada olukorda `user_count > max_size`.

> **Mõtle:**
> - Kujuta ette, et kaks kasutajat vajutavad täpselt samal ajal nuppu "Registreeru". Mõlemad loevad `user_count = 3`, `max_size = 4`, mõlemad läbivad kontrolli ja mõlemad kirjutavad. Mis juhtub?
> - Service meetodile `@Transactional` üksi seda probleemi **ei lahenda**. See tagab ainult, et kõik kirjutamised õnnestuvad või ebaõnnestuvad koos.
> - Mis oleks, kui `training_date` rea lugemisel see rida **lukustataks** tehingu lõpuni? Otsi märksõnu: Spring Data JPA `@Lock`, `LockModeType.PESSIMISTIC_WRITE`.
> - Kui teed lukustava päringu, kas asendad sellega Samm 2 `findById`, või teed eraldi meetodi?

### Salvestamine

Kui kontrollid on läbitud:
- Uus seose-entiteet tuleb salvestada. Kas `save()` baasmeetodist piisab?
- `training_date.user_count` tuleb suurendada. Kui entiteet on tehingu sees laetud ja sa muudad tema välja, kas on vaja eraldi `save()`-d? Vihje: `@Transactional` meetodi sees jälgib JPA laetud entiteetide muudatusi ise (otsi märksõna "dirty checking").

---

## Samm 5 — tagasi Service'i

### Mida teha?

Pane service meetodis kõik kokku taskifaili loogika järjekorras.

> **Tuleta meelde Samm 1 otsust:** Endpoint tagastab objekti ühe tekstiväljaga. Seega:
> - on vaja **response DTO** klassi (`TrainingRegisterResponseDto`, `controller/training/dto/`)
> - kust tuleb teate tekst? Kas kirjutad selle otse koodi sisse või hoiad seda sarnases kohas nagu veateated?
> - kas selle DTO täitmiseks on mapperit vaja, või piisab konstruktorist/setter'ist?

Uued veakoodid (`NOT_TRAINING_GROUP_MEMBER`, `ALREADY_REGISTERED`, `TRAINING_FULL`) lisa `Error` enumisse ja kasuta neid samamoodi nagu `RegisterService` kasutab `SPORT_MISSING`-it. Kõik kolm on taskifaili järgi **403**. Milline exception klass annab 403?

Kui service meetod lõpuks `return`-ib midagi, aga tagastustüüp on `void`:

> **IntelliJ vihje:** Vajuta **Alt+Enter** punasel joonel → "Change return type".

---

## Samm 6 — tagasi RestController'isse

### Mida teha?

Service meetod tagastab nüüd response DTO. Täienda kontrolleri meetodit: lisa `return` ja paranda tagastustüüp.

```java
public TagastatavTüüp meetodiNimi(@PathVariable TeeMuutujaTüüp teeMuutuja, @RequestBody SisendDtoTüüp sisendDto) {
    return teenuseMuutuja.meetodiNimi(teeMuutuja, sisendDto);
}
```

> **IntelliJ vihje:** Alt+Enter → "Change return type" parandab signatuuri automaatselt.

---

## Samm 7 — kood ilusaks (refactor)

### Make it work → Make it beautiful

Kui kood töötab, vaata, kas saad selle puhtamaks teha. Selles taskis on service meetodis mitu järjestikust kontrolli. Iga kontroll on hea kandidaat eraldi helper meetodiks (nt `getValid...`, `validate...`).

**Extract Method IntelliJ'ga:**

Märgi service meetodis koodilõik → paremklõps → Refactor → Extract Method.

> **Tähelepanu:** IntelliJ kasutab ekstraktimisel tihti kogu objekti parameetrina.
> Kontrolli, kas helper meetod vajab tervet objekti või ainult üht välja.

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

Pärast (parem, sest edasi antakse ainult vajalik):
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

1. `public` meetodid enne
2. `private` meetodid pärast
3. Järjesta ka väljakutsumise hierarhia järgi: peameetod üleval, helper meetodid all

---

## Kokkuvõte ja kontrollnimekiri

Enne kui pead koodi valmis, kontrolli läbi:

- [ ] `TrainingController`-is on uus `@PostMapping` meetod õige teega, `@PathVariable` ja `@RequestBody` parameetritega
- [ ] Kontrolleri meetodil on `@Operation` ja `@ApiResponses` (200, 403, 404)
- [ ] `TrainingDateRegisterRequestDto` ja `TrainingRegisterResponseDto` on olemas paketis `controller/training/dto/`
- [ ] Olematu `trainingDateId` viskab `PrimaryKeyNotFoundException`-i väljanimega `trainingDateId`
- [ ] Kolm uut veakoodi on `Error` enumis ja neid visatakse 403-ga
- [ ] `UserTraining` jaoks on repository olemas ja see laiendab `JpaRepository`-t
- [ ] Uutel repository meetoditel on `@Query` Named parameters stiilis
- [ ] Mapperi kasutamisel: iga target-väli on kas `source` või `ignore = true`
- [ ] Service meetodil on `@Transactional` ja `training_date` rida on race condition'i vastu lukustatud
- [ ] Õnnestumise korral luuakse üks `user_training` kirje ja `user_count` kasvab ühe võrra
- [ ] Meetodite järjekord: `public` enne, `private` pärast, väljakutsumise hierarhia järgi
- [ ] Kood kompileerub ja endpoint on Swagger UI-s nähtav

---

> **Järgmine samm:** Testi endpointi Swagger UI kaudu (`http://localhost:8080/swagger-ui/index.html`).
> Näidisandmetega: `trainingDateId = 1`, `userId = 3` peaks esimesel korral õnnestuma ja teisel korral andma `ALREADY_REGISTERED`.
> Proovi ka `trainingDateId = 999` (404) ja mõnda kasutajat, kes pole grupi 1 liige (403 `NOT_TRAINING_GROUP_MEMBER`).
