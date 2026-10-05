import java.io.IOException;

public class Verify {
    public static void main(String[] args) {
        Git repository = new Git();

        try {
            repository.makeGitRepository();
            Git.createBlob("git/sha.txt");
            Git.updateIndex("git/sha.txt");
        } catch (IOException e) {
            System.err.println("Something happened:" + e.getMessage());
        }
    }
}
