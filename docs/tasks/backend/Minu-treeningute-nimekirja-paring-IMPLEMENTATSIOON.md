# Minu treeningute nimekirja päring — implementatsiooni plaan

**Seotud task:** `docs/tasks/backend/Minu-treeningute-nimekirja-paring.md`

## Hetkeseis (mis on juba olemas)

Eeldab, et task "Treeningute nimekirja päring" (`Treeningute-nimekirja-paring-IMPLEMENTATSIOON.md`) on juba tehtud — sellest on olemas entiteedid `Facility`, `Sportclub`, `SkillLevel`, `TrainingGroup`, `Training`, `TrainingDate`, `UserTrainingGroup` koos repositooriumitega, ning `service/training/TrainingService.java` (kasutatakse ka selle taski jaoks, vt allpool).

Puudub veel: `user_training` tabeli entiteet/repository, `MyTrainingDto`, endpoint `GET /api/users/{userId}/trainings`.

## Puuduv/muudetav

Uus entiteet + repository `user_training` jaoks, uus DTO, uus service meetod (samas `TrainingService` klassis), uus controller.

## Sammud

### 1. `UserTraining` entiteet

Fail: `persistence/usertraining/UserTraining.java`

Väljad: `id`, `user` (`ManyToOne User`, `user_id`), `trainingDate` (`ManyToOne TrainingDate`, `training_date_id`). Sama muster mis `UserTrainingGroup` (vt eelmine task).

### 2. `UserTrainingRepository`

Fail: `persistence/usertraining/UserTrainingRepository.java`

```java
public interface UserTrainingRepository extends JpaRepository<UserTraining, Integer> {

    @Query("""
        SELECT new ee.sportclub.controller.user.dto.MyTrainingDto(
            td.id, tg.id, sp.name, f.name, CONCAT(pr.firstName, ' ', pr.lastName),
            td.startDate, td.startTime, td.userCount, td.maxSize
        )
        FROM UserTraining ut
        JOIN ut.trainingDate td
        JOIN td.training t
        JOIN t.trainingGroup tg
        JOIN tg.sport sp
        JOIN tg.user u
        JOIN Profile pr ON pr.user = u
        JOIN td.facility f
        WHERE ut.user.id = :userId
          AND (td.startDate > CURRENT_DATE OR (td.startDate = CURRENT_DATE AND td.startTime >= CURRENT_TIME))
        ORDER BY td.startDate ASC, td.startTime ASC
    """)
    List<MyTrainingDto> findMyUpcomingTrainingsBy(Integer userId);

    boolean existsByUserIdAndTrainingDateId(Integer userId, Integer trainingDateId);
}
```

Siin saab konstruktori-avaldist kasutada otse (erinevalt eelmisest taskist), kuna iga registreering on juba täpselt üks väljundrida — pole vaja "järgmise treeningu" väljavalimist grupi kaupa.

`existsByUserIdAndTrainingDateId` on Spring Data derived query — seda kasutab task "Treeningule registreerimine" topeltregistreerimise kontrolliks.

### 3. `MyTrainingDto`

Fail: `controller/user/dto/MyTrainingDto.java` — väljad täpselt taski järgi (`trainingDateId`, `trainingGroupId`, `sportName`, `facilityName`, `trainerName`, `nextTrainingDate`, `nextTrainingTime`, `userCount`, `maxSize`).

### 4. Service meetod

Fail: `service/training/TrainingService.java` (täiendus, mitte uus klass — vt eelmise taski plaani põhjendust "üks teenusklass domeeni kohta")

```java
public List<MyTrainingDto> findMyTrainings(Integer userId) {
    getValidUserBy(userId);
    return userTrainingRepository.findMyUpcomingTrainingsBy(userId);
}
```

`getValidUserBy(...)` on juba loodud eelmise taski käigus, taaskasutatakse.

### 5. Controller

Fail: `controller/user/UserTrainingController.java`

```java
@GetMapping("/api/users/{userId}/trainings")
public List<MyTrainingDto> findMyTrainings(@PathVariable Integer userId) {
    return trainingService.findMyTrainings(userId);
}
```

## Veakäsitlus

Sama muster mis eelmisel taskil: olematu `userId` → `PrimaryKeyNotFoundException("userId", userId)` → 404 + `PRIMARY_KEY_NOT_FOUND`. Uut errorCode'i pole vaja.

## Testid

- **`TrainingServiceTest`** (täiendus): `findMyTrainings` — mitme registreeringuga kasutaja, registreeringuteta kasutaja (tühi list), ainult tulevaste kuupäevade tagastamine, olematu `userId`.
- **`UserTrainingControllerTest`**: `GET /api/users/3/trainings` tagastab 200 ja õige struktuuriga JSON-i; tundmatu `userId` tagastab 404.

## Avatud küsimused

Ei tuvastanud vastuolusid. Paketi valik (`MyTrainingDto` ja `UserTrainingController` paketis `controller/user/`, vastavalt URL-i `/api/users/{userId}/...` ressursile) on kasutajaga kinnitatud.
