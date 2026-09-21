# Kasutaja sisselogimine — implementatsiooni plaan

**Seotud task:** `docs/tasks/backend/Kasutaja-sisselogimine.md`

## Hetkeseis (mis on juba olemas)

Backend (`backend/src/main/java/ee/sportclub/`) sisaldab praegu ainult infrastruktuuri:

- `SportclubApplication.java` — Spring Boot käivitusklass.
- `infrastructure/RestExceptionHandler.java` — `@ControllerAdvice`, mis käsitleb `ForbiddenException` (403), `DataNotFoundException` (404), `PrimaryKeyNotFoundException` (404) ja `MethodArgumentNotValidException` (400, `INCORRECT_INPUT`). Vastuseks on `ApiError` (`message`, `errorCode`).
- `infrastructure/exception/ForbiddenException.java` — konstruktor `(String message, String errorCode)`. **See katab taski 403 vea, muudatust ei vaja.**
- `infrastructure/error/ApiError.java`, `DataNotFoundException.java`, `PrimaryKeyNotFoundException.java`, `util/StringBytesConverter.java` — sellel taskil ei ole rolli.

Seadistus ja andmebaas:

- `backend/build.gradle` — olemas `web`, `data-jpa`, `validation`, Lombok, MapStruct 1.6.3 (`defaultComponentModel=spring`), `spring-boot-starter-test`, `spring-boot-starter-webmvc-test`. H2 puudub.
- `backend/src/main/resources/application.properties` — PostgreSQL `vali_it` (P6Spy kaudu), `server.port` seadmata (8080). **Skeemi seadistust (`sportclub`) ei ole.**
- `frontend/vite.config.js` — `/api` proxy `http://localhost:8080`, seega CORS-i seadistada ei ole vaja.
- `docs/database/2_create.sql` ja `3_import.sql` — tabelid `user`, `role`. Näidisandmed: rollid `admin` (1), `trainer` (2), `customer` (3); kasutajad `admin@admin.ee` (admin), `trainer@trainer.ee` (trainer), `customer@customer.ee` (customer), kõik salasõnaga `123` ja `status = 'A'`, ning `inactive@inactive.ee` (customer, `status = 'D'`).

**Puudub täielikult:** ühtegi JPA entiteeti, repositooriumi, mapperit, teenust ega kontrollerit; klassid `Error` ja `Status` (mida `docs/backend/projekti-struktuur.md` mainib); kaust `backend/src/test/`.

## Puuduv/muudetav

- **Muuta:** `application.properties` — lisada vaikeskeem `sportclub`.
- **Luua (base pakett `ee.sportclub`):**
  - `Status.java`, `Error.java`
  - `persistence/role/Role.java`
  - `persistence/user/User.java`, `UserRepository.java`, `UserMapper.java`
  - `controller/login/dto/LoginRequestDto.java`, `LoginResponseDto.java`
  - `service/UserService.java`
  - `controller/login/LoginController.java`
  - testid kaustas `backend/src/test/java/ee/sportclub/`

`RestExceptionHandler` ja erindiklassid jäävad muutmata.

## Sammud

1. **Seadista andmebaasi skeem** — fail: `backend/src/main/resources/application.properties`
   - Lisa JPA sektsiooni: `spring.jpa.properties.hibernate.default_schema=sportclub`. Ilma selleta otsib Hibernate tabelit vaikeskeemist ja päring `"user"` tabelile ebaõnnestub (tabelid asuvad skeemis `sportclub`, vt `1_reset_database.sql` ja `3_import.sql`).

2. **Loo `Status` enum** — fail: `backend/src/main/java/ee/sportclub/Status.java`
   - Vastavalt `docs/backend/projekti-struktuur.md` asub see base paketis.
   ```java
   @Getter
   @AllArgsConstructor
   public enum Status {
       ACTIVE("A");

       private final String code;
   }
   ```
   - Ainult `ACTIVE`; andmebaasis on ka `'D'` (`inactive@inactive.ee`), aga selle tähendus ei ole taskides defineeritud (vt Avatud küsimused).

3. **Loo `Error` enum** — fail: `backend/src/main/java/ee/sportclub/Error.java`
   - Hoiab veakoodi ja teate ühes kohas, et vältida sõnede kordamist teenustes.
   ```java
   @Getter
   @AllArgsConstructor
   public enum Error {
       INCORRECT_CREDENTIALS("Vale e-post või parool");

       private final String message;

       public String getErrorCode() {
           return name();
       }
   }
   ```

4. **Loo `Role` entiteet** — fail: `backend/src/main/java/ee/sportclub/persistence/role/Role.java`
   - `@Entity @Table(name = "role") @Getter @Setter`
   - Väljad: `Integer id` (`@Id @GeneratedValue(strategy = GenerationType.IDENTITY)`), `String name`.
   - Eraldi `RoleRepository` ei ole vaja, sest rolli loetakse ainult `User` kaudu.

5. **Loo `User` entiteet** — fail: `backend/src/main/java/ee/sportclub/persistence/user/User.java`
   - `@Entity @Table(name = "\"user\"") @Getter @Setter` — `user` on PostgreSQL-is reserveeritud sõna, seetõttu jutumärgid (nagu `2_create.sql`-is).
   - Väljad: `Integer id` (IDENTITY), `String email`, `String password`, `String status`, `@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "role_id", nullable = false) Role role`.

6. **Loo `UserRepository`** — fail: `backend/src/main/java/ee/sportclub/persistence/user/UserRepository.java`
   - Kohandatud JPQL päring `@Query` annotatsiooniga, meetodi nimi mainib subjekti (`findUserBy`, vt backend/CLAUDE.md):
   ```java
   public interface UserRepository extends JpaRepository<User, Integer> {

       @Query("select u from User u join fetch u.role " +
              "where u.email = :email and u.password = :password and u.status = :status")
       Optional<User> findUserBy(@Param("email") String email,
                                 @Param("password") String password,
                                 @Param("status") String status);
   }
   ```
   - `join fetch u.role` on vajalik, et `role.name` oleks mapperis kättesaadav ka siis, kui tehingut/Open-Session-in-View'd ei ole.

7. **Loo DTO-d** — kaust: `backend/src/main/java/ee/sportclub/controller/login/dto/`
   - `LoginRequestDto.java` — `@Data`; väljad `String email`, `String password`. Valideerimisannotatsioone ei lisa (task ei nõua, vt "Veakäsitlus").
   - `LoginResponseDto.java` — `@Data`; väljad `Integer userId`, `String roleName`.
   - Kontrollitud: neid DTO nimesid ei kasuta praegu ükski teine ressurss, seega jäävad ressursipõhisesse `dto/` paketti (mitte `controller/common/dto/`).

8. **Loo `UserMapper`** — fail: `backend/src/main/java/ee/sportclub/persistence/user/UserMapper.java`
   ```java
   @Mapper
   public interface UserMapper {

       @Mapping(source = "id", target = "userId")
       @Mapping(source = "role.name", target = "roleName")
       LoginResponseDto toLoginResponseDto(User user);
   }
   ```
   - `componentModel = "spring"` tuleb `build.gradle` kompilaatori argumentidest, eraldi määrata ei ole vaja.

9. **Loo `UserService`** — fail: `backend/src/main/java/ee/sportclub/service/UserService.java`
   - `@Service @RequiredArgsConstructor`; sõltuvused `UserRepository userRepository`, `UserMapper userMapper`.
   - Meetodid väljakutsumise järjekorras:
   ```java
   public LoginResponseDto login(LoginRequestDto loginRequestDto) {
       User user = getValidUserBy(loginRequestDto.getEmail(), loginRequestDto.getPassword());
       return userMapper.toLoginResponseDto(user);
   }

   public User getValidUserBy(String email, String password) {
       return userRepository.findUserBy(email, password, Status.ACTIVE.getCode())
               .orElseThrow(() -> new ForbiddenException(
                       Error.INCORRECT_CREDENTIALS.getMessage(),
                       Error.INCORRECT_CREDENTIALS.getErrorCode()));
   }
   ```
   - `getValidUserBy` järgib backend/CLAUDE.md `getValid<Entiteet>By` mustrit (`orElseThrow` public meetodis teenuses); erinevus on, et otsing käib e-posti + salasõna + staatuse järgi, mitte ID järgi.
   - Erindi vahel ei eristata põhjuseid (tundmatu e-post, vale salasõna, mitteaktiivne konto) — kõigil sama 403.

10. **Loo `LoginController`** — fail: `backend/src/main/java/ee/sportclub/controller/login/LoginController.java`
    ```java
    @RestController
    @RequestMapping("/api")
    @RequiredArgsConstructor
    public class LoginController {

        private final UserService userService;

        @PostMapping("/login")
        public LoginResponseDto login(@RequestBody LoginRequestDto loginRequestDto) {
            return userService.login(loginRequestDto);
        }
    }
    ```
    - Kontroller ei sisalda loogikat, ainult delegeerib teenusele. `@Valid` ei kasuta.

11. **Kontrolli kompileerimist ja käsitsi** — `cd backend && ./gradlew compileJava`, seejärel `./gradlew bootRun` (nõuab PostgreSQL-i koos skriptidega `1_`, `2_`, `3_`) ja:
    ```bash
    curl -i -X POST http://localhost:8080/api/login \
      -H "Content-Type: application/json" \
      -d '{"email":"admin@admin.ee","password":"123"}'
    ```
    Oodatud: 200 ja `{"userId":1,"roleName":"admin"}`.

## Veakäsitlus

- **403 `INCORRECT_CREDENTIALS`** — visatakse `UserService.getValidUserBy` sees `ForbiddenException` (`Error.INCORRECT_CREDENTIALS`), kui `findUserBy` tagastab tühja `Optional`-i. Olemasolev `RestExceptionHandler.handleForbiddenException` teisendab selle vastuseks `{ "message": "Vale e-post või parool", "errorCode": "INCORRECT_CREDENTIALS" }`. Handleri muutmine ei ole vaja.
- **Tühi/puuduv `email` või `password`** — JPQL võrdlus `null`-iga ei leia ühtegi rida, seega tuleb sama 403 (nagu taskis kirjas). Eraldi 400 valideerimist ei lisata.
- **500** — käsitlemata erindid (nt andmebaas ei vasta) annab Spring vaikimisi; erikäsitlust ei lisata.

## Testid

Kausta `backend/src/test/` praegu ei ole; luua pakettide struktuur `ee/sportclub/...`.

1. **Ühiktest** — `backend/src/test/java/ee/sportclub/service/UserServiceTest.java` (`@ExtendWith(MockitoExtension.class)`, mock `UserRepository` ja `UserMapper`):
   - edukas sisselogimine: repository tagastab kasutaja, tulemus on mapperi väljund; kontrolli, et päringule anti staatus `"A"`;
   - kasutajat ei leidu (`Optional.empty()`): visatakse `ForbiddenException` errorCode'iga `INCORRECT_CREDENTIALS` ja teatega "Vale e-post või parool".

2. **Integratsioonitest** — `backend/src/test/java/ee/sportclub/controller/login/LoginControllerTest.java` (`@SpringBootTest` + `@AutoConfigureMockMvc`, päris lokaalne PostgreSQL koos `3_import.sql` andmetega; kontrolli `@AutoConfigureMockMvc` importi Spring Boot 4 all — pakett on `org.springframework.boot.webmvc.test.autoconfigure`). Juhtumid:
   - `admin@admin.ee` / `123` → 200, `roleName = "admin"`, `userId = 1`
   - `trainer@trainer.ee` / `123` → 200, `roleName = "trainer"`
   - `customer@customer.ee` / `123` → 200, `roleName = "customer"`
   - õige e-post, vale salasõna → 403, `INCORRECT_CREDENTIALS`, message "Vale e-post või parool"
   - tundmatu e-post → 403, `INCORRECT_CREDENTIALS`
   - `inactive@inactive.ee` / `123` (`status = 'D'`) → 403, `INCORRECT_CREDENTIALS`
   - tühi body `{}` → 403, `INCORRECT_CREDENTIALS`

Käivitamine: `cd backend && ./gradlew test`.

## Avatud küsimused

1. **Skeem ja vananenud dokumentatsioon.** `backend/CLAUDE.md` ja `docs/backend/projekti-struktuur.md` räägivad paketist `ee.minuprojekt` ja skeemist `minu_projekt`, aga kood ja SQL kasutavad `ee.sportclub` ja `sportclub`. Plaan eeldab `sportclub` ja lisab `hibernate.default_schema` (samm 1). Alternatiiv on `?currentSchema=sportclub` andmebaasi URL-is. Kumb sobib?
2. **`Error` vs `ErrorResponse`.** Struktuuridokument nimetab base paketi klassi `Error.java` (veakoodide enum), `backend/CLAUDE.md` aga `ErrorResponse` enumi. Kumbagi praegu ei ole, olemasolevad erindid võtavad `errorCode`i Stringina. Plaan loob `Error` (vastavalt struktuuridokumendile). Klassinimi `Error` varjab `java.lang.Error`i, seega võib eelistada `ErrorResponse`. Millise nime valime?
3. **Salasõnad on andmebaasis lahtise tekstina** ja päring võrdleb neid otse. Task ei nõua räsimist, aga see mõjutaks ka registreerimist ja importi (`'123'`). Jätame praegu nii?
4. **Staatus `'D'`.** Tähendus ei ole kuskil defineeritud; plaan loob ainult `Status.ACTIVE`. Kui `'D'` (nt deaktiveeritud) on ametlik staatus, tuleks see lisada.
5. **Jagatud DTO.** `LoginResponseDto` (`userId`, `roleName`) ja registreerimise `RegisterResponseDto` on väljadelt identsed. Kui registreerimine hakkab sama DTO-d kasutama, tuleb see tõsta pakketti `controller/common/dto/`.
6. **Testide andmebaas.** Projektis ei ole H2 ega test-profiili, seega integratsioonitest vajab käivitatud lokaalset PostgreSQL-i koos skriptidega. Kas see sobib või tuleb testidele eraldi seadistus (nt Testcontainers/eraldi andmebaas)?
