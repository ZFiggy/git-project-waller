import java.io.IOException;

public class Verify {
    public static void main(String[] args) {
        Git repository = new Git();

        try {
            repository.makeGitRepository();
            Git.updateIndexAndBlob("test.txt");
            Git.updateIndexAndBlob("sha.txt");
            Git.updateIndexAndBlob("duplicate.txt");
            Git.updateIndexAndBlob("empty.txt");
        } catch (IOException e) {
            System.err.println("Something happened: " + e.getMessage());
        }
    }
}
