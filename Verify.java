import java.io.IOException;

public class Verify {
    public static void main(String[] args) {
        Git repository = new Git();

        try {
            repository.makeGitRepository();
            System.out.println(Git.createTree(""));
        } catch (IOException e) {
            System.err.println("Something happened: " + e.getMessage());
        }
    }
}
