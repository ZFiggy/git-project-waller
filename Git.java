import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    public static void main(String[] args) {
        Git repository = new Git();

        try {
            repository.makeGitRepository();
        } catch (IOException e) {
            System.err.println("Could not initialize repository: " + e.getMessage());
        }
    }
}
