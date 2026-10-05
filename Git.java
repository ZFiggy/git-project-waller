import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class Git {


    public void makeGitRepository() throws IOException {
        Path folderPath = Paths.get("git");
        Path objectsPath = folderPath.resolve("objects");
        Path indexPath = folderPath.resolve("index");
        Path headPath = folderPath.resolve("HEAD");

        boolean alreadyExists = Files.isDirectory(folderPath) && Files.isDirectory(objectsPath)
                && Files.isRegularFile(indexPath) && Files.isRegularFile(headPath);

        Files.createDirectories(folderPath);
        Files.createDirectories(objectsPath);

        if (!Files.exists(indexPath)) {
            Files.createFile(indexPath);
        }

        if (!Files.exists(headPath)) {
            Files.createFile(headPath);
        }

        if (alreadyExists) {
            System.out.println("Git Repository Already Exists");
        } else {
            System.out.println("Git Repository Created");
        }
    }

    public static String hashFile(String filePath) throws IOException {
        Path path = Path.of(filePath);

        if (!Files.isRegularFile(path)) {
            throw new IOException("no such file: " + filePath);
        }

        byte[] fileBytes = Files.readAllBytes(path);

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-1 is not available", e);
        }

        byte[] hash = digest.digest(fileBytes);

        return HexFormat.of().formatHex(hash);
    }

    public static void main(String[] args) {
        Git repository = new Git();

        try {
            repository.makeGitRepository();
            System.out.println(hashFile("git/test.txt"));
        } catch (IOException e) {
            System.err.println("Could not initialize repository: " + e.getMessage());
        }
    }
}
