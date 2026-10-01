# Liitumistaotluste kinnitamine ja tagasilükkamine haldusvaates

**Vaade:** `ManageTrainingsView.vue`, route `/manage-trainings`

**Roll:** Trainer (treener haldab ainult endale kuuluvate treeninggruppide liitumistaotlusi)

**Vaste balsamic mockupis:** "Halda treeninggruppe ja treeninguid" (`docs/balsamic/SportClub_Halda_treeninggruppe.pdf`), lehekülg 1/1, sektsioon "Treeninggruppide liitumistaotlused" (vt lisatud pilt `Liitumistaotluste-kinnitamine-ja-tagasilukkamine.png`)

![Mockup](./Liitumistaotluste-kinnitamine-ja-tagasilukkamine.png)

> **Märkus pildi kohta:** pilt on kopeeritud failist `docs/balsamic/pdf-images/1.png` (kogu "Halda treeninggruppe" lehekülg). Sellel taskil on huvipakkuv ainult alumine tabel "Treeninggruppide liitumistaotlused" ja selle veerg "Kinnitus" (✓ ja ✕).

## Kasutajavoog

Treener avab haldusvaate ja näeb alumises sektsioonis "Treeninggruppide liitumistaotlused" ootel olevaid taotlusi (nimekirja laadimine ja tabeli kuvamine on frontendis juba tehtud — vt "Komponendid ja failistruktuur"). Iga rea juures on kaks nuppu: ✓ (kinnita) ja ✕ (lükka tagasi). Nupule vajutades saadetakse kohe vastav päring backendile — mockup ei näita siin eraldi kinnitusmodaali. Õnnestumisel laetakse taotluste nimekiri uuesti ja menetletud rida enam ei kuvata. Kinnitamise korral saab taotleja treeninggrupi liikmeks (see toimub backendis).

Selle taski skoobist on välja jäetud: nimekirja laadimine (`GET /api/trainers/{trainerId}/join-applications`, valmis), treeningute tabel ning veerg "Taotluse Kuupäev" (andmebaasis puudub taotluse esitamise kuupäev, vt `Treeninggrupi-liitumistaotluste-nimekirja-paring.md`).

## Kasutajaliidese elemendid

| Element | Tüüp | Kirjeldus/käitumine |
|---|---|---|
| Tabel "Treeninggruppide liitumistaotlused" | tabel | Veerud: Nimi, Spordiklubi, Treeninggrupp, Grupi hetkeläitvus, Kinnitus. Üks rida iga ootel taotluse kohta |
| ✓ (linnuke) nupp | ikoonnupp (`PhCheck`) | Igal real. Saadab `PUT /api/join-applications/{joinApplicationId}/confirm` |
| ✕ (rist) nupp | ikoonnupp (`PhX`) | Igal real. Saadab `PUT /api/join-applications/{joinApplicationId}/reject` |
| Veateade | `AlertDanger.vue` | Kuvatakse tabeli kohal, kui kinnitamine/tagasilükkamine ebaõnnestub teadaoleva veaga (403, 404) |
| Tühi nimekiri | tekstirida tabelis | "Ootel liitumistaotlusi ei ole" (juba olemas) |

## Käitumine ja valideerimine

1. Nuppude vajutus ei vaja frontendis eelvalideerimist ega kinnitusmodaali (mockupi järgi toimub tegevus kohe).
2. Päringu ajal tuleb mõlemad selle rea (või kõik tabeli) ✓/✕ nupud ajutiselt keelata, et topeltvajutus ei saadaks kahte päringut samale taotlusele.
3. **Õnnestumisel (200):** laadi nimekiri uuesti kutsega `GET /api/trainers/{trainerId}/join-applications` (trainerId = sisselogitud kasutaja `userId`), nii et menetletud rida kaob. Mockup ei nõua eraldi õnnestumisteadet.
4. **403 `JOIN_APPLICATION_ALREADY_PROCESSED`** (taotlus on juba menetletud, nt teine sessioon tegi seda): kuva backend'i `message` komponendiga `AlertDanger.vue` ja laadi nimekiri uuesti, et iganenud rida kaoks.
5. **404 `PRIMARY_KEY_NOT_FOUND`:** kuva backend'i `message` komponendiga `AlertDanger.vue` ja laadi nimekiri uuesti.
6. **Muu viga (nt 500, võrguviga):** suuna kasutaja veaviolule (`NavigationService.navigateToErrorView()`), nagu mujal projektis.
7. Veateade tühjendatakse iga uue nupuvajutuse alguses.
8. Kasutaja `userId` võetakse `SessionStorageService.getUserId()` kaudu (juba olemas).

## API kutsed

### `PUT /api/join-applications/{joinApplicationId}/confirm`

**Backend task:** vt `docs/tasks/backend/Liitumistaotluse-kinnitamine.md`

**Backend allikas:** `JoinApplicationController.confirmJoinApplication`, `JoinApplicationService.confirmJoinApplication`.

Path variable: `joinApplicationId` — kinnitatav taotlus (`PendingJoinApplicationDto.joinApplicationId`). Request body puudub.

Response (200 OK): backend task kirjeldab "NONE" (body'd pole). Frontend ei tohi sõltuda vastuse sisust — nimekiri laetakse pärast õnnestumist uuesti. Vt lahtine ots allpool.

**Veateated:**

| Status code | errorCode | message | Frontend käitumine |
|---|---|---|---|
| 404 | `PRIMARY_KEY_NOT_FOUND` | "Ei leidnud primary keyd 'joinApplicationId' väärtusega: 999" | Kuva `AlertDanger`, laadi nimekiri uuesti |
| 403 | `JOIN_APPLICATION_ALREADY_PROCESSED` | "Taotlus on juba menetletud" | Kuva `AlertDanger`, laadi nimekiri uuesti |
| 500 | — | — | Suuna veaviolule |

### `PUT /api/join-applications/{joinApplicationId}/reject`

**Backend task:** vt `docs/tasks/backend/Liitumistaotluse-tagasilukkamine.md`

**Backend allikas:** `JoinApplicationController.rejectJoinApplication`, `JoinApplicationService.rejectJoinApplication`.

Path variable: `joinApplicationId` — tagasi lükatav taotlus. Request body puudub.

Response (200 OK): vt eelmine kutse (body'ga ei arvestata).

**Veateated:** samad mis kinnitamise kutsel (404 `PRIMARY_KEY_NOT_FOUND`, 403 `JOIN_APPLICATION_ALREADY_PROCESSED`, 500).

### Taotluste nimekirja uuestilaadimine — `GET /api/trainers/{trainerId}/join-applications`

Juba realiseeritud (`TrainerService.getTrainerJoinApplicationsRequest`, `ManageTrainingsView.getTrainerJoinApplications()`); selle taski käigus ainult kutsutakse uuesti pärast toimingut. Struktuur vt `docs/tasks/backend/Treeninggrupi-liitumistaotluste-nimekirja-paring.md`.

**Backend allikas:** `TrainerController.findTrainerPendingJoinApplications`, `PendingJoinApplicationDto.java` (`joinApplicationId`, `userId`, `userFullName`, `sportclubId`, `sportclubName`, `trainingGroupId`, `trainingGroupName`, `trainingGroupMemberCount`).

## Lahtised otsad

- **Lahknevus backend koodi ja taski vahel (täpsusta enne implementeerimist):** `JoinApplicationController.confirmJoinApplication` ja `rejectJoinApplication` on deklareeritud tagastama `JoinApplicationResponse`, kuid `JoinApplicationService` vastavad meetodid on `void` — see kood ei kompileeru nii, nagu see praegu on. Backend task ütleb "Response 200: NONE". Frontend ei kasuta vastuse sisu, seega see ei blokeeri, kuid backend tuleb enne testimist kompileeruvaks parandada (kas tagasta `void` või ehita vastus, ja uuenda backend task selle järgi).
- Mockup ei näita õnnestumisteadet ega kinnitusdialoogi; kui soovid (nt "Taotlus kinnitatud" `AlertSuccess`), lisa see eraldi otsusena.
- Backend ei kontrolli, kas taotlus kuulub päringu teinud treeneri treeninggrupile (tee kontroll backendis, kui tahetakse roll-põhist piiramist) — frontend näitab ainult treeneri enda taotlusi.

## Komponendid ja failistruktuur

Projekti tegelik struktuur erineb osaliselt dokumendist `docs/frontend/projekti-struktuur.md`: API teenused asuvad `src/services/` (mitte `api-services/`) ja modaalid `src/components/modal/` (mitte `modals/`). Järgi tegelikku koodibaasi.

- **Vaade:** `frontend/src/views/ManageTrainingsView.vue` — eksisteerib, route `/manage-trainings` (`manageTrainingsRoute`) on `src/router/index.js` failis olemas. Taotluste tabel koos ✓/✕ ikoonidega (`PhCheck`, `PhX`) on juba olemas, kuid nuppudel puudub klõpsukäitleja.
- **Teenus:** lisa `frontend/src/services/JoinApplicationService.js` meetoditega `putConfirmRequest(joinApplicationId)` ja `putRejectRequest(joinApplicationId)` (`axios.put`). Nimekirja päring on juba `TrainerService.getTrainerJoinApplicationsRequest`.
- **Vaates (`methods`):** `confirmJoinApplication(joinApplicationId)`, `rejectJoinApplication(joinApplicationId)`, ühine `handleJoinApplicationProcessed()` (nimekirja uuestilaadimine) ja `handleJoinApplicationError(error)`; `data()`-sse `joinApplicationErrorMessage` (ja vajadusel `isProcessingJoinApplication`). Veateade `AlertDanger` komponendiga tabeli kohal.
- Uusi komponente ega route'e pole vaja.

## Vastuvõtu kriteeriumid

- [ ] ✓ nupp saadab `PUT /api/join-applications/{joinApplicationId}/confirm` selle rea `joinApplicationId`-ga
- [ ] ✕ nupp saadab `PUT /api/join-applications/{joinApplicationId}/reject` selle rea `joinApplicationId`-ga
- [ ] Mõlemad toimingud toimuvad kohe nupuvajutusel, ilma kinnitusmodaalita
- [ ] Pärast õnnestunud toimingut laetakse nimekiri uuesti ja menetletud rida ei kuvata
- [ ] Päringu ajal ei saa sama taotlust topelt menetleda (nupud keelatud)
- [ ] 403 `JOIN_APPLICATION_ALREADY_PROCESSED` ja 404 `PRIMARY_KEY_NOT_FOUND` korral kuvatakse backend'i `message` `AlertDanger` komponendiga ja nimekiri laetakse uuesti
- [ ] Muu vea (nt 500) korral suunatakse kasutaja veaviolule
- [ ] Veateade kaob järgmise nupuvajutuse alguses
- [ ] Tühi nimekiri kuvab tekstirea "Ootel liitumistaotlusi ei ole"
- [ ] Kinnitatud taotleja on pärast kinnitamist treeninggrupi liige (kontrolli vaates `TrainingsView` — taotleja näeb Täituvust ja nuppu "Registreeru")
