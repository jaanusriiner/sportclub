# HomeView.vue — Balsamiq märkmed

Avaleht (Kodu) koos sisselogimise modaalaknaga. Struktuur vastab failile `balsamiq-markmete-struktuur.md`.

---

## Vaate märkmed

```text
Roll: Kõik rollid
Failinimi: HomeView.vue
Frontend rada: /

Vaatega seotud lisainfo:
Vaate avamisel laetakse backendist GET päringuga avalehe pilt ja tekst. Menüü link "Logi Sisse" avab modaalakna "Sisselogimine" väljadega email ja salasõna ning nuppudega "Logi Sisse" ja "Sulge".

Nupule "Logi Sisse" vajutades saadetakse backendile POST /api/login. Kui backend vastab errorCode'ga INCORRECT_CREDENTIALS, kuvatakse AlertDanger.vue's backend'i message väli ("Vale e-post või parool"). Eduka sisselogimise korral salvestatakse userId ja roleName sessionStorage'isse, kasutajale kuvatakse success message "login successful" ning ta suunatakse vaatele /training.

Nupule "Sulge" vajutades modaalaken suletakse (ilma API kutseta).
```

---

## API märkmed — POST /api/login

```text
API: POST /api/login

LoginRequestDto.java
Request body:
{
  "email": "jaanus@gmail.com",
  "password": "123"
}

LoginResponseDto.java
Response (200):
{
  "userId": 1,
  "roleName": "admin"
}

API teenuse lisainfo:
Süsteemist otsitakse email ja password abil kasutajat, kelle konto on aktiivne (user tabeli status = 'A').

Veateated:
HTTP: 403
errorCode: INCORRECT_CREDENTIALS
message: "Vale e-post või parool"
```
