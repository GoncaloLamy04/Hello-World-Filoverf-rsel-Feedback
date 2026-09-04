# Unit Test Guide

## Navngivning
`metode_scenarie_forventetResultat`

Eksempel: `getById_idIsZero_throwsIllegalArgument`

## AAA: Arrange, Act, Assert
Strukturen hver test skal følge.

```java
@Test
void getById_validId_returnsCard() {
    // Arrange - sæt data op
    when(cardRepository.findById(1)).thenReturn(testCard);

    // Act - kør metoden
    Card result = cardService.getById(1);

    // Assert - tjek resultatet
    assertEquals(testCard, result);
}
```

## TDD: Test Driven Development
Skriv testen før koden.

1. Skriv en test der fejler (rød)
2. Skriv den mindste kode der får testen til at bestå (grøn)
3. Refaktorer koden uden at bryde testen (refaktor)

Også kaldet Red, Green, Refactor.

## Uden mock (ren logik)
Bruges når metoden ikke har eksterne afhængigheder.

```java
@Test
void fetchImageUrlByScryfallLink_invalidPrefix_returnsNull() {
    // Arrange
    String ugyldigtLink = "https://evil.com/card/tla/4/aang";

    // Act
    String result = scryfallService.fetchImageUrlByScryfallLink(ugyldigtLink);

    // Assert
    assertNull(result);
}
```

## Med mock (ekstern afhængighed)
Bruges når metoden kalder database, netværk eller filsystem.

```java
@Test
void fetchImageUrlBySetAndNumber_normalCard_returnsImageUrl() throws Exception {
    // Arrange
    URI uri = new URI("https", "api.scryfall.com", "/cards/tla/4", null);
    when(restTemplate.getForObject(uri, String.class)).thenReturn(NORMAL_IMAGE_JSON);

    // Act
    String result = scryfallService.fetchImageUrlBySetAndNumber("tla", "4");

    // Assert
    assertEquals("https://cards.scryfall.io/normal/front/a/b/abc.jpg", result);
}
```

## Tommelfingerregler

- En ting per test. Hvis testen fejler skal du vide præcis hvad der gik galt.
- Test grænser. Nul, negativt, null, tom string, ugyldige tegn. Ikke kun happy path.
- Test fejlscenarier. Kaster metoden den rigtige exception når noget går galt?
- Mock til afhængigheder. Alt der kalder database, netværk eller filsystem mockes.
- Ingen logik i tests. Ingen if, ingen loops. Tests skal være dumme og direkte.
- Test opførslen, ikke implementationen. Test hvad metoden gør, ikke hvordan den gør det indeni.
