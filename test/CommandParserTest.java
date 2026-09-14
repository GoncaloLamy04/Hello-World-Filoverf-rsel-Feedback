import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.Test;

class CommandParserTest {
    @Test
    void parse_validCommand_returnsCommandRequest() {
        // Arrange
        String command = "GET|messi.txt";

        // Act
        CommandParser.CommandRequest result = CommandParser.parse(command);

        // Assert
        assertNotNull(result);
        assertEquals("GET|messi.txt", result.getRawCommand());
        assertEquals("messi.txt", result.getFilename());
    }

    @Test
    void parse_null_returnsNull() {
        // Arrange
        String command = null;

        // Act
        CommandParser.CommandRequest result = CommandParser.parse(command);

        // Assert
        assertNull(result);
    }

    @Test
    void parse_emptyString_returnsNull() {
        // Arrange
        String command = "";

        // Act
        CommandParser.CommandRequest result = CommandParser.parse(command);

        // Assert
        assertNull(result);
    }

    @Test
    void parse_missingGetPrefix_returnsNull() {
        // Arrange
        String command = "messi.txt";

        // Act
        CommandParser.CommandRequest result = CommandParser.parse(command);

        // Assert
        assertNull(result);
    }

    @Test
    void parse_commandWithoutFilename_returnsNull() {
        // Arrange
        String command = "GET|";

        // Act
        CommandParser.CommandRequest result = CommandParser.parse(command);

        // Assert
        assertNull(result);
    }

    @Test
    void parse_invalidCommandPrintsErrorMessage() {
        // Arrange
        String command = "POST|messi.txt";
        ByteArrayOutputStream errorOutput = new ByteArrayOutputStream();
        PrintStream originalError = System.err;
        System.setErr(new PrintStream(errorOutput));

        try {
            // Act
            CommandParser.CommandRequest result = CommandParser.parse(command);

            // Assert
            assertNull(result);
            assertEquals(true, errorOutput.toString().contains("GET|"));
        } finally {
            System.setErr(originalError);
        }
    }

    @Test
    void parse_allowsPathTraversalFilename_whenPrefixIsValid() {
        // Arrange
        String command = "GET|../hemmelig.txt";

        // Act
        CommandParser.CommandRequest result = CommandParser.parse(command);

        // Assert
        assertNotNull(result);
        assertEquals("GET|../hemmelig.txt", result.getRawCommand());
        assertEquals("../hemmelig.txt", result.getFilename());
    }
}
