# Copilot Instructions

Dette er en øvelsesopgave i Programmering, hvor målet ikke kun er kørende kode, men også at bruge en AI-agent systematisk gennem processen Forstå → Planlæg → Implementér → Test → Review → Forbedr → Forklar.

## Opgaven

Simpel filoverførsel over TCP i Java. En klient beder serveren om en fil, serveren sender den som bytes.

Case: TechFile Solutions skal give medarbejdere på forskellige kontorer mulighed for at hente filer fra en central server.

## Build og kør

```bash
javac FileServer.java FileClient.java
java FileServer
java FileClient
```

(Opdatér hvis I ender med Maven eller en anden mappestruktur.)

## Arkitektur

To klasser som minimum, flere må gerne oprettes hvis det giver mening:

- `FileServer.java`, lytter på port 5000
- `FileClient.java`, forbinder og henter en fil

Ingen multithreading, det er ikke et krav. Programmet skal kun håndtere én klient ad gangen.

## Protokol

Klient sender: `GET|filnavn`

Server svarer enten:

- `OK` efterfulgt af filens bytes
- `ERROR|besked` hvis filen ikke findes eller ikke er tilladt

Filen læses som bytes på serveren og skrives som bytes på klienten, den sendes ikke direkte gennem socket'en som et objekt eller en tekststreng.

## Sikkerhed

Serveren skal afvise filnavne der forsøger at bryde ud af den tilladte mappe, fx `GET|../hemmelig.txt`. Kun filer i serverens tilladte filmappe må kunne hentes.

## Fejlhåndtering og ressourcer

Både klient og server skal:

- håndtere fejl med forståelige beskeder
- lukke sockets og streams korrekt, også ved fejl (try-with-resources er en god idé)

## Kodestil

- Kommentarer og fejlbeskeder på dansk, identifiers på engelsk
- Enkel og letforståelig kode frem for smarte løsninger, koden skal kunne forklares til eksamen
- Ingen unødvendig kompleksitet, KISS

## Test

Se `unit-test-guide.md` for testkonventioner (AAA, navngivning `metode_scenarie_forventetResultat`, en ting per test).

Test som minimum manuelt:

| Test | Forventet resultat |
|---|---|
| Normal fil | Filen overføres |
| Større fil | Hele filen overføres |
| Ukendt fil | Fejlbesked |
| `../hemmelig.txt` | Afvises |
| Server ikke startet | Forståelig fejl |

## Arbejdsproces med agenten

Bed ikke om hele løsningen på én gang. Arbejd i små trin, og bed om forklaring efter hvert trin:

1. Server starter, klient forbinder
2. `GET|filnavn` implementeres
3. `OK`/`ERROR` håndteres
4. Filen sendes som bytes
5. Filen gemmes hos klienten

Ved code review: agenten må pege på fejl i TCP-kommunikation, streams, ressourcelukning, sikkerhed og unødvendig kompleksitet, men må ikke selv ændre koden i den fase.
