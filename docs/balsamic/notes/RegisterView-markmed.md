# RegisterView.vue — Balsamiq märkmed

Uue kasutaja registreerimise vorm. Struktuur vastab failile `balsamiq-markmete-struktuur.md`.

---

## Vaate märkmed

```text
Roll: Külastaja (pole sisse logitud)
Failinimi: RegisterView.vue
Frontend rada: /register

Vaatega seotud lisainfo:
Enne saatmist kontrollitakse, kas kõik väljad on täidetud, kas Salasõna ja Korda salasõna väärtused ühtivad ning kas "Nõustun Tingimustega" märkeruut on märgitud — kui mitte, kuvatakse AlertDanger.vue komponendiga vastav teade. Salasõna kordust ja märkeruudu olekut backendile ei saadeta.

Piirkond rippmenüü sisu tuleb päringust GET /api/areas ja Spordiala huvid rippmenüü (mitmikvalik) päringust GET /api/sports — mõlemad tehakse vaate avamisel. Backend'i veateated (nt USER_UNAVAILABLE) kuvatakse samuti AlertDanger.vue's message väljast.

Eduka registreerimise korral salvestatakse userId ja roleName sessionStorage'isse ning kasutaja suunatakse avalehele (Kodu).
```

---

## API märkmed — POST /api/register

```text
API: POST /api/register

RegisterRequestDto.java
Request body:
{
  "firstName": "Mari",
  "lastName": "Maasikas",
  "areaId": 1,
  "phoneNumber": 55512345,
  "email": "mari.maasikas@gmail.com",
  "password": "parool123",
  "sportIds": [1, 3]
}

RegisterResponseDto.java
Response (200):
{
  "userId": 1,
  "roleName": "customer"
}

API teenuse lisainfo:
Luuakse uus kasutaja rolliga customer ja aktiivse staatusega (user tabeli status = 'A'), temaga seotud profiil (profile) ning valitud spordialade huvid (user_sport). E-post peab olema unikaalne aktiivsete kasutajate seas. Salasõna kordust ja tingimustega nõustumist kontrollitakse ainult frontendis.

Veateated:
HTTP: 403
errorCode: USER_UNAVAILABLE
message: "Sellise e-postiga aktiivne kasutaja on süsteemis juba olemas"

HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'areaId' väärtusega: 99"

HTTP: 404
errorCode: PRIMARY_KEY_NOT_FOUND
message: "Ei leidnud primary keyd 'sportId' väärtusega: 99"

HTTP: 400
errorCode: INCORRECT_INPUT
message: "email: Sisesta korrektne e-posti aadress"
```

---

## API märkmed — GET /api/areas

```text
API: GET /api/areas

AreaDto.java
Response (200):
[
  {
    "areaId": 1,
    "areaName": "Harjumaa"
  },
  {
    "areaId": 2,
    "areaName": "Läänemaa"
  },
  {
    "areaId": 3,
    "areaName": "Saaremaa"
  },
  {
    "areaId": 4,
    "areaName": "Hiiumaa"
  },
  {
    "areaId": 5,
    "areaName": "Pärnumaa"
  }
]

API teenuse lisainfo:
—

Veateated: —
```

---

## API märkmed — GET /api/sports

```text
API: GET /api/sports

SportDto.java
Response (200):
[
  {
    "sportId": 1,
    "sportName": "Tennis"
  },
  {
    "sportId": 2,
    "sportName": "Football"
  },
  {
    "sportId": 3,
    "sportName": "Basketball"
  },
  {
    "sportId": 4,
    "sportName": "Golf"
  }
]

API teenuse lisainfo:
—

Veateated: —
```
