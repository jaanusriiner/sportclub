# Juhend: GET /api/users/{userId}/trainings

**Taski fail:** `Minu-treeningute-nimekirja-paring.md`
**Kontroller:** `UserController.java` (uus, pakett `controller/user/`)
**Implementeerimise voog:** RestController → Service → Repository → Service → Mapper → RestController

---

## Sissejuhatus

See endpoint tagastab ühe kasutaja tulevaste registreeritud treeningute nimekirja, millega `TrainingsView.vue` täidab sektsiooni "Minu Grupid ja Treeningud". Päring käib läbi kontrolleri, service'i ja repository ning tagastab `MyTrainingDto` objektide listi (või tühja listi, kui registreeringuid pole).

Selle harjutuse käigus õpid:
- kuidas luua uus kontroller uude ressursipaketti
- kuidas koostada listi tagastav päring, mis filtreerib nii kasutaja kui ka aja järgi
- kuidas otsustada, kas DTO täidetakse mapperiga või otse päringus, kui DTO väljad tulevad mitmest tabelist

---

## Samm 0 — Mis on juba olemas, mis puudub?

### Mida teha?

Suur osa vajalikust on eelmistest taskidest olemas. Vaata üle:

- `persistence/user/UserTraining.java` ja `UserTrainingRepository.java` on olemas
- `persistence/training/` all on `TrainingDate`, `Training`, `TrainingGroup` ja vaate-entiteet `TrainingDateOverview` koos repositooriumiga
- `service/training/TrainingService.java` on olemas, seal on ka kasutaja leidmise meetod
- `persistence/profile/Profile.java` ja `ProfileRepository.java` on olemas

Puudu on:

- kontroller ja selle pakett `controller/user/`
- DTO `MyTrainingDto`
- päring, mis leiab ühe kasutaja tulevased registreeringud

> **Mõtle:** Implementatsiooniplaanis (`Minu-treeningute-nimekirja-paring-IMPLEMENTATSIOON.md`) on paketiks kokku lepitud `controller/user/`, sest URL algab ressursiga `/api/users/...`. Kas selline pakett on projektis juba olemas?

---

## Samm 1 — RestController

### Mida teha?

Selle endpointi jaoks tuleb luua **uus kontroller**. Olemasolevad kontrollerid (`area`, `login`, `register`, `training`) seda ressurssi ei kata.

- Kaust: `backend/src/main/java/ee/sportclub/controller/user/` (uus pakett)
- Klass: `UserController`
- Loo IntelliJ'ga: paremklõps `controller` paketil → New → Java Class, nimeks `user.UserController` (IntelliJ loob paketi ja klassi korraga)

> **Tähelepanu:** Selles projektis ei kasutata klassi tasemel `@RequestMapping("/api")`, vaid **kogu tee** kirjutatakse mappingannotatsiooni sisse. Vaata `TrainingController`-it ja järgi sama mustrit.

Vajalikud klassiannotatsioonid:

```java
@RestController
@RequiredArgsConstructor
public class KontrolleriKlass {
    // ...
}
```

### Meetodi loomine

Alusta meetodist **ilma mappingannotatsioonideta**:

```java
public void meetodiNimi(SisendTüüp parameetriNimi) {
    // tühi meetod esialgu
}
```

> **Mõtle:** Mis on selle meetodi hea nimi? Nimi peaks kirjeldama, mida meetod teeb.
> Vaata HTTP meetodit ja API teed taskifailist, need annavad vihje.

Seejärel lisa:
1. **Mappingannotatsioon** `@GetMapping` koos taskifailis toodud teega
2. **Parameetri annotatsioon**: sisend tuleb URL-i seest. Kas see on `@PathVariable` või `@RequestParam`?
3. **Swagger annotatsioonid** `@Operation` ja `@ApiResponses`. Lisa taskifaili vastusekoodid (200, 404) ja vea juurde `content = @Content(schema = @Schema(implementation = ApiError.class))`

```java
@GetMapping("/mingi/{teeMuutuja}/rada")
@Operation(summary = "Lühikokkuvõte")
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Millal see tekib",
                content = @Content(schema = @Schema(implementation = VeaKlass.class)))})
public void meetodiNimi(@PathVariable TeeMuutujaTüüp teeMuutuja) {
}
```

### Service'i väljakutse

Uut service klassi pole vaja. Implementatsiooniplaani järgi läheb loogika olemasolevasse `TrainingService` klassi ("üks teenusklass domeeni kohta").

Lisa service muutuja kontrolleri klassi:

```java
private final TeenusKlass teenuseMuutuja;
```

Kutsu service meetodit välja (esialgne tühi väljakutse):

```java
public void meetodiNimi(TeeMuutujaTüüp teeMuutuja) {
    teenuseMuutuja.meetodiNimi(teeMuutuja);
}
```

> **IntelliJ vihje:** Kui `teenuseMuutuja.meetodiNimi(...)` on punasega alla joonitud,
> vajuta **Alt+Enter** punasel joonel → vali **"Create method in TeenusKlass"**.

---

## Samm 2 — Service ja esimene repository päring

### Mida teha?

Nüüd liigud `TrainingService` klassi äsja loodud meetodisse. Sisse tuleb kasutaja id. Meetodil on kaks ülesannet: kontrollida, et kasutaja on olemas, ja leida tema tulevased registreeringud.

### Kasutaja kontroll

Taskifaili "Veaolukorrad" tabel nõuab, et tundmatu `userId` annaks 404 ja `PRIMARY_KEY_NOT_FOUND`.

> **Mõtle:** Kas selle jaoks on `TrainingService`-is juba meetod olemas? Kas selle tagastatud objekti läheb sul siin edasi vaja, või piisab sellest, et meetod vea korral erindi viskab?

### Registreeringute päring

Mõtle: **millisest tabelist** saad teada, millistele treeningutele kasutaja on registreerunud? Vaata taskifaili sektsiooni "Seotud andmebaasi tabelid".

Alusta repositooriumi muutuja nime kirjutamist service meetodis:

```java
public void meetodiNimi(SisendTüüp parameetriNimi) {
    entiteetRep  // <- kirjuta algus siia
}
```

> **IntelliJ vihje:** Kui repositoorium on klassis juba väljana olemas, pakub IntelliJ seda **Ctrl+Space** abil. Kui pole, siis kirjuta nime algus ja vajuta **Tab**.

**Küsi endalt:** Kas JPA pakub valmis meetodit, mis leiab read kasutaja id järgi?

> **Rusikareegel:** Kui päringusse läheb sisendina muu väärtus kui tabeli enda `id`,
> on tõenäoliselt vaja **uut meetodit** teha (vt Samm 5 "Uue meetodi loomine JPA Buddy abil").

Päringul on kolm nõuet, mõtle need enne kirjutamist läbi:

1. **Kelle read?** Ainult antud kasutaja registreeringud.
2. **Millised read?** Ainult need, mille treening toimub praegusest hetkest hiljem. Nii kuupäev kui ka kellaaeg loevad. Vaata, kuidas `TrainingDateRepository.findTrainingBy` "tulevikus" tingimuse kirja paneb.
3. **Mis järjekorras?** Nimekiri peaks olema kasutajale loogilises järjestuses.

Kui repository meetod midagi tagastab, **pane tulemus kohe muutujasse**.

---

## Samm 3 — DTO klass

### Mida teha?

Loo väljundi DTO. Taskifail annab nime (`MyTrainingDto`) ja "Väljund" sektsiooni JSON näidis annab kõik üheksa välja.

- Kaust: `backend/src/main/java/ee/sportclub/controller/user/dto/`
- Vaata olemasolevat `TrainingGroupOverviewDto`-d, milliseid Lomboki annotatsioone siin projektis DTO-del kasutatakse

Selle DTO väljad tulevad **mitmest tabelist** (`training_date`, `training_group`, `sport`, `facility`, `profile`), seega JPA Buddy ühe-entity generaator ei anna õiget tulemust. Loo klass käsitsi JSON näidise järgi.

> **Mõtle iga välja tüübi peale:**
> - Millised väljad on arvud, millised tekstid?
> - `nextTrainingDate` ja `nextTrainingTime`: mis Java tüübid sobivad kuupäevale ja kellaajale? Vaata, mida kasutab `TrainingDate` entiteet.
> - JSON näidises on kellaaeg kujul `"19:30"`. Kontrolli hiljem Swaggeris, mis kujul sinu väli välja tuleb.

---

## Samm 4 — Mapper ja ülejäänud andmete kogumine

### Mida teha?

Nüüd tuleb päringu tulemus viia `MyTrainingDto` kujule. Enne mapperi kirjutamist vaata taskifaili "Väljade tähendus" nimekirja ja käi iga väli läbi: kas sellele pääseb ligi `UserTraining` entiteedist seoseid mööda?

> **Mõtle:**
> - `sportName`, `facilityName`, `userCount`: kas nendeni jõuab getteritega (`...getTrainingDate().get...`)?
> - `trainerName`: taskifaili järgi tuleb see `profile` tabelist. Kas `TrainingGroup` → `User` kaudu jõuab `Profile`-ni? Vaata, kummal pool seos on: kas `User` viitab `Profile`-le või `Profile` `User`-ile?
> - Kuidas lahendati sama `trainerName` küsimus eelmises taskis ("Treeningute nimekirja päring")? Vaata `TrainingDateOverview` entiteeti ja `TrainingDateOverviewRepository` päringut.

Siit on kaks võimalikku teed. Vali üks ja oska valikut põhjendada.

### Tee A — entiteedid + mapper

Päring tagastab entiteetide listi, mapper teeb neist DTO-d ja üle jääv väli täidetakse service'is.

Loo mapper interface (`@Mapper(componentModel = "spring")`) entiteediga samasse paketti. Vaata eeskujuks `TrainingDateMapper`-it.

Nimeta meetodid konventsiooni järgi:

```java
// Ühele DTO-le
TagastatavDtoTüüp toDtoKlassiNimi(EntiteetTüüp entiteet);

// Lista DTO listiks
List<TagastatavDtoTüüp> toDtoKlassiNimid(List<EntiteetTüüp> entiteedid);
```

`@Mapping` annotatsioonid käivad **ainult üksiku objekti meetodile**. List-meetod jääb annotatsioonideta, MapStruct genereerib selle ise ja kutsub iga elemendi kohta üksiku objekti meetodit.

Lisa `@Mapping` annotatsioonid:

```java
@Mapping(source = "", target = "")
TagastatavDtoTüüp toDtoKlassiNimi(EntiteetTüüp entiteet);
```

> **IntelliJ vihje:** Kliki `target = ""` jutumärkide vahele → vajuta **Ctrl+Space**.
> IntelliJ näitab, mitu välja DTO-l on. Nii saad luua ettevalmistatud `@Mapping` malli, üks rida iga target-välja kohta.

Täida kõik read. **Iga target-väli peab olema eksplitsiitselt kirjas**, kas `source`-iga (ka siis, kui nimi kattub) või `ignore = true`-ga:

```java
@Mapping(source = "seotudObjekt.id", target = "seotudObjektiId")
@Mapping(source = "tavaveerg", target = "samaNimiDtos")
@Mapping(ignore = true, target = "väliMidaEntiteetEiKata")
TagastatavDtoTüüp toDtoKlassiNimi(EntiteetTüüp entiteet);
```

> **Väljad, mida entiteet ei kata** (`ignore = true`) → täida need service meetodis pärast mappimist täiendava repository päringu tulemusel. Mõtle, kuidas vältida eraldi päringut iga rea kohta (N+1 probleem). `ProfileRepository`-s on juba meetod, mis võtab korraga mitu id-d.

### Tee B — päring projekteerib otse DTO-sse

`backend/CLAUDE.md` jaotis "SQL päringud" ütleb: "Vajadusel kasutab repositoorium konstruktori avaldist otse DTOsse projekteerimiseks." Nii tehti eelmises taskis (`TrainingDateOverviewRepository`).

Sel juhul mapperit ei ole vaja: päring ise tagastab `List<TagastatavDtoTüüp>`.

> **Mõtle:**
> - Konstruktori avaldis (`select new täis.paketi.nimi.DtoKlass(...)`) kutsub DTO konstruktorit. Milline Lomboki annotatsioon selle konstruktori tekitab, ja mis määrab argumentide **järjekorra**?
> - Kust tuleb treeneri nimi ühe väljana? Vaata, mida `v_training_date_overview` vaade (`2_create.sql`) juba kokku paneb.
> - Kuidas siduda vaate read ühe kasutaja registreeringutega? Eelmise taski päringus on vihje, kuidas `UserTraining` ja vaade omavahel kokku käivad.

### Service meetodi lõpetamine

Kumma tee ka valid, service meetodi lõpp on sama kujuga: tulemus muutujasse ja `return`.

```java
public void meetodiNimi(SisendTüüp parameetriNimi) {
    // kasutaja kontroll
    List<TagastatavDtoTüüp> dtod = ...;
    return dtod;
}
```

> **IntelliJ vihje:** Meetodi tagastustüüp on veel `void`, aga `return` on sees.
> Vajuta **Alt+Enter** punase joone peal → IntelliJ parandab tagastustüübi automaatselt.

> **Mõtle:** Mis juhtub, kui kasutajal pole ühtegi tulevast registreeringut? Taskifail ootab tühja massiivi `[]` ja staatust 200. Kas pead selle jaoks midagi eraldi kirjutama?

---

## Samm 5 — Repository (päringu loomine)

### Mida teha?

See samm kirjeldab, kuidas Samm 2 ja Samm 4 juures läbi mõeldud päring päriselt luua. Päring kuulub sellesse repositooriumisse, mille entiteedist ta alustab.

### Uue meetodi loomine JPA Buddy abil

Mine repository interface'i faili. Kasuta **JPA Buddy** funktsionaalsust:

1. Ava JPA Buddy paneel (paremklõps repository klassis → JPA Buddy)
2. Valikutes **Method** ja **Query** vali → **Query**
3. Vali meetodi tüüp: **Find collection** (mitme rea leidmiseks)
4. Määra **Wrap type**: `List`
5. Lisa **query conditionid**: milliseid välju filtreeritakse?
6. **Advanced** sektsioonis: vali **Named parameters**
7. Mõtle läbi **Order By Attributes**

Kui JPA Buddy dialoog ei reageeri, kirjuta `@Query` ja meetodi signatuur käsitsi. Eeskujuks sobivad projekti olemasolevad repository päringud.

Peale meetodi loomist:
- Kontrolli parameetrite nimed. Ebamäärane `id` asenda konkreetsemaga ja tee sama muudatus `@Query` sees
- Eemalda ebavajalikud `@Param()` annotatsioonid ja kasutamata importid (**Ctrl+Alt+O**)
- Meetodi nimi peab ütlema, **mida ta tagastab** (vt `backend/CLAUDE.md` "Repositooriumi meetodi nimetamine")
- "Tulevikus" tingimus tuleb JPA Buddy genereeritud päringusse tõenäoliselt käsitsi lisada

### Optional käsitlemine

Listi tagastav päring `Optional`-it ei vaja: kui ridu pole, on tulemus tühi list. `Optional` tuleb mängu ainult kasutaja leidmisel id järgi ja see on olemasolevas `getValid...` meetodis juba `orElseThrow`-ga käsitletud.

---

## Samm 6 — tagasi RestController'isse

### Mida teha?

Service meetod tagastab nüüd DTO-de listi. Täienda kontrolleri meetodit: lisa `return` ja paranda tagastustüüp.

```java
public void meetodiNimi(@PathVariable TeeMuutujaTüüp teeMuutuja) {
    teenuseMuutuja.meetodiNimi(teeMuutuja);  // <- enne: tulemus kasutamata
}
```

> **IntelliJ vihje:** Lisa `return` lause.
> IntelliJ kurdab, et `void` ei saa midagi tagastada. Vajuta **Alt+Enter** → "Change return type".

Tulemus:

```java
public List<TagastatavTüüp> meetodiNimi(@PathVariable TeeMuutujaTüüp teeMuutuja) {
    return teenuseMuutuja.meetodiNimi(teeMuutuja);
}
```

---

## Samm 7 — kood ilusaks (refactor)

### Make it work → Make it beautiful

Kui kood töötab, on aeg vaadata, kas saab koodi puhtamaks muuta.

**Extract Method IntelliJ'ga:**

Märgi service meetodis koodilõik, mida soovid eraldada helper meetodiks → paremklõps → Refactor → Extract Method.

> **Tähelepanu:** IntelliJ kasutab ekstraktimisel kogu objekti parameetrina.
> Vaata üle, kas helper meetod vajab tegelikult kogu objekti või ainult üht välja, ja tee vajadusel korrektuur.

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

Selles taskis on service meetod tõenäoliselt lühike. Vaata siiski üle:
- kas muutujate nimed peegeldavad täistüüpi (`MyTrainingDto myTrainingDto`, mitte `dto`)
- kas kasutaja leidmise meetodi nimi järgib konventsiooni `getValid<Entiteet>By(...)`

### Meetodite järjekord

Kontrolli meetodite järjekorda vastavalt Java konventsioonile:
1. `public` meetodid enne
2. `private` meetodid pärast
3. Järjesta ka väljakutsumise hierarhia järgi: peameetod üleval, helper meetodid all

---

## Kokkuvõte ja kontrollnimekiri

Enne kui pead koodi valmis, kontrolli läbi:

- [ ] `UserController` on paketis `controller/user/` annotatsioonidega `@RestController` ja `@RequiredArgsConstructor`
- [ ] Kontrolleri meetodil on `@GetMapping` õige teega, `@PathVariable`, `@Operation` ja `@ApiResponses` (200, 404)
- [ ] `MyTrainingDto` on paketis `controller/user/dto/` ja sellel on kõik üheksa välja taskifaili järgi
- [ ] Tundmatu `userId` annab 404 ja `PRIMARY_KEY_NOT_FOUND` väljanimega `userId`
- [ ] Päring tagastab ainult antud kasutaja registreeringud
- [ ] Päring tagastab ainult tulevikus toimuvad treeningud (kuupäev ja kellaaeg koos)
- [ ] Registreeringuteta kasutaja saab 200 ja tühja massiivi `[]`
- [ ] Repository meetodil on `@Query` Named parameters stiilis ja nimi ütleb, mida meetod tagastab
- [ ] Mapperi kasutamisel: `@Mapper(componentModel = "spring")`, iga target-väli on kas `source` või `ignore = true`, list-meetodi nimi on mitmuses
- [ ] `trainerName` on kujul "Eesnimi Perekonnanimi"
- [ ] Meetodite järjekord: `public` enne, `private` pärast, väljakutsumise hierarhia järgi
- [ ] Kood kompileerub ja endpoint on Swagger UI-s nähtav
- [ ] Automaattestid on kirjutatud (vt taskifaili viimast vastuvõtu kriteeriumi)

---

> **Järgmine samm:** Testi endpointi Swagger UI kaudu (`http://localhost:8080/swagger-ui/index.html`).
> Näidisandmetega: `userId = 3` peaks tagastama vähemalt `training_date.id = 1` registreeringu (Tennis, Laagri Tennisekeskus, Jaana Kask).
> Proovi ka `userId = 99` (404) ja kasutajat, kellel pole ühtegi registreeringut (200 ja `[]`).
