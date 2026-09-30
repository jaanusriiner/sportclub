# Treeninggrupiga liitumise taotlemine — implementatsiooni plaan

**Seotud task:** `docs/tasks/backend/Treeninggrupiga-liitumise-taotlemine.md`

## Hetkeseis (mis on juba olemas)

Eeldab, et tehtud on task "Treeningute nimekirja päring" (entiteet `TrainingGroup` + `TrainingGroupRepository`, `UserTrainingGroup` + `UserTrainingGroupRepository`, `service/training/TrainingService.java` koos `getValidUserBy(...)`-ga).

Error enumis puudub selle taski errorCode (`JOIN_APPLICATION_UNAVAILABLE`) — tuleb lisada. `join_application` tabeli jaoks entiteeti/repositoryt veel ei ole.

## Puuduv/muudetav

Uus entiteet + repository `join_application` jaoks, uued DTO-d, uus service meetod, uus controller, `Error` enumi täiendus.

## Sammud

### 1. `joinApplication` entiteet

Fail: `persistence/joinapplication/JoinApplication.java`

Väljad: `id`, `user` (`ManyToOne User`, `user_id`), `trainingGroup` (`ManyToOne TrainingGroup`, `training_group_id`), `status` (String, `@Size(max = 3)`).

### 2. `Status` enumi täiendus

Fail: `service/Status.java` — lisa `STATUS_PENDING("PEN")` (`join_application.status` väärtus uue taotluse loomisel), kõrvuti olemasoleva `STATUS_ACTIVE("A")`/`STATUS_DELETED("D")`-ga.

### 3. `JoinApplicationRepository`

Fail: `persistence/joinapplication/JoinApplicationRepository.java`

```java
public interface JoinApplicationRepository extends JpaRepository<JoinApplication, Integer> {
    boolean existsByUserIdAndTrainingGroupIdAndStatus(Integer userId, Integer trainingGroupId, String status);
}
```

### 4. `Error` enumi täiendus

Fail: `Error.java`

```java
JOIN_APPLICATION_UNAVAILABLE("Oled selle treeninggrupiga juba liitunud või taotlus on juba esitatud"),
```

### 5. DTO-d

1. `controller/traininggroup/dto/JoinApplicationRequestDto.java` — väli `userId` (Integer, `@NotNull`).
2. `controller/traininggroup/dto/JoinApplicationResponseDto.java` — väli `message` (String).

### 6. Service meetod

Fail: `service/training/TrainingService.java` (täiendus)

```java
@Transactional
public JoinApplicationResponseDto applyToJoinTrainingGroup(Integer trainingGroupId, Integer userId) {
    User user = getValidUserBy(userId);
    TrainingGroup trainingGroup = trainingGroupRepository.findById(trainingGroupId)
        .orElseThrow(() -> new PrimaryKeyNotFoundException("trainingGroupId", trainingGroupId));

    boolean isMember = userTrainingGroupRepository.existsByUserIdAndTrainingGroupId(userId, trainingGroupId);
    boolean hasPendingApplication = joinApplicationRepository
        .existsByUserIdAndTrainingGroupIdAndStatus(userId, trainingGroupId, STATUS_PENDING.getCode());
    if (isMember || hasPendingApplication) {
        throw new ForbiddenException(JOIN_APPLICATION_UNAVAILABLE.getMessage(), JOIN_APPLICATION_UNAVAILABLE.name());
    }

    JoinApplication joinApplication = new JoinApplication();
    joinApplication.setUser(user);
    joinApplication.setTrainingGroup(trainingGroup);
    joinApplication.setStatus(STATUS_PENDING.getCode());
    joinApplicationRepository.save(joinApplication);

    JoinApplicationResponseDto response = new JoinApplicationResponseDto();
    response.setMessage("Taotlus esitatud");
    return response;
}
```

Kasutab sama `getValidUserBy(...)` meetodit, mis "Treeningule registreerimine" taski plaanis (vt selle taski "Avatud küsimused" punkt 1 — meetod peab tagastama `User`).

### 7. Controller

Fail: `controller/traininggroup/TrainingGroupController.java`

```java
@PostMapping("/api/training-groups/{trainingGroupId}/join-applications")
public JoinApplicationResponseDto applyToJoin(@PathVariable Integer trainingGroupId,
                                               @RequestBody @Valid JoinApplicationRequestDto requestDto) {
    return trainingService.applyToJoinTrainingGroup(trainingGroupId, requestDto.getUserId());
}
```

## Veakäsitlus

| Olukord | Erind | HTTP |
|---|---|---|
| `trainingGroupId` ei eksisteeri | `PrimaryKeyNotFoundException("trainingGroupId", trainingGroupId)` | 404 |
| `userId` ei eksisteeri | `PrimaryKeyNotFoundException("userId", userId)` | 404 |
| Kasutaja juba liige VÕI juba ootel taotlus | `ForbiddenException(JOIN_APPLICATION_UNAVAILABLE...)` | 403 |

Kõiki neid püüab kinni juba olemas olev `RestExceptionHandler` — uusi handler-meetodeid pole vaja.

## Testid

- **`TrainingServiceTest`** (täiendus): edukas taotlus (kontrolli, et `join_application` rida tekib staatusega `'PEN'`), juba liige, juba ootel taotlus, olematu `trainingGroupId`, olematu `userId`.
- **`TrainingGroupControllerTest`**: `POST /api/training-groups/5/join-applications` — 200 + sõnum "Taotlus esitatud"; iga veaolukord tagastab õige status code'i ja `errorCode`-i.

## Avatud küsimused

Ei tuvastanud selle taski jaoks eraldi lahtisi küsimusi — `getValidUserBy(userId)` tagastab kinnitatult `User` objekti (vt eelmiste taskide plaane), mida siin otse kasutatakse.
