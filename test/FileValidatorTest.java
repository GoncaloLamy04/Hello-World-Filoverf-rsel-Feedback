import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class FileValidatorTest {
    @Test
    void isValid_validFilename_returnsTrue() {
        // Arrange
        String filename = "messi.txt";

        // Act
        boolean result = FileValidator.isValid(filename);

        // Assert
        assertTrue(result);
    }

    @Test
    void isValid_null_returnsFalse() {
        // Arrange
        String filename = null;

        // Act
        boolean result = FileValidator.isValid(filename);

        // Assert
        assertFalse(result);
    }

    @Test
    void isValid_emptyString_returnsTrue() {
        // Arrange
        String filename = "";

        // Act
        boolean result = FileValidator.isValid(filename);

        // Assert
        assertTrue(result);
    }

    @Test
    void isValid_filenameContainingParentDirectory_returnsFalse() {
        // Arrange
        String filename = "../hemmelig.txt";

        // Act
        boolean result = FileValidator.isValid(filename);

        // Assert
        assertFalse(result);
    }

    @Test
    void isValid_filenameContainingForwardSlash_returnsFalse() {
        // Arrange
        String filename = "folder/messi.txt";

        // Act
        boolean result = FileValidator.isValid(filename);

        // Assert
        assertFalse(result);
    }

    @Test
    void isValid_filenameContainingBackslash_returnsFalse() {
        // Arrange
        String filename = "folder\\messi.txt";

        // Act
        boolean result = FileValidator.isValid(filename);

        // Assert
        assertFalse(result);
    }
}
