# TCP filoverførsel

## Gruppe

Gruppe 4, Hello World
Navne: Nicki, Goncalo, Mattias

## Løsningen

`FileServer` lytter på port 5000 og håndterer én klient ad gangen (single-threaded, ikke et krav i opgaven). `FileClient` forbinder til serveren og sender en kommando i formatet `GET|filnavn`.

Protokollen bruger `DataInputStream`/`DataOutputStream` med `writeUTF`/`readUTF` til beskeder, ikke `BufferedReader`/`PrintWriter`/`Scanner`. Ved succes svarer serveren `OK`, efterfulgt af filstørrelsen (`writeLong`) og selve filens bytes. Ved fejl svarer serveren `ERROR|besked`. Filen læses som bytes på serveren og skrives som bytes på klienten, den sendes aldrig som tekst eller objekt gennem socket'en.

Serveren afgrænser adgang til en tilladt mappe (`files/`) og afviser filnavne der indeholder `..`, `/` eller `\`, for at forhindre path traversal, og validerer desuden filens kanoniske sti mod den tilladte mappe som et ekstra lag. Klienten har samme grundvalidering, men det er serverens tjek der er autoritativt, da en klient i princippet kan bygges om til at omgå sin egen validering.

## AI-agent

### Plan

Den første plan fra AI'en foreslog et tekstbaseret beskedformat med `\n` som terminator, `GET|filnavn\n` og `OK|<størrelse>\n<data>`.

Vi ændrede det til `writeUTF`/`readUTF`/`writeLong` med `DataInputStream`/`DataOutputStream` i stedet. Begrundelsen var at `BufferedReader`/`Scanner` kan bufre ekstra bytes fra socket'en når man blander tekstlæsning og binær datalæsning på samme stream, hvilket risikerer at miste de første bytes af filen. `DataInputStream`/`DataOutputStream` håndterer længden af data eksplicit og undgår den fælde.

### Implementering

Agenten hjalp mest med at implementere hvert trin isoleret, server starter og klient forbinder, GET-kommandoen, OK/ERROR-håndtering, selve byte-overførslen, og med at forklare ændringerne bagefter så vi kunne følge med i koden løbende i stedet for at få det hele på én gang.

Efter det formelle review bad vi den også dele main() i FileServer op i mindre metoder, handleRequest og resolveValidatedFile, for bedre struktur uden at ændre logikken. Vi opdagede at den første version duplikerede filbygningen, så vi bad den rette det til at bygge filen ét sted i stedet for to.

### Kritisk vurdering


Et forslag vi fulgte: filnavnevalideringen i FileServer. Simpel, gør det den skal, nem at forklare til eksamen.

Senere bad vi agenten om et rigtigt code review, uden at den måtte ændre noget. Den påpegede at vores `..`-tjek kun fanger de åbenlyse forsøg, ikke fx en fil der reelt ligger uden for `files/` uden at have `..` i navnet. Det gav mening, så vi tilføjede et ekstra lag, canonical path-tjek, oven på det vi allerede havde.

Et forslag vi droppede igen: socket timeout. Vi bad den tilføje det, testede det, og fjernede det bagefter. Gav ikke mening til en opgave der kun kører én klient ad gangen i et kontrolleret setup, bare ekstra kode uden nogen reel gevinst.

Samme review foreslog også at håndtere afbrudte overførsler bedre. Droppede den også, det var ikke noget opgaven krævede, og vi gad ikke bygge noget vi ikke skulle bruge.

## Test

| Test | Resultat |
|---|---|
| Normal fil | Bestået, fil overført og gemt korrekt i `downloads/` |
| Stor fil | Bestået, testet med `files/stor-fil-test.txt` (680.000 bytes), størrelse bekræftet identisk med kildefilen |
| Ukendt fil | Bestået, `ERROR\|File not found` |
| ../hemmelig.txt | Bestået, testet med path traversal-forsøg direkte mod serveren udenom klientens filter, fx `../conversation.md`, både `contains`-tjekket og den senere tilføjede canonical path-validering afviser korrekt med `ERROR\|Ugyldigt filnavn` |
| Server ikke startet | Bestået, `Connection refused: connect` |

## Peer review

Vigtigste feedback fra den anden gruppe:

[udfyld når vi har fået den]

Hvad ændrede vi efter reviewet?

[udfyld]

Hvad valgte vi ikke at ændre, og hvorfor?

[udfyld]

## Refleksion

1. Hvor var AI mest nyttig?

Til selve implementeringen, den skrev det meste af boilerplate-koden, GET/OK/ERROR-flowet, streams osv, mens vi kunne fokusere på at forstå og teste det trin for trin i stedet for at skrive det hele selv.

2. Hvornår skulle vi være kritiske over for AI?

Da den fjernede path traversal-tjekket bare fordi vi bad om det uden at tænke over at det brød et krav. Vi opdagede det senere og skulle have den sat tilbage. God påmindelse om at den bare gør hvad man beder om, uden at den nødvendigvis tænker på om det stadig opfylder kravene.

3. Hvordan kontrollerede vi at koden faktisk virkede?

Kørte alle testene manuelt selv, server og klient i hver sin proces, tjekkede output linje for linje. Til path traversal skrev vi en lille testklient der gik direkte mod serveren, fordi klientens eget filter ellers gjorde at vi aldrig testede serverens egen validering.