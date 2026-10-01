# Juhend: POST /api/trainings

**Taski fail:** `Treeningu-lisamine.md`
**Kontroller:** `TrainingController.java` (olemas, pakett `ee.sportclub.controller.training`)
**Implementeerimise voog:** RestController → Service → Mapper → Repository → Service → RestController

---

## Sissejuhatus

Selle endpointiga saab treener luua uue treeningu. Ühe päringuga tekib üks `training` kirje (treeningu "mall") ja selle põhjal kõik toimumiskorrad (`training_date` read) valitud perioodis ja nädalapäevadel. Teekond läbib kontrolleri, service'i, mapperi ja kaks repositooriumi. Selle harjutuse käigus õpid sisendi DTO-d entity-ks mappima, mitut seotud kirjet ühes transaktsioonis salvestama ja kuupäevadega tsüklis töötama (`LocalDate`, `DayOfWeek`).

---

## Samm 1 — RestController

### Mida teha?

`TrainingController.java` on juba olemas ja sinna on lisatud mitu treeninguga seotud endpointi. Uut kontrollerit pole vaja luua, lisa olemasolevasse klassi uus meetod.

> **Pane tähele:** Selles projektis **ei kasutata** klassitasemel `@RequestMapping("/api")`-d. Iga meetodi mappingannotatsioonis on kogu tee koos `/api` eesliitega. Vaata olemasolevaid meetodeid ja järgi sama mustrit.

### Meetodi loomine

Alusta meetodist **ilma mappingannotatsioonideta**:

```java
public void meetodiNimi(SisendDtoTüüp sisendDto) {
    // tühi meetod esialgu
}
```

> **Mõtle:** Mis on selle meetodi hea nimi? See peaks ütlema, mida meetod teeb, nagu teistel meetoditel samas klassis.

Sisendiks on request body DTO. Taskis on selle nimi `TrainingCreateRequestDto`, aga seda klassi **veel pole**. Kui kirjutad tüübi parameetrisse, läheb see punaseks. Siis vajuta **Alt+Enter** → **Create class**. Kontrolli, et klass läheks paketti `controller.training.dto`.

DTO väljad leiad taskifaili "Sisend" tabelist. Mõtle iga välja juures läbi kaks asja:
- **Tüüp:** milline Java tüüp sobib kellaajale ja milline kuupäevale? Vihje: vaata, mis tüüpe kasutab `UpdateTrainingDateRequestDto`.
- **Valideerimine:** millised väljad on kohustuslikud ja millistel peab väärtus olema > 0? Vaata, milliseid `jakarta.validation` annotatsioone kasutavad teised DTO-d projektis.

Seejärel lisa meetodile:
1. **Mappingannotatsioon:** `@PostMapping`
2. **Parameetri annotatsioonid:** `@RequestBody` ja `@Valid`
3. **Swagger annotatsioonid:** `@Operation` ja `@ApiResponses`. Vaata taskist "Veaolukorrad" tabelit: milliseid koode (404, 403, 400) tuleb dokumenteerida?

> **Mõtle enne tagastustüübi valimist:** Taskifaili järgi on vastus `200 OK` **ilma body-ta**. Mida see meetodi tagastustüübi kohta ütleb?

### Service klassi ettevalmistus

Service klass `TrainingService` on juba olemas ja kontrolleris on juba olemas ka väli, mis sellele viitab. Kutsu kontrollerist service meetodit:

```java
public void meetodiNimi(SisendDtoTüüp sisendDto) {
    teenuseMuutuja.meetodiNimi(sisendDto);
}
```

> **IntelliJ vihje:** Kui meetodi nimi on punane, vajuta **Alt+Enter** → **"Create method in TrainingService"**.

---

## Samm 2 — Service

### Mida teha?

Nüüd liigud `TrainingService` klassi äsja loodud meetodisse. Sisse tuleb kogu vormi info. Enne millegi loomist tuleb veenduda, et viidatud kirjed on olemas ja treeneril on õigus seda teha.

Vaata taskifailist "Teenuse loogika" samme 1–3 ja mõtle läbi:

- **Millised kolm id-d** tuleb kontrollida, et need oleksid andmebaasis olemas?
- Klassis on juba muster `getValid...By(...)`, mis tagastab entity või viskab `PrimaryKeyNotFoundException`. Kas mõnda neist saab kasutada otse? Milliste jaoks tuleb samasugune meetod juurde teha?

> **Tähelepanu:** Projektis on `TrainingGroupRepository` juba olemas, aga **`Facility` ja `Training` entity jaoks repositooriumi veel pole**. Kui kirjutad service'is `facilityRep...`, ei paku IntelliJ midagi. Loo uus interface paketti `persistence.facility` (Alt+Enter või JPA Buddy), mis laiendab `JpaRepository`-t.

Kui repository meetod midagi tagastab, **pane tulemus kohe muutujasse**. Neid entity'sid läheb hiljem vaja.

### Omandiõiguse kontroll

Taskis on nõue, et treeninggrupp peab kuuluma just sellele treenerile, kes päringu tegi.

> **Mõtle:** Sul on juba olemas treeninggrupi entity. Kas kontrolliks on üldse vaja uut repository päringut, või on vajalik info juba entity küljes olemas?

Kui tingimus ei kehti, viska `ForbiddenException` uue veakoodiga `NOT_TRAINING_GROUP_TRAINER`. Selle peab enne lisama `Error.java` enumisse. Vaata, kuidas teised `ForbiddenException`-id service'is visatakse.

---

## Samm 3 — Mapper (sisendi teisendamine)

### Mida teha?

Enne salvestamist tuleb request DTO teisendada `Training` entity-ks.

### Mapper meetodi loomine

Selles projektis asuvad mapperid `persistence` paketis entity kõrval (nt `TrainingGroupMapper`, `TrainingDateMapper`). `Training` entity jaoks mapperit **veel pole**. Mõtle, kas lood uue `TrainingMapper`-i või lisad meetodi mõnda olemasolevasse mapperisse. Mis oleks loogilisem?

Uue mapperi saad luua JPA Buddy abil (paremklõps `Training` entity'l → New → DTO → MapStruct Interface väljas loo uus plussmärgiga) või käsitsi. Vaata olemasolevast mapperist, milline on `@Mapper(...)` annotatsiooni kuju.

Nimeta meetod konventsiooni järgi:

```java
EntiteetTüüp toEntiteetKlassiNimi(SisendDtoTüüp dto);
```

> **IntelliJ vihje:** Kliki `target = ""` jutumärkide vahele ja vajuta **Ctrl+Space**. IntelliJ näitab kõiki entity välju. Nii näed kohe, mitu `@Mapping` rida vaja on.

Lisa `@Mapping` annotatsioon **iga** entity välja kohta:

```java
@Mapping(ignore = true, target = "id")
@Mapping(source = "dtoVäli", target = "entiteediVäli")
@Mapping(ignore = true, target = "väliMisDtostPuudub")
EntiteetTüüp toEntiteetKlassiNimi(SisendDtoTüüp dto);
```

> **Mõtle iga välja juures:**
> - `id` on loomisel alati `ignore = true`.
> - **Seotud objektid** (treeninggrupp, asukoht): DTO-s on ainult id, entity vajab aga tervet objekti. Kust see objekt tuleb? Mapper ei pääse andmebaasi ligi.
> - **Väljad, mida DTO-s üldse pole**, aga entity nõuab (nimi, lõpuaeg): need arvutatakse service'is. Taskifailist leiad, kuidas.
> - **Nimed, mis ei kattu**: DTO-s ja entity-s võib sama asja nimi olla erinev. Kirjuta `source` ka siis, kui nimi kattub.

---

## Samm 4 — Repository

### Mida teha?

Salvestada on vaja **kahte** sorti kirjeid: üks `Training` ja mitu `TrainingDate`-i.

- `TrainingDateRepository` on juba olemas.
- `Training` jaoks repositoorium puudub. Loo see samamoodi nagu Samm 2-s `Facility` jaoks.

> **Rusikareegel:** Lihtsa loomise puhul piisab `JpaRepository` baasmeetoditest. Uut `@Query` päringut pole selles taskis tõenäoliselt üldse vaja. Kas leiad baasmeetodi, mis salvestab terve listi korraga?

---

## Samm 5 — tagasi Service'i

### Mida teha?

See on taski kõige sisukam osa. Service meetod paneb kõik tükid kokku.

**5a. Training kirje:**

Kutsu välja mapper ja määra entity-le service'is need väljad, mis mapperis jäid `ignore = true`.

```java
EntiteetTüüp entiteet = mapperMuutuja.toEntiteetKlassiNimi(sisendDto);
entiteet.setSeotudObjekt(eelnevaltLeitudObjekt);
// ... ülejäänud ignore-väljad
```

> **Mõtle:** Kuidas saab `LocalTime` objektile minuteid juurde liita? Proovi `startTime.` järel **Ctrl+Space**. IntelliJ näitab kõiki meetodeid, mis `plus...` algavad.

**5b. TrainingDate read:**

Nüüd tuleb käia läbi kõik kuupäevad alguskuupäevast lõppkuupäevani ja iga sobiva nädalapäeva kohta luua uus `TrainingDate` objekt. Mõtle läbi:

1. **Kuidas käia kuupäevad tsüklis läbi?** `LocalDate`-l on meetodid, mis algavad `plus...`, `isAfter`, `isBefore` ja `datesUntil`. Uuri neid **Ctrl+Space**-iga.
2. **Kuidas teada, mis nädalapäev on mingi kuupäev?** `LocalDate`-lt saab küsida `DayOfWeek`-i.
3. **Kuidas siduda `"E,N"` string `DayOfWeek` väärtustega?** Mõtle, kas see teisendus võiks olla eraldi väike helper meetod.
4. **Kust tulevad väärtused** `status`, `userCount` ja `dateAdded` jaoks? Taskifailis on need kirjas. Staatuse jaoks on projektis olemas `Status` enum.

Kogu genereeritud read listi ja salvesta need korraga.

> **Meetodi palve:** Kui kutsud välja meetodi, mis midagi tagastab, ja tahad selle infoga edasi töötada, siis pane tulemus kohe muutujasse. See kehtib ka `save()` puhul. Kas `TrainingDate` vajab viidet **salvestatud** `Training`-ule?

**5c. Veaolukorrad ja transaktsioon:**

- Kui list jääb tühjaks, tuleb visata `NO_TRAINING_DATES`. Mõtle: **mis järjekorras** peaksid asjad toimuma, et tühja listi korral ei jääks andmebaasi üksildast `training` kirjet?
- Kuidas tagada, et kõik salvestused kas õnnestuvad või ebaõnnestuvad koos? Vaata, millise annotatsiooniga on samas klassis kaitstud teised kirjutavad meetodid.
- `endDate < startDate` ja vigase `weekdays` kontroll peavad tagastama **400 `INCORRECT_INPUT`**. Selle koodi annab `RestExceptionHandler.handleMethodArgumentNotValid` ainult siis, kui DTO valideerimine (`@Valid`) kukub läbi. Service'ist visatud `ForbiddenException` annaks 403. Seepärast peavad need kontrollid olema **DTO valideerimisannotatsioonid** (vt `jakarta.validation.constraints`, nt `@Pattern`, `@AssertTrue`). `INCORRECT_INPUT`-i ei pea `Error.java`-sse lisama.

---

## Samm 6 — tagasi RestController'isse

### Mida teha?

Service meetod on valmis. Mine tagasi kontrolleri meetodisse ja kontrolli:

- Kas tagastustüüp vastab Samm 1 otsusele? (200, body puudub)
- Kas `@ApiResponses` kirjeldab kõiki veaolukordi, mis service'is nüüd tegelikult visatakse, sh uued `NOT_TRAINING_GROUP_TRAINER` ja `NO_TRAINING_DATES`?
- Kas `@Operation` summary kirjeldab täpselt, mida endpoint teeb? Ära unusta, et see loob ka toimumiskorrad.

---

## Samm 7 — kood ilusaks (refactor)

### Make it work → Make it beautiful

Kui kood töötab, vaata, kas seda saab puhtamaks muuta. Selles taskis on service meetod tõenäoliselt pikk, seega on siin hea võimalus harjutada.

**Extract Method IntelliJ'ga:**

Märgi service meetodis koodilõik, mida soovid eraldada helper meetodiks → paremklõps → Refactor → Extract Method.

Head kandidaadid: omandiõiguse kontroll, `Training` objekti ettevalmistamine, toimumiskordade genereerimine, nädalapäevade teisendus.

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
3. Järjesta ka väljakutsumise hierarhia järgi: peameetod üleval, helper meetodid all. Selleks saab kasutada ka skilli `/skill-jarjesta-meetodid`.

---

## Kokkuvõte ja kontrollnimekiri

Enne kui koodi valmis kuulutad, kontrolli läbi:

- [ ] `TrainingController`-isse on lisatud uus `@PostMapping("/api/trainings")` meetod
- [ ] Kontrolleri meetodil on `@Operation` ja `@ApiResponses` annotatsioonid, mis katavad 404, 403 ja 400 juhud
- [ ] `TrainingCreateRequestDto` asub paketis `controller.training.dto` ja sellel on valideerimisannotatsioonid
- [ ] `Error.java` sisaldab uusi väärtusi `NOT_TRAINING_GROUP_TRAINER` ja `NO_TRAINING_DATES`
- [ ] `Facility` ja `Training` repositooriumid on olemas ja laiendavad `JpaRepository`-t
- [ ] Mapper interface on olemas `@Mapper(componentModel = ...)` annotatsiooniga, nagu teistel mapperitel projektis
- [ ] Kõik `@Mapping` annotatsioonid on täidetud: iga entity väli on kas `source` või `ignore = true`
- [ ] Service meetod on `@Transactional` ja tühja toimumiskordade listi korral ei looda ühtegi kirjet
- [ ] Meetodite järjekord: `public` enne, `private` pärast, järjestatud väljakutsumise hierarhia järgi
- [ ] Kood kompileerub ja endpoint on Swagger UI-s nähtav

---

> **Järgmine samm:** Testi endpointi Swagger UI kaudu (`http://localhost:8080/swagger-ui/index.html`) taskifaili näidispäringuga (`trainerId = 6`, `trainingGroupId = 2`, `facilityId = 1`, `"E,N"`, `2026-10-05..2026-10-15`).
> Kontrolli `psql`-iga, et tekkis üks `training` rida ja neli `training_date` rida (05.10, 08.10, 12.10, 15.10).
> Proovi ka `trainerId = 5`-ga: tulemuseks peab olema 403 `NOT_TRAINING_GROUP_TRAINER`.
