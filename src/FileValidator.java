public class FileValidator {
    public static boolean isValid(String filename) {
        if (filename == null) {
            return false;
        }

        String trimmedFilename = filename.trim();
        return !trimmedFilename.contains("..")
                && !trimmedFilename.contains("/")
                && !trimmedFilename.contains("\\");
    }
}
