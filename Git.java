import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.nio.file.StandardCopyOption;

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

    public static void createBlob(String filePath) throws IOException {
        String hash = hashFile(filePath);

        Path originalPath = Path.of(filePath);
        Path blobPath = Path.of("git", "objects", hash);

        Files.copy(originalPath, blobPath, StandardCopyOption.REPLACE_EXISTING);
    }

    public static void updateIndex(String filePath) throws IOException {
        String hash = hashFile(filePath);
        String entry = hash + " " + filePath;

        Path indexPath = Path.of("git", "index");
        ArrayList<String> indexLines = new ArrayList<String>();
        BufferedReader indexReader = new BufferedReader(new FileReader(indexPath.toString()));
        String contents = indexReader.readLine();
        while (contents != null) {
            if (contents.length() > 41 && contents.substring(41).equals(filePath)) {
                contents = indexReader.readLine();
            } else {
                indexLines.add(contents);
                contents = indexReader.readLine();
            }
        }
        indexReader.close();

        indexLines.add(entry);
        StringBuilder newIndex = new StringBuilder(indexLines.get(0));
        for (int i = 1; i < indexLines.size(); i++) {
            newIndex.append("\n" + indexLines.get(i));
        }
        Files.writeString(indexPath, newIndex.toString());
    }

    public static void updateIndexAndBlob(String filePath) throws IOException {
        updateIndex(filePath);
        createBlob(filePath);
    }

    public static String createTree(String dirPath) throws IOException {
        Path startPath = Paths.get(dirPath);
        Stream<Path> stream = Files.walk(startPath);
        List<Path> allFilePaths = stream.filter(Files::isRegularFile).collect(Collectors.toList());
        for (Path filePath : allFilePaths) {
            updateIndexAndBlob(filePath.toString());
        }
        stream.close();

        ArrayList<String> workingList = new ArrayList<String>();
        List<String> indexList = Files.readAllLines(Path.of("git", "index"));
        for (int i = 0; i < indexList.size(); i++) {
            workingList.add("blob " + indexList.get(i));
        }

        return createTree(workingList, dirPath);
    }

    public static String createTree(ArrayList<String> workingList, String dirPath) throws IOException {
        StringBuilder treeBuilder = new StringBuilder();
        for (String entry : workingList) {
            String[] components = entry.split(" ", 3);
            if (components.length != 3) {
                throw new IllegalArgumentException("Working List made incorrectly.");
            }
            String type = components[0];
            String hash = components[1];
            String path = components[2];
            String fileParent;
            String fileName;
            if (path.contains("/")) {
                fileParent = path.substring(0, path.lastIndexOf("/"));
                fileName = path.substring(path.lastIndexOf("/") + 1);
            } else {
                fileParent = "";
                fileName = path;
            }
            if (fileParent.equals(dirPath)) {
                if (treeBuilder.length() > 0) {
                    treeBuilder.append("\n");
                }
                treeBuilder.append(type).append(" ").append(hash).append(" ").append(fileName);
            }
        }

        Path treePath = Path.of("git", "objects", "tempName");
        Files.writeString(treePath, treeBuilder.toString());
        String treeHash = hashFile(treePath.toString());
        Files.move(treePath, Path.of("git", "objects", treeHash), StandardCopyOption.REPLACE_EXISTING);
        return treeHash;
    }

    public static void main(String[] args) {
        Git repository = new Git();

        try {
            repository.makeGitRepository();
            updateIndexAndBlob("test.txt");
            updateIndexAndBlob("sha.txt");
            updateIndexAndBlob("duplicate.txt");
            updateIndexAndBlob("empty.txt");
        } catch (IOException e) {
            System.err.println("Something happened: " + e.getMessage());
        }
    }
}
