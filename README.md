# TCP filoverførsel

## Gruppe

Gruppe 4, Hello World
Navne: Nicki, Goncalo, Mattias

## Løsningen

`FileServer` lytter på port 5000 og kører nu i en løkke, den betjener klienter én ad gangen, men lukker ikke efter den første, den venter bare på den næste. `FileClient` forbinder til serveren og sender en kommando i formatet `GET|filnavn`. Skriver man bare selve filnavnet uden `GET|` foran, bliver det automatisk til en GET-kommando, siden det alligevel er den eneste kommando der findes. Skriver man derimod noget med en `|` i, der ikke starter med `GET|` (case-insensitivt, `get|` virker også), bliver det afvist som ugyldigt i stedet for at blive gættet på.

Protokollen bruger `DataInputStream`/`DataOutputStream` med `writeUTF`/`readUTF` til beskeder, ikke `BufferedReader`/`PrintWriter`/`Scanner`. Ved succes svarer serveren `OK`, efterfulgt af filstørrelsen (`writeLong`) og selve filens bytes. Ved fejl svarer serveren `ERROR|besked`. Filen læses som bytes på serveren og skrives som bytes på klienten, den sendes aldrig som tekst eller objekt gennem socket'en.

Filnavnevalidering og kommando-parsing er samlet i to separate klasser, `FileValidator` og `CommandParser`, i stedet for at ligge duplikeret inde i selve klient/server-koden. Serveren afgrænser adgang til en tilladt mappe (`files/`) og afviser filnavne der indeholder `..`, `/` eller `\`, og validerer desuden filens kanoniske sti mod den tilladte mappe som et ekstra lag. Klienten har samme grundvalidering, men det er serverens tjek der er autoritativt, en klient kan i princippet bygges om til at omgå sin egen validering.

`main()` i både klient og server er nu kun et opstartspunkt, selve logikken ligger i `runClient()`/`startServer()` og en række mindre, navngivne metoder derunder.

## AI-agent

### Plan

Den første plan fra AI'en foreslog et tekstbaseret beskedformat med `\n` som terminator, `GET|filnavn\n` og `OK|<størrelse>\n<data>`.

Vi ændrede det til `writeUTF`/`readUTF`/`writeLong` med `DataInputStream`/`DataOutputStream` i stedet. Begrundelsen var at `BufferedReader`/`Scanner` kan bufre ekstra bytes fra socket'en når man blander tekstlæsning og binær datalæsning på samme stream, hvilket risikerer at miste de første bytes af filen.

### Implementering

Agenten hjalp mest med at implementere hvert trin isoleret, server starter og klient forbinder, GET-kommandoen, OK/ERROR-håndtering, selve byte-overførslen, og med at forklare ændringerne bagefter så vi kunne følge med i koden løbende i stedet for at få det hele på én gang.

### Kritisk vurdering

Et forslag vi fulgte: filnavnevalideringen i FileServer. Simpel, gør det den skal, nem at forklare til eksamen.

Senere bad vi agenten om et rigtigt code review, uden at den måtte ændre noget. Den påpegede at vores `..`-tjek kun fanger de åbenlyse forsøg, ikke fx en fil der reelt ligger uden for `files/` uden at have `..` i navnet. Det gav mening, så vi tilføjede et ekstra lag, canonical path-tjek.

Et forslag vi droppede igen: socket timeout. Vi bad den tilføje det, testede det, og fjernede det bagefter. Gav ikke mening til en opgave der kun kører én klient ad gangen i et kontrolleret setup.

Da vi senere skulle implementere feedback fra en anden gruppes review, refererede agenten til to nye klasser, `FileValidator` og `CommandParser`, i sin kode, men glemte reelt at oprette og committe filerne. Vi opdagede det først da projektet slet ikke kunne kompilere på den branch vi troede var den nyeste, det viste sig også at arbejdet var landet på en forkert, forældet branch i første omgang. Vi fandt fejlen ved selv at forsøge at bygge projektet fra bunden, ikke ved at stole på agentens egen "success"-besked.

Da vi rettede rækkefølgen på overskrivnings-spørgsmålet, introducerede en efterfølgende ændring en `Stream closed`-fejl, fordi to forskellige læsemekanismer (`System.console()` og en `BufferedReader`) begge forsøgte at bruge `System.in`, og den ene lukkede den for den anden. Det fandt vi ved at teste selv, ikke ved at stole på at "build success" betød koden virkede.

## Test

| Test | Resultat |
|---|---|
| Normal fil | Bestået, fil overført og gemt korrekt i `downloads/` |
| Stor fil | Bestået, testet med `files/stor-fil-test.txt` (680.000 bytes), størrelse bekræftet identisk med kildefilen |
| Ukendt fil | Bestået, `ERROR\|File not found` |
| ../hemmelig.txt | Bestået, testet direkte mod serveren udenom klientens eget filter, både det simple tjek og canonical path-valideringen afviser korrekt med `ERROR\|Ugyldigt filnavn` |
| Server ikke startet | Bestået, `Connection refused: connect` |
| Flere klienter i træk | Bestået, serveren betjener to klienter efter hinanden uden at genstarte |
| Overskrivning af eksisterende fil | Bestået, spørger j/n før noget sendes til serveren |
| Case-insensitiv GET | Bestået, `GET|`, `get|` og `Get|` genkendes alle |

## Peer review

Vigtigste feedback fra den anden gruppe (Toby's gruppe):
- Serveren kørte ikke i et loop og lukkede efter én klient
- Klienten tvang alt input uden pipe til automatisk at blive en GET-kommando, uanset om det gav mening
- Ingen fejlrapportering ved afbrudt filoverførsel
- Ingen advarsel ved overskrivning af eksisterende fil
- Manglende struktur, filnavnevalidering var duplikeret i både klient og server

Hvad ændrede vi efter reviewet?
Alle fem punkter. Vi tilføjede en løkke så serveren betjener flere klienter efter hinanden, strammede kommandoparsingen så kun eksplicit gyldige kommandoer accepteres, tilføjede et tjek der rapporterer præcist hvor mange bytes der reelt blev modtaget hvis overførslen afbrydes, en overskrivningsadvarsel med bekræftelse, og udtrak filnavnevalidering og kommando-parsing til to separate, testbare klasser i stedet for duplikeret kode.

Hvad valgte vi ikke at ændre, og hvorfor?
Vi genindførte senere en let, automatisk GET-præfiksning, men kun for rent filnavn uden nogen pipe i det hele taget, ikke for alt input som den oprindelige, kritiserede version gjorde. Vi vurderede det som en reel forskel, den gamle version skjulte forsøg på ugyldige kommandoer, den nye letter kun det almindelige tilfælde, uden at maskere noget.

## Refleksion

1. Hvor var AI mest nyttig?

Til selve implementeringen, den skrev det meste af boilerplate-koden, GET/OK/ERROR-flowet, streams osv, mens vi kunne fokusere på at forstå og teste det trin for trin i stedet for at skrive det hele selv.

2. Hvornår skulle vi være kritiske over for AI?

Flere gange faktisk. Første gang da den fjernede path traversal-tjekket bare fordi vi bad om det, uden at tænke over at det brød et krav. Anden gang da den refererede til to nye klasser i sin kode uden faktisk at oprette og committe dem, vi opdagede det kun fordi vi selv forsøgte at bygge projektet. Tredje gang da en lille ændring introducerede en `Stream closed`-fejl, fordi to forskellige måder at læse fra konsollen på kom i konflikt, den så ikke selv problemet, det gjorde vi først da vi testede det manuelt.

3. Hvordan kontrollerede vi at koden faktisk virkede?

Kørte alle testene manuelt selv, server og klient i hver sin proces, tjekkede output linje for linje, ofte flere gange i træk for at fange fejl der ikke nødvendigvis viste sig første gang. Til path traversal skrev vi en lille testklient der gik direkte mod serveren, fordi klientens eget filter ellers gjorde at vi aldrig testede serverens egen validering. Vi lærte hen ad vejen at "kompilerer uden fejl" og "kører uden at crashe" ikke er det samme som "gør det rigtige", flere af de fejl vi fandt viste sig kun ved at faktisk gennemføre den fulde brugerinteraktion, ikke kun se på om koden byggede.