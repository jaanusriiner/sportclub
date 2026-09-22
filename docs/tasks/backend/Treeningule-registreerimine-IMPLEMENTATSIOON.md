# Treeningule registreerimine — implementatsiooni plaan

**Seotud task:** `docs/tasks/backend/Treeningule-registreerimine.md`

## Hetkeseis (mis on juba olemas)

Eeldab, et tehtud on task "Treeningute nimekirja päring" (entiteedid `TrainingGroup`, `Training`, `TrainingDate`, `UserTrainingGroup` + repositooriumid, `service/training/TrainingService.java` koos `getValidUserBy(...)`-ga) ja task "Minu treeningute nimekirja päring" (`UserTraining` entiteet + `UserTrainingRepository`, sh `existsByUserIdAndTrainingDateId`).

Error enumis (`ee.sportclub.Error`) on hetkel ainult `INCORRECT_CREDENTIALS`, `USER_UNAVAILABLE`, `SPORT_MISSING` — selle taski kolm errorCode'i (`NOT_TRAINING_GROUP_MEMBER`, `TRAINING_FULL`, `ALREADY_REGISTERED`) tuleb lisada.

Puudub veel: endpoint, request/response DTO-d, registreerimise äriloogika.

## Puuduv/muudetav

Uued DTO-d, uus service meetod, uus controller, `Error` enumi täiendus, `TrainingDateRepository`/`UserTrainingRepository` täiendused (leidmise/kirjutamise meetodid).

## Sammud

### 1. `Error` enumi täiendus

Fail: `Error.java`

```java
NOT_TRAINING_GROUP_MEMBER("Registreerumiseks pead olema treeninggrupi liige"),
TRAINING_FULL("Sellel treeningul pole enam vabu kohti"),
ALREADY_REGISTERED("Oled juba sellele treeningule registreerunud"),
```

### 2. `TrainingDateRepository` täiendus

Fail: `persistence/trainingdate/TrainingDateRepository.java` (loodud eelmises taskis)

Lisa meetod, mis leiab `TrainingDate` koos `TrainingGroup` ID-ga (vajalik liikmelisuse kontrolliks) **ja lukustab rea** (`PESSIMISTIC_WRITE`) kuni transaktsiooni lõpuni — see väldib olukorda, kus kaks kasutajat loevad mõlemad `user_count < max_size` ja registreeruvad mõlemad viimasele vabale kohale (race condition):

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("select td from TrainingDate td join fetch td.training t join fetch t.trainingGroup where td.id = :trainingDateId")
Optional<TrainingDate> findByIdWithTrainingGroupForUpdate(Integer trainingDateId);
```

See genereerib SQL-i `SELECT ... FOR UPDATE`, mis lukustab konkreetse `training_date` rea — teine samaaegne `registerForTraining` päring sama `trainingDateId`-ga ootab, kuni esimese transaktsioon (`@Transactional`, vt samm 4) commit'ib või rollback'ib, mitte ei loe vahepeal vana `user_count` väärtust.

### 3. DTO-d

1. `controller/trainingdate/dto/TrainingDateRegisterRequestDto.java` — väli `userId` (Integer, `@NotNull`).
2. `controller/trainingdate/dto/TrainingRegisterResponseDto.java` — väli `message` (String).

### 4. Service meetod

Fail: `service/training/TrainingService.java` (täiendus)

```java
@Transactional
public TrainingRegisterResponseDto registerForTraining(Integer trainingDateId, Integer userId) {
    User user = getValidUserBy(userId);
    // Lukustab training_date rea (FOR UPDATE) transaktsiooni lõpuni, vt samm 2
    TrainingDate trainingDate = trainingDateRepository.findByIdWithTrainingGroupForUpdate(trainingDateId)
        .orElseThrow(() -> new PrimaryKeyNotFoundException("trainingDateId", trainingDateId));
    Integer trainingGroupId = trainingDate.getTraining().getTrainingGroup().getId();

    boolean isMember = userTrainingGroupRepository.existsByUserIdAndTrainingGroupId(userId, trainingGroupId);
    if (!isMember) {
        throw new ForbiddenException(NOT_TRAINING_GROUP_MEMBER.getMessage(), NOT_TRAINING_GROUP_MEMBER.name());
    }

    boolean alreadyRegistered = userTrainingRepository.existsByUserIdAndTrainingDateId(userId, trainingDateId);
    if (alreadyRegistered) {
        throw new ForbiddenException(ALREADY_REGISTERED.getMessage(), ALREADY_REGISTERED.name());
    }

    if (trainingDate.getUserCount() >= trainingDate.getMaxSize()) {
        throw new ForbiddenException(TRAINING_FULL.getMessage(), TRAINING_FULL.name());
    }

    UserTraining userTraining = new UserTraining();
    userTraining.setUser(user);
    userTraining.setTrainingDate(trainingDate);
    userTrainingRepository.save(userTraining);

    trainingDate.setUserCount(trainingDate.getUserCount() + 1);
    trainingDateRepository.save(trainingDate);

    TrainingRegisterResponseDto response = new TrainingRegisterResponseDto();
    response.setMessage("Oled edukalt treeningule registreerinud");
    return response;
}
```

`@Transactional` tagab, et liikmelisuse/täituvuse kontroll ja kirjutamine (samm `user_training` loomine + `user_count` kasvatamine) toimuvad koos — vt backend/CLAUDE.md ja `RegisterService` eeskuju.

`getValidUserBy(userId)` tagastab `User` objekti (kinnitatud eelmise taski plaanis) — kasutatakse siin otse `UserTraining.setUser(...)` jaoks, ilma lisapäringuta.

**Samaaegsuse kaitse on rakendatud** `findByIdWithTrainingGroupForUpdate(...)` päringu kaudu (samm 2, `PESSIMISTIC_WRITE`-lukk) — `user_count`/`max_size` kontroll ja `user_count` kasvatamine toimuvad sama lukustatud rea peal ühes `@Transactional` plokis, mistõttu kaks samaaegset registreerumist ei saa mõlemad läbida täituvuskontrolli enne kirjutamist.

### 5. Controller

Fail: `controller/trainingdate/TrainingDateController.java`

```java
@PostMapping("/api/training-dates/{trainingDateId}/register")
public TrainingRegisterResponseDto register(@PathVariable Integer trainingDateId,
                                             @RequestBody @Valid TrainingDateRegisterRequestDto requestDto) {
    return trainingService.registerForTraining(trainingDateId, requestDto.getUserId());
}
```

## Veakäsitlus

| Olukord | Erind | HTTP |
|---|---|---|
| `trainingDateId` ei eksisteeri | `PrimaryKeyNotFoundException("trainingDateId", trainingDateId)` | 404 |
| `userId` ei eksisteeri | `PrimaryKeyNotFoundException("userId", userId)` | 404 |
| Kasutaja pole treeninggrupi liige | `ForbiddenException(NOT_TRAINING_GROUP_MEMBER...)` | 403 |
| Kasutaja juba registreeritud | `ForbiddenException(ALREADY_REGISTERED...)` | 403 |
| Treening täis | `ForbiddenException(TRAINING_FULL...)` | 403 |

Kõiki neid püüab kinni juba olemas olev `RestExceptionHandler` — uusi handler-meetodeid pole vaja.

## Testid

- **`TrainingServiceTest`** (täiendus): edukas registreerimine (kontrolli, et `user_count` kasvab ja `user_training` rida tekib), mitte-liige, täis treening, juba registreeritud, olematu `trainingDateId`, olematu `userId`. Lukustuse enda toimimist (`PESSIMISTIC_WRITE`) mockitud repositooriumiga ei saa testida — see nõuaks eraldi integratsioonitesti kahe paralleelse päringuga päris andmebaasi vastu (nt kaks lõime, mis mõlemad registreeruvad viimasele vabale kohale, ja kontroll, et `user_count` ei ületa `max_size`-i). Selline test on soovituslik, kuid pole kohustuslik vastuvõtu kriteeriumides.
- **`TrainingDateControllerTest`**: `POST /api/training-dates/1/register` — 200 + õige sõnum; iga veaolukord tagastab õige status code'i ja `errorCode`-i.

## Avatud küsimused

Ei tuvastanud vastuolusid taski ja koodibaasi vahel. Samaaegsuse kaitse (pessimistlik lukustus) on kasutajaga kinnitatud ja plaani lisatud (vt samm 2 ja 4).
