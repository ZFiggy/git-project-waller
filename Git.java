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
import java.util.Comparator;
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

    public static ArrayList<String> createWorkingList() throws IOException {
        ArrayList<String> workingList = new ArrayList<String>();
        List<String> indexList = Files.readAllLines(Path.of("git", "index"));
        ArrayList<String[]> tempList = new ArrayList<>();
        for (int i = 0; i < indexList.size(); i++) {
            String[] parts = indexList.get(i).split(" ", 2);
            String[] listEntry = { parts[1].toLowerCase(), indexList.get(i) };
            tempList.add(listEntry);
        }

        for (int i = 1; i < tempList.size(); i++) {
            String[] current = tempList.get(i);
            int j = i - 1;
            while (j >= 0 && tempList.get(j)[0].compareTo(current[0]) > 0) {
                tempList.set(j + 1, tempList.get(j));
                j--;
            }
            tempList.set(j + 1, current);
        }

        for (int i = 0; i < tempList.size(); i++) {
            workingList.add("blob " + tempList.get(i)[1]);
        }

        return workingList;
    }

    public static String createTree(String dirPath) throws IOException {
        Path startPath = Paths.get(dirPath);
        Stream<Path> stream = Files.walk(startPath);
        List<Path> allFilePaths = stream.filter(Files::isRegularFile).collect(Collectors.toList());
        for (Path filePath : allFilePaths) {
            if (filePath.startsWith("git") || filePath.startsWith(".git")) {
            } else if (filePath.getFileName().toString().equals(".DS_Store")) {
            } else {
                updateIndexAndBlob(filePath.toString());
            }
        }
        stream.close();
        return createRootTree();
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

    public static String createRootTree() throws IOException {
        return buildTree(createWorkingList(), "");
    }

    public static String buildTree(ArrayList<String> workingList, String dirPath) throws IOException {
        String prefix = "";
        if (!dirPath.equals("")) {
            prefix = dirPath + "/";
        }
        ArrayList<String> subfolders = new ArrayList<String>();
        for (String entry : workingList) {
            String path = entry.split(" ", 3)[2];
            if (path.startsWith(prefix)) {
                String restOfPath = path.substring(prefix.length());
                if (restOfPath.contains("/")) {
                    String subfolder = prefix + restOfPath.substring(0, restOfPath.indexOf("/"));
                    if (!subfolders.contains(subfolder)) {
                        subfolders.add(subfolder);
                    }
                }
            }
        }
        for (String subfolder : subfolders) {
            String subfolderHash = buildTree(workingList, subfolder);
            replaceWithTreeLine(workingList, subfolder, subfolderHash);
        }
        return createTree(workingList, dirPath);
    }

    public static void replaceWithTreeLine(ArrayList<String> workingList, String dirPath, String hash) {
        ArrayList<String> replacingList = new ArrayList<String>();
        boolean added = false;
        for (String entry : workingList) {
            if (entry.split(" ", 3)[2].startsWith(dirPath + "/")) {
                if (!added) {
                    replacingList.add("tree " + hash + " " + dirPath);
                    added = true;
                }
            } else {
                replacingList.add(entry);
            }
        }
        workingList.clear();
        workingList.addAll(replacingList);
    }

    public static void main(String[] args) {
        Git repository = new Git();

        try {
            repository.makeGitRepository();
            createTree("");
        } catch (IOException e) {
            System.err.println("Something happened: " + e.getMessage());
        }
    }
}
