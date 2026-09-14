public class FileValidator {
    private FileValidator() {
    }

    public static boolean isValid(String filename) {
        return filename != null
                && !filename.contains("..")
                && !filename.contains("/")
                && !filename.contains("\\");
    }
}
