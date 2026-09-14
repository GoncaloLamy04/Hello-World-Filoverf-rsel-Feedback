public class FileValidator {
    private static final String PARENT_DIRECTORY = "..";
    private static final String FORWARD_SLASH = "/";
    private static final String BACKWARD_SLASH = "\\";

    public static boolean isValid(String filename) {
        if (filename == null) {
            return false;
        }

        String trimmedFilename = filename.trim();
        return !trimmedFilename.contains(PARENT_DIRECTORY)
                && !trimmedFilename.contains(FORWARD_SLASH)
                && !trimmedFilename.contains(BACKWARD_SLASH);
    }
}
