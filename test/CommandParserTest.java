import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CommandParserTest {
    @Test
    void parsesGetCommandAndPreservesRawCommand() {
        CommandParser parsed = CommandParser.parse("GET|sample.txt");

        assertNotNull(parsed);
        assertEquals("sample.txt", parsed.getFilename());
        assertEquals("GET|sample.txt", parsed.getRawCommand());
    }

    @Test
    void parsesExplicitGetWithEmptyFilename() {
        CommandParser parsed = CommandParser.parse("GET|");

        assertNotNull(parsed);
        assertEquals("", parsed.getFilename());
        assertEquals("GET|", parsed.getRawCommand());
    }

    @Test
    void rejectsNullCommand() {
        assertNull(CommandParser.parse(null));
    }

    @Test
    void rejectsEmptyCommand() {
        assertNull(CommandParser.parse(""));
    }

    @Test
    void rejectsCommandWithoutExplicitGetPrefix() {
        assertNull(CommandParser.parse("sample.txt"));
        assertNull(CommandParser.parse("file.txt"));
    }

    @Test
    void rejectsLowercaseGetPrefix() {
        assertNull(CommandParser.parse("get|sample.txt"));
    }
}
