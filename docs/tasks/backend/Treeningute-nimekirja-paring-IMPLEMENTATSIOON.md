# Treeningute nimekirja päring — implementatsiooni plaan

**Seotud task:** `docs/tasks/backend/Treeningute-nimekirja-paring.md`

## Hetkeseis (mis on juba olemas)

Olemasolevad `persistence/` entiteedid: `Area`, `Profile`, `Role`, `Sport`, `User` (koos `AreaRepository`/`AreaMapper` ja `UserRepository`/`UserMapper`; `Profile`, `Role`, `Sport` on ainult entiteedina, ilma repository/mapperita).

Selle taski jaoks vajalikke tabeleid (`facility`, `sportclub`, `skill_level`, `training_group`, `training`, `training_date`, `user_training_group`) ei ole `persistence/` all veel üldse — kõik tuleb luua nullist. Kontrollerit, service'it ega DTO-sid `trainings` teenuse jaoks samuti veel ei ole.

Eeskujuks olemasolevast koodist: `AreaController` + `AreaService` + `AreaMapper` (lihtne nimekirja-teenus) ja `RegisterService.getValidArea(...)` muster (`getValid<Entiteet>By` meetod `orElseThrow`-ga).

## Puuduv/muudetav

Kõik allolev tuleb luua. See task loob ka mitu entiteeti/repositooriumit, mida hiljem kasutavad ka teised kolm `/trainings` vaate taski ("Minu treeningute nimekirja päring", "Treeningule registreerimine", "Treeninggrupiga liitumise taotlemine") — vt nende endi implementatsiooniplaane.

## Sammud

### 1. Uued JPA entiteedid

Kõik entiteedid järgivad `Area`/`Profile` musterit (`@Getter @Setter @Entity @Table(schema = "sportclub")`, `@ManyToOne(fetch = FetchType.LAZY, optional = false)` + `@JoinColumn` seoste jaoks).

1. **`Facility`** — fail: `persistence/facility/Facility.java`
   - Väljad: `id`, `area` (`ManyToOne Area`, `area_id`), `name`, `address`, `description` (nullable, ilma `@NotNull`).
2. **`Sportclub`** — fail: `persistence/sportclub/Sportclub.java`
   - Väljad: `id`, `name`.
3. **`SkillLevel`** — fail: `persistence/skilllevel/SkillLevel.java`
   - Väljad: `id`, `sport` (`ManyToOne Sport`, `sport_id`), `name` (`@Size(max = 30)`).
4. **`TrainingGroup`** — fail: `persistence/traininggroup/TrainingGroup.java`
   - Väljad: `id`, `sportclub` (`ManyToOne Sportclub`), `sport` (`ManyToOne Sport`), `user` (`ManyToOne User`, treener), `name` (`@Size(max = 100)`), `description` (nullable), `skillLevel` (`ManyToOne SkillLevel`).
5. **`Training`** — fail: `persistence/training/Training.java`
   - Väljad: `id`, `trainingGroup` (`ManyToOne TrainingGroup`), `defaultFacility` (`ManyToOne Facility`), `name`, `maxsize` (Integer), `description` (nullable), `defaultStartDate`/`defaultEndDate` (`LocalDate`, nullable), `defaultStartTime`/`defaultEndTime` (`LocalTime`), `duration` (Integer), `weekdays` (String).
6. **`TrainingDate`** — fail: `persistence/trainingdate/TrainingDate.java`
   - Väljad: `id`, `training` (`ManyToOne Training`), `facility` (`ManyToOne Facility`), `startDate` (`LocalDate`), `startTime` (`LocalTime`), `duration` (Integer), `status` (String, `@Size(max = 3)`), `userCount` (Integer), `maxSize` (Integer), `dateAdded` (`LocalDate`).
7. **`UserTrainingGroup`** — fail: `persistence/usertraininggroup/UserTrainingGroup.java`
   - Väljad: `id`, `user` (`ManyToOne User`), `trainingGroup` (`ManyToOne TrainingGroup`).

### 2. Repositooriumid

1. `persistence/facility/FacilityRepository.java` — `JpaRepository<Facility, Integer>`, ilma lisameetoditeta (kasutatakse liitumistes teiste päringute kaudu).
2. `persistence/sportclub/SportclubRepository.java` — `JpaRepository<Sportclub, Integer>`.
3. `persistence/skilllevel/SkillLevelRepository.java` — `JpaRepository<SkillLevel, Integer>`.
4. `persistence/traininggroup/TrainingGroupRepository.java` — `JpaRepository<TrainingGroup, Integer>`.
5. `persistence/training/TrainingRepository.java` — `JpaRepository<Training, Integer>`.
6. `persistence/trainingdate/TrainingDateRepository.java` — `JpaRepository<TrainingDate, Integer>` + custom päring (vt samm 3).
7. `persistence/usertraininggroup/UserTrainingGroupRepository.java` — `JpaRepository<UserTrainingGroup, Integer>` + meetod:
   ```java
   @Query("select utg.trainingGroup.id from UserTrainingGroup utg where utg.user.id = :userId")
   Set<Integer> findTrainingGroupIdsByUserId(Integer userId);
   ```
   Seda meetodit kasutavad ka teised taskid (Registreeru/Taotle Liitumist liikmelisuse kontrolliks).

### 3. Projektsiooni-DTO tulevaste treeningute jaoks

Fail: `persistence/trainingdate/TrainingDateOverviewRow.java` — tavaline klass (mitte entiteet), mida kasutatakse ainult JPQL konstruktori-avaldise sihtklassina samm 4 päringus. Väljad vastavad `TrainingGroupOverviewDto` väljadele, va `isTrainingGroupMember` (see arvutatakse alles service kihis, vt samm 5).

```java
public class TrainingDateOverviewRow {
    private Integer trainingGroupId;
    private Integer sportId;
    private String sportName;
    private Integer facilityId;
    private String facilityName;
    private Integer trainerId;
    private String trainerFirstName;
    private String trainerLastName;
    private Integer sportclubId;
    private String sportclubName;
    private Integer skillLevelId;
    private String skillLevelName;
    private Integer trainingDateId;
    private LocalDate nextTrainingDate;
    private LocalTime nextTrainingTime;
    private Integer userCount;
    private Integer maxSize;
    // konstruktor kõigi väljadega (vajalik JPQL "new" avaldiseks), getterid
}
```

### 4. Custom JPQL päring tulevaste treeningute ridade jaoks

Fail: `persistence/trainingdate/TrainingDateRepository.java`

```java
@Query("""
    SELECT new ee.sportclub.persistence.trainingdate.TrainingDateOverviewRow(
        tg.id, sp.id, sp.name, f.id, f.name, u.id, pr.firstName, pr.lastName,
        sc.id, sc.name, sl.id, sl.name, td.id, td.startDate, td.startTime, td.userCount, td.maxSize
    )
    FROM TrainingDate td
    JOIN td.training t
    JOIN t.trainingGroup tg
    JOIN tg.sport sp
    JOIN tg.sportclub sc
    JOIN tg.skillLevel sl
    JOIN tg.user u
    JOIN Profile pr ON pr.user = u
    JOIN td.facility f
    WHERE td.status = 'A'
      AND (td.startDate > CURRENT_DATE OR (td.startDate = CURRENT_DATE AND td.startTime >= CURRENT_TIME))
      AND (:areaId IS NULL OR f.area.id = :areaId)
      AND (:sportId IS NULL OR sp.id = :sportId)
      AND (:trainerId IS NULL OR u.id = :trainerId)
      AND (:date IS NULL OR td.startDate = :date)
      AND (:time IS NULL OR td.startTime = :time)
    ORDER BY tg.id ASC, td.startDate ASC, td.startTime ASC
""")
List<TrainingDateOverviewRow> findUpcomingTrainingRowsBy(Integer areaId, Integer sportId, Integer trainerId, LocalDate date, LocalTime time);
```

**Disainiotsus:** päring tagastab iga treeninggrupi kõik tulevased toimumisajad (mitte ainult järgmise), sorteeritud grupi kaupa kasvavas kuupäeva/kellaaja järjekorras. "Järgmise treeningu" väljavõtmine (esimene rida grupi kohta) tehakse service kihis (samm 5), kuna korreleeritud alampäring ("MIN(start_date) sama treeningu kohta, mis on tulevikus") JPQL konstruktori-avaldisega on ebamugav/vähemloetav. See on lihtsam ja kergemini testitav lahendus.

### 5. `TrainingGroupOverviewDto` ja `TrainingService`

1. DTO fail: `controller/training/dto/TrainingGroupOverviewDto.java` — väljad täpselt vastavalt taskile (`trainingGroupId`, `sportId`, `sportName`, `facilityId`, `facilityName`, `trainerId`, `trainerName`, `sportclubId`, `sportclubName`, `skillLevelId`, `skillLevelName`, `trainingDateId`, `nextTrainingDate`, `nextTrainingTime`, `userCount`, `maxSize`, `isTrainingGroupMember`).

2. Service fail: `service/training/TrainingService.java` — see klass katab kogu `trainings` domeeni (kasutatakse ka teistes selle vaate taskides, vt nende plaane), sarnaselt sellele, kuidas backend/CLAUDE.md kirjeldab "iga domeenialal oma teenusklass".

   ```java
   public List<TrainingGroupOverviewDto> findTrainings(Integer userId, Integer areaId, Integer sportId,
                                                         Integer trainerId, LocalDate date, LocalTime time) {
       getValidUserBy(userId); // 404, kui userId ei eksisteeri
       List<TrainingDateOverviewRow> rows =
           trainingDateRepository.findUpcomingTrainingRowsBy(areaId, sportId, trainerId, date, time);
       Set<Integer> memberGroupIds = userTrainingGroupRepository.findTrainingGroupIdsByUserId(userId);

       Map<Integer, TrainingDateOverviewRow> nextRowByGroup = new LinkedHashMap<>();
       for (TrainingDateOverviewRow row : rows) {
           nextRowByGroup.putIfAbsent(row.getTrainingGroupId(), row); // esimene rida grupi kohta = lähim tulevane
       }

       return nextRowByGroup.values().stream()
           .map(row -> toTrainingGroupOverviewDto(row, memberGroupIds.contains(row.getTrainingGroupId())))
           .toList();
   }
   ```

   `toTrainingGroupOverviewDto(row, isMember)` on eraldi privaatne meetod, mis paneb kokku DTO (sh `trainerName = row.getTrainerFirstName() + " " + row.getTrainerLastName()`).

   Kasutajakontrolli jaoks vajalik `getValidUserBy(Integer userId)` — kasuta olemasolevat `UserRepository` (juba olemas), viska `PrimaryKeyNotFoundException("userId", userId)`, kui puudub (vt backend/CLAUDE.md "Entiteedi otsing ID järgi" konventsiooni). Meetod **tagastab `User` objekti** (mitte void) — praeguses taskis kasutatakse tagastusväärtust ainult kontrolliks (tulemus visatakse minema), kuid hilisemad taskid ("Treeningule registreerimine", "Treeninggrupiga liitumise taotlemine") vajavad päris `User` objekti seose loomiseks, seega on õigem see kohe nii implementeerida:

   ```java
   public User getValidUserBy(Integer userId) {
       return userRepository.findById(userId)
           .orElseThrow(() -> new PrimaryKeyNotFoundException("userId", userId));
   }
   ```

### 6. Controller

Fail: `controller/training/TrainingController.java`

```java
@GetMapping("/api/trainings")
public List<TrainingGroupOverviewDto> findTrainings(
        @RequestParam Integer userId,
        @RequestParam(required = false) Integer areaId,
        @RequestParam(required = false) Integer sportId,
        @RequestParam(required = false) Integer trainerId,
        @RequestParam(required = false) LocalDate date,
        @RequestParam(required = false) LocalTime time) {
    return trainingService.findTrainings(userId, areaId, sportId, trainerId, date, time);
}
```

## Veakäsitlus

Ainus taskis kirjeldatud veaolukord on olematu `userId` → `PrimaryKeyNotFoundException("userId", userId)` `TrainingService.getValidUserBy(...)`-s, mille `RestExceptionHandler` (juba olemas) püüab kinni ja tagastab 404 + `PRIMARY_KEY_NOT_FOUND`. Uut errorCode'i pole vaja lisada — see kasutab juba olemasolevat mustrit (vt `RegisterService.getValidArea`).

## Testid

- **`TrainingServiceTest`** (unit, mocked repositoriumid):
  - mitme treeninggrupiga vastus, kus igal on õige "järgmine" treening valitud (mitte varasem/hilisem toimumisaeg samast grupist)
  - `isTrainingGroupMember` on `true`/`false` vastavalt `userTrainingGroupRepository` tagastusele
  - tühi tulemus, kui filter ei anna vasteid
  - möödas olev `training_date` ei ilmu tulemusse
  - olematu `userId` viskab `PrimaryKeyNotFoundException`
- **`TrainingControllerTest`** (integratsioon, nt `@SpringBootTest` + test-andmebaas või `MockMvc` + mockitud service):
  - `GET /api/trainings?userId=3` tagastab 200 ja JSON struktuuri vastavalt taskile
  - filtrite (`areaId`, `sportId`, `trainerId`, `date`, `time`) rakendumine
  - tundmatu `userId` tagastab 404 `PRIMARY_KEY_NOT_FOUND`

## Avatud küsimused

Ei tuvastanud vastuolusid taski ja koodibaasi vahel — kõik vajalikud tabelid/entiteedid on uued ja neid pole varem teistmoodi implementeeritud.
