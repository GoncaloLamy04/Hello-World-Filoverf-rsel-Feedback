import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileValidatorTest {
    @Test
    void acceptsNormalFilename() {
        assertTrue(FileValidator.isValid("sample.txt"));
    }

    @Test
    void acceptsEmptyFilenameBecauseItHasNoForbiddenCharacters() {
        assertTrue(FileValidator.isValid(""));
    }

    @Test
    void rejectsNullFilename() {
        assertFalse(FileValidator.isValid(null));
    }

    @Test
    void rejectsParentDirectorySequence() {
        assertFalse(FileValidator.isValid("../secret.txt"));
        assertFalse(FileValidator.isValid("file..txt"));
    }

    @Test
    void rejectsForwardSlash() {
        assertFalse(FileValidator.isValid("subdir/file.txt"));
    }

    @Test
    void rejectsBackslash() {
        assertFalse(FileValidator.isValid("subdir\\file.txt"));
    }
}
