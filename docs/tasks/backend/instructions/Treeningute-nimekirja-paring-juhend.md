# Juhend: GET /api/trainings

**Taski fail:** `Treeningute-nimekirja-paring.md`
**Kontroller:** `TrainingController.java`
**Implementeerimise voog:** RestController → Service → Repository → Service → Mapper → RestController

---

## Sissejuhatus

See endpoint tagastab "Treeningud" tabeli sisu — kõigi treeninggruppide nimekirja koos iga grupi järgmise toimuva treeninguga ja infoga, kas päritud kasutaja on selle grupi liige. See on selle vaate neljast teenusest kõige mahukam, kuna sellega loome ka mitu uut andmebaasi entiteeti, mida hiljem taaskasutavad ülejäänud kolm teenust ("Minu treeningud", "Registreeru", "Taotle Liitumist"). Õpid siin nii mitme seotud entiteedi ülesehitamist kui ka mitme tabeli liitepäringu tegemist ja tulemuse töötlemist service kihis.

---

## Samm 0 — Entiteedid ja seosed

### Mida teha?

Enne kontrolleri juurde minekut vajame JPA entiteete tabelite jaoks, mida see teenus kasutab. Vaata taski failist sektsiooni "Seotud andmebaasi tabelid" ja `docs/database/2_create.sql`-st struktuuri.

Vajalikud uued entiteedid (kaustas `backend/src/main/java/ee/sportclub/persistence/`):

- `facility`
- `sportclub`
- `skill_level`
- `training_group`
- `training`
- `training_date`
- `user_training_group`

Olemasolevad `Area`, `Profile`, `Sport`, `User` entiteedid on juba olemas — neid ei looda uuesti, ainult viidatakse neile seostes.

> **IntelliJ vihje:** Ava **Persistence** tool window (View → Tool Windows → Persistence) ja ühenda oma andmebaas, kui see pole veel ühendatud. Sealt saad tabelid valida ja **"Generate Persistence Mapping"** kaudu entiteedid genereerida — see säästab palju käsitsi kirjutamist. Kontrolli pärast genereerimist, et:
> - iga entiteet läheb enda paketti (nt `persistence/facility/Facility.java`, mitte kõik ühte kausta)
> - `schema = "sportclub"` on `@Table` annotatsioonil olemas (vaata olemasolevat `Area.java` eeskujuks)
> - foreign key veerud (nt `area_id`, `sport_id`) on genereeritud `@ManyToOne` seostena, mitte pelgalt `Integer` väljadena

Vaata olemasolevat `Profile.java` (`persistence/profile/`) eeskujuks, kuidas seosed (`@ManyToOne` + `@JoinColumn`) selles projektis kirja pannakse.

> **Mõtle:** `training.description`, `training_group.description`, `facility.description` on kõik andmebaasis nullable (`NULL` lubatud) — kas sinu entiteedi väljadel on ka vastavalt `@NotNull` puudu neil väljadel?

Kui kõik seitse entiteeti on loodud ja projekt kompileerub, liigu edasi.

Anna märku, kui oled valmis, või kui mõne seose juures kahtled!

---

## Samm 1 — RestController

### Mida teha?

Loo uus kontrolleri klass — seda veel pole.

- Kaust: `backend/src/main/java/ee/sportclub/controller/`
- Uus alampakett: `training`

```java
@RestController
@RequiredArgsConstructor
public class TrainingController {
    // ...
}
```

> Pane tähele — projekti olemasolevad kontrollerid (`AreaController`) ei kasuta klassitasandi `@RequestMapping`-ut, vaid annotatsiooni panevad otse meetodile täisteega. Vaata `AreaController.java` eeskujuks.

### Meetodi loomine

Alusta meetodist ilma mappingannotatsioonita:

```java
public void meetodiNimi(SisendTüüp parameetriNimi) {
    // tühi meetod esialgu
}
```

> **Mõtle:** Mis on hea nimi meetodile, mis pärib treeningute nimekirja? Vaata, kuidas `AreaController`-is on nimetatud (`findAreas`) — millise sarnase nime annaksid sina siin?

Taskifailis on kuus võimalikku sisendparameetrit — üks kohustuslik (`userId`) ja viis valikulist (`areaId`, `sportId`, `trainerId`, `date`, `time`). Kõik kuus on query parameetrid (mitte path variable, kuna `/api/trainings` teel pole path osasid).

Seejärel lisa:
1. **Mappingannotatsioon** — `@GetMapping`
2. **Parameetrite annotatsioonid** — kõik kuus on `@RequestParam`; mõtle, millised neist vajavad `required = false`
3. **Swagger annotatsioonid** — `@Operation` ja `@ApiResponses` (taski "Veaolukorrad" sektsioonist leiad, mis 404 stsenaarium kirja panna)

### Service klassi ettevalmistus

Kas service klass juba eksisteerib? Ei — loo uus:

- Kaust: `backend/src/main/java/ee/sportclub/service/`
- Uus alampakett: `training`

```java
@Service
@RequiredArgsConstructor
public class TrainingService {
    // ...
}
```

> See teenusklass kasvab selle vaate järgmiste taskide käigus veel — see on kavatsuslik (vt implementatsiooniplaani "üks teenusklass domeeni kohta" põhjendust), ei ole viga, kui see hiljem täieneb.

Lisa service muutuja kontrollerisse ja kutsu esialgu tühi meetod välja — kasuta IntelliJ **Alt+Enter** → "Create method in TrainingService" nippi, kui `trainingService.meetodiNimi(...)` on punasega alla joonitud.

Anna märku, kui oled selle sammuga valmis!

---

## Samm 2 — Service ja esimene repository päring

### Mida teha?

Enne kui repository poole liigud, mõtle: kellel selles teenuses on üldse õigust vastust saada? Taskifail ütleb, et olematu `userId` on veaolukord (404). See tähendab, et enne treeningute otsimist tuleb `userId` kehtivust kontrollida — sarnaselt sellele, kuidas `RegisterService.getValidArea(...)` kontrollib `areaId` kehtivust `orElseThrow`-ga.

> **Rusikareegel projektis:** `repository.findById()` + `orElseThrow` kuulub meetodisse nimega `getValid<Entiteet>By(...)` (vt backend/CLAUDE.md). Kas `UserRepository`-l on juba olemas meetod, mida siin kasutada saad?

Kirjuta service meetodisse algus, mis kontrollib `userId`-d, enne kui midagi muud teed.

### Treeningute päring

Nüüd tuleb kõige olulisem osa — treeninggruppide ja nende järgmiste toimumisaegade leidmine. Mõtle taski "Väljund" JSON näite peale: iga rida vajab andmeid **seitsmest** tabelist korraga (`training_group`, `sport`, `sportclub`, `skill_level`, `user`+`profile` treeneri jaoks, `training`, `training_date`, `facility`).

> **Rusikareegel:** kui päring vajab andmeid mitmest tabelist liidetuna, ja tulemus pole lihtsalt `findById()`, siis läheb vaja uut, kohandatud päringut repositooriumis (vt "Uue meetodi loomine JPA Buddy abil" allpool).

**Mõtle enne kirjutamist läbi kaks asja:**

1. **"Järgmine treening" grupi kohta** — igal treeninggrupil võib olla mitu tulevast `training_date` kirjet. Kuidas valid nende seast just selle, mis järgmisena toimub? Kas see peab juhtuma SQL/JPQL päringus endas, või võib repository tagastada kõik tulevased read ja "järgmise" valimine toimuda Java koodis (service kihis) pärast?
2. **Millised read üldse loetakse "tulevasteks"?** Vaata `training_date.start_date`/`start_time` ja mõtle, kuidas praeguse hetkega võrrelda (JPQL-is on selleks olemas `CURRENT_DATE`/`CURRENT_TIME`).

Kumbki lähenemine (kõik SQL-is vs. osa Java's) on korrektne — vali see, mis sulle endale selgem tundub. Kui valid teha "järgmise valimise" Java poolel, mõtle, milline andmestruktuur (nt `Map`) aitaks sul hõlpsalt öelda "iga trainingGroupId kohta ainult esimene rida".

> **Kui mõtled valikuliste filtrite (`areaId`, `sportId`, `trainerId`, `date`, `time`) peale:** JPQL-is saab kirjutada tingimuse kujul `(:parameeter IS NULL OR veerg = :parameeter)` — nii jääb filter rakendamata, kui parameetrit ei antud.

Alusta kirjutama repositooriumi muutuja nime service meetodis — vajuta **Tab**, kui IntelliJ pakub vastavat repositooriumi.

Anna märku, kuhu jõudsid, või kui tahad kumbagi lähenemist (SQL vs. Java) rohkem arutada!

---

## Samm 3 — DTO klass

### Mida teha?

Loo väljundi DTO klass juba nüüd — enne kui service meetod on täielikult valmis. Kuna andmed tulevad mitmest allikast (repository päring + hilisem liikmelisuse info, vt Samm 4), on selgem täita üht DTO struktuuri järk-järgult.

Vaata taski failist "Väljund" JSON näidist — sealt loed välja täpsed 16 välja, mis `TrainingGroupOverviewDto`-l peavad olema.

Kuna see DTO ei tule ühest entiteedist (see on mitme tabeli kombinatsioon), ei sobi JPA Buddy "New → DTO" ühe-entity generaator siin otseselt — loo DTO struktuur käsitsi, taski JSON näidise järgi.

- Kaust: `controller/training/dto/`
- Nimi: `TrainingGroupOverviewDto.java`

Anna märku, kui DTO on paigas!

---

## Samm 4 — Mapper/koostamine ja liikmelisuse info

### Mida teha?

Kuna sisend tuleb otse mitme tabeli liitepäringust (mitte ühest entiteedist), ei pruugi klassikaline entity→DTO MapStruct mapper siin sobida sama otseselt, mis lihtsamates teenustes. Mõtle: kas sinu Samm 2 päring saab tagastada juba peaaegu valmis kuju (nt otse konstruktori-avaldisega, vt backend/CLAUDE.md "SQL päringud" jaotist "vajadusel kasutab repositoorium konstruktori avaldist otse DTOsse projekteerimiseks"), või on selgem koguda vahepealsed väärtused ja panna need service kihis käsitsi kokku?

Kummagi variandi puhul jääb üks väli puudu esimesest päringust: **`isTrainingGroupMember`**. See väli ei tule `training_date`/`training_group` liitepäringust, vaid eraldi kontrollist: kas päritud `userId` on olemas `user_training_group` tabelis antud `trainingGroupId` kohta.

> **Mõtle:** kas tasub selle jaoks teha eraldi päring iga rea kohta, või üks päring, mis toob korraga **kõik** `trainingGroupId`-d, mille liige kasutaja on (nt `Set<Integer>`), ja siis Java poolel iga rea kohta lihtsalt `set.contains(...)` kontrollida? Teine variant väldib N+1 päringu probleemi.

Loo see teine repository meetod (samamoodi nagu Samm 2 juhtnööris — JPA Buddy "Query" tüüp).

Kui mõlemad andmeallikad (treeningute read + liikmelisuse `Set`) on käes, pane need service meetodis kokku lõplikuks `TrainingGroupOverviewDto` listiks.

> **Meetodi palve:** kui kutsud välja repository meetodi, mis midagi tagastab, ja kavatsed selle infoga midagi teha — pane tulemus **kohe** muutujasse, ära jäta väljakutset "rippuma".

Anna märku, kuidas edeneb!

---

## Samm 5 — tagasi RestController'isse

### Mida teha?

Service meetod peaks nüüd tagastama `List<TrainingGroupOverviewDto>`. Täienda kontrolleri meetodit `return` lausega ja paranda tagastustüüp (`void` → `List<TrainingGroupOverviewDto>`).

> **IntelliJ vihje:** kui kontrolleri meetod on veel `void`, aga sa kirjutad `return trainingService...`, IntelliJ kurdab — **Alt+Enter** → "Change return type" parandab automaatselt.

---

## Samm 6 — kood ilusaks (refactor)

Kui kood töötab, vaata service meetod uuesti üle:

- Kas "userId kehtivuse kontroll", "treeningute päring", "liikmelisuse kontroll" ja "tulemuse kokkupanek" on selgelt eristatavad (kas ühe meetodi sees, kas eraldi private meetoditena)?
- Kas meetodite järjekord service klassis on loogiline — `public` meetod üleval, `private` abimeetodid all, väljakutsumise hierarhia järgi?

Kasuta **Refactor → Extract Method**, kui mõni loogikaplokk (nt "leia järgmine treening grupi kohta") tundub omaette nimetamist väärt.

---

## Kokkuvõte ja kontrollnimekiri

Enne kui pead koodi valmis, kontrolli läbi:

- [ ] Kõik seitse uut entiteeti (`Facility`, `Sportclub`, `SkillLevel`, `TrainingGroup`, `Training`, `TrainingDate`, `UserTrainingGroup`) on loodud, õiges paketis, `schema = "sportclub"`
- [ ] `TrainingController` klass on olemas, `@GetMapping("/api/trainings")`, kuus `@RequestParam`-it (üks kohustuslik, viis valikulist)
- [ ] Kontrolleri meetodil on `@Operation` ja `@ApiResponses` annotatsioonid
- [ ] `TrainingService` klass on olemas
- [ ] Repository meetodid kasutavad `@Query` Named parameters stiilis
- [ ] Olematu `userId` viskab õige erindi (404, `PRIMARY_KEY_NOT_FOUND`)
- [ ] Iga treeninggrupp ilmub tulemuses ainult üks kord, koos oma **lähima tulevase** treeninguga
- [ ] Möödas olevad treeningud (kõik `training_date` kirjed minevikus) ei mõjuta tulemust
- [ ] `isTrainingGroupMember` on õige iga rea kohta, vastavalt `user_training_group` sisule
- [ ] Valikulised filtrid (`areaId`, `sportId`, `trainerId`, `date`, `time`) toimivad ja puuduva filtri korral piirangut ei rakendata
- [ ] Kood kompileerub ja Swagger UI kaudu on endpoint nähtav

---

> **Järgmine samm:** Testi endpointi Swagger UI kaudu (`http://localhost:8080/swagger-ui/index.html`), kasuta päringus `userId=3` — sinu `3_import.sql`-i näidisandmetes peaks customer (id 3) olema liige gruppides 1–4, aga mitte grupis 5 (Golf). Kontrolli, et vastus vastab taski failis toodud JSON näidisele.
