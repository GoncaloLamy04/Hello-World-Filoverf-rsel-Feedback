# TCP filoverførsel

## Gruppe

Gruppe 4, Hello World
Navne: Nicki, Goncalo, Mattias

## Løsningen

`FileServer` lytter på port 5000 og håndterer én klient ad gangen (single-threaded, ikke et krav i opgaven). `FileClient` forbinder til serveren og sender en kommando i formatet `GET|filnavn`.

Protokollen bruger `DataInputStream`/`DataOutputStream` med `writeUTF`/`readUTF` til beskeder, ikke `BufferedReader`/`PrintWriter`/`Scanner`. Ved succes svarer serveren `OK`, efterfulgt af filstørrelsen (`writeLong`) og selve filens bytes. Ved fejl svarer serveren `ERROR|besked`. Filen læses som bytes på serveren og skrives som bytes på klienten, den sendes aldrig som tekst eller objekt gennem socket'en.

Serveren afgrænser adgang til en tilladt mappe (`files/`) og afviser filnavne der indeholder `..`, `/` eller `\`, for at forhindre path traversal. Klienten har samme validering, men det er serverens tjek der er autoritativt, da en klient i princippet kan bygges om til at omgå sin egen validering.

## AI-agent

### Plan

Den første plan fra AI'en foreslog et tekstbaseret beskedformat med `\n` som terminator, `GET|filnavn\n` og `OK|<størrelse>\n<data>`.

Vi ændrede det til `writeUTF`/`readUTF`/`writeLong` med `DataInputStream`/`DataOutputStream` i stedet. Begrundelsen var at `BufferedReader`/`Scanner` kan bufre ekstra bytes fra socket'en når man blander tekstlæsning og binær datalæsning på samme stream, hvilket risikerer at miste de første bytes af filen. `DataInputStream`/`DataOutputStream` håndterer længden af data eksplicit og undgår den fælde.

### Implementering

Agenten hjalp mest med at implementere hvert trin isoleret, server starter og klient forbinder, `GET`-kommandoen, `OK`/`ERROR`-håndtering, selve byte-overførslen, og med at forklare ændringerne bagefter så vi kunne følge med i koden løbende i stedet for at få det hele på én gang.

### Kritisk vurdering

Et AI-forslag vi fulgte: Filnavnevalideringen i `FileServer`, afvisning af `..`, `/` og `\` før filen slås op, holder sig til planens krav og er enkel at forklare.

Et AI-forslag vi ændrede eller afviste: Vi bad agenten tilføje `socket.setSoTimeout(5000)` med håndtering af `SocketTimeoutException`. Efter at have testet det, valgte vi at fjerne det igen.

Hvorfor? Opgaven kører single-threaded med én klient ad gangen i et kontrolleret testmiljø, en timeout løste ikke noget krav og tilføjede kompleksitet uden en tilsvarende gevinst. Det er lettere at holde koden enkel og forklarbar til eksamen uden.

## Test

| Test | Resultat                                                                                                                                             |
|---|------------------------------------------------------------------------------------------------------------------------------------------------------|
| Normal fil | Bestået, fil overført og gemt korrekt i `downloads/`                                                                                                 |
| Stor fil | Bestået, testet med `files/stor-fil-test.txt` (680.000 bytes), størrelse bekræftet identisk med kildefilen                                                                                                                       |
| Ukendt fil | Bestået, `ERROR\|File not found`                                                                                                                     |
| ../hemmelig.txt | Bestået, testet med `../agent-samtale-log.md` via en midlertidig testklient udenom klientens eget filter, serveren svarede `ERROR\|Ugyldigt filnavn` |
| Server ikke startet | Bestået, `Connection refused: connect`                                                                                                               |

## Peer review

Vigtigste feedback fra den anden gruppe:

Hvad ændrede vi efter reviewet?

Hvad valgte vi eventuelt ikke at ændre, og hvorfor?

## Refleksion

1. Hvor var AI mest nyttig?

2. Hvornår skulle I være kritiske over for AI?

3. Hvordan kontrollerede I, at AI-genereret kode faktisk virkede?