import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MFS {
    private static final String initPath = "root";
    private static final String rootFileExtension = ".mfs";
    private static final Path rootFilePath = Path.of(initPath, ".", initPath + rootFileExtension);
    private static final String DIRECTORY_INDICATOR = "D";
    private static final String FILE_INDICATOR = "F";
    public static void main(String[] args) {
        MFS mfs = new MFS();
        mfs.mainInternal(args);
    }
    void mainInternal(String... args){
        initRootFolder();
        for (int i = 0; i < args.length; i++){
            String command = args[i];
            switch (command){
                case Commands.HELP -> Commands.displayAllCommandsWithDescription();
                case Commands.MAKE_DIR -> createDirectory(args[++i]);
                case Commands.REMOVE_DIR -> removeDirectory(args[++i]);
                case Commands.CREATE_FILE -> createFile(args[++i]);
                case Commands.DELETE_FILE -> removeFile(args[++i]);
                case Commands.ADD_TO_FILE -> writeToFile(args[++i], args[++i]);
                case Commands.PRINT_FILE -> printFile(args[++i]);
                case Commands.PRINT_CATALOG -> printDirectory(args[++i]);
                case Commands.MOVE_FILE -> moveFile(args[++i], args[++i]);
                case Commands.COPY_FILE -> copyFile(args[++i], args[++i]);
                case Commands.MOVE_DIR -> moveDirectory(args[++i], args[++i]);
            }
        }
    }
    private static void createFile(String name){
        try {
            String fileName = translateToPathInRoot(name);
            if(!checkFileExist(fileName)) {
                Files.createFile(Path.of(initPath, ".", fileName));
                writeToRootFile(fileName, FILE_INDICATOR);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private static void removeFile(String name){
        String fileName = translateToPathInRoot(name);
        if(checkFileExist(fileName)){
            Path filePath = Path.of(initPath, ".", fileName);
            try {
                Files.delete(filePath);
                removeFromRootFile(fileName, FILE_INDICATOR);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    private static void writeToFile(String name, String info){
        String fileName = translateToPathInRoot(name);
        if(!checkFileExist(fileName)){
            throw new RuntimeException(Commands.FIND_FILE_ERROR);
        }
        try {
            Path filePath = Path.of(initPath, ".", fileName);
            Files.writeString(filePath, info, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private static void moveFile(String source, String target){
        String oldFileName = source.equals(initPath)? "" : translateToPathInRoot(source);
        String newFileName = target.equals(initPath)? "" : translateToPathInRoot(target) + "-";
        newFileName += getNameOfFile(source);
        if(!checkFileExist(oldFileName)){
            throw new RuntimeException(Commands.FIND_FILE_ERROR);
        }
        try {
            Files.move(Path.of(initPath, ".", oldFileName), Path.of(initPath, ".", newFileName));
            writeToRootFile(newFileName, FILE_INDICATOR);
            removeFromRootFile(oldFileName, FILE_INDICATOR);
        } catch (IOException e) {
            throw new RuntimeException(Commands.FILE_EXISTS_ERROR);
        }
    }
    private static void copyFile(String source, String target){
        String oldFileName = source.equals(initPath)? "" : translateToPathInRoot(source);
        String newFileName = target.equals(initPath)? "" : translateToPathInRoot(target) + "-";
        newFileName += getNameOfFile(source);
        if(!checkFileExist(oldFileName)){
            throw new RuntimeException(Commands.FIND_FILE_ERROR);
        }
        try {
            Files.copy(Path.of(initPath, ".", oldFileName), Path.of(initPath, ".", newFileName));
            writeToRootFile(newFileName, FILE_INDICATOR);
            removeFromRootFile(oldFileName, FILE_INDICATOR);
        } catch (IOException e) {
            throw new RuntimeException(Commands.FILE_EXISTS_ERROR);
        }
    }
    private static void printFile(String name){
        String fileName = translateToPathInRoot(name);
        if(!checkFileExist(fileName)){
            throw new RuntimeException(Commands.FIND_FILE_ERROR);
        }
        Path filePath = Path.of(initPath, ".", fileName);
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toString()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private static void createDirectory(String name){
        String fileName = translateToPathInRoot(name);
        if(!checkFileExist(fileName)) {
            try {
                Files.createDirectory(Path.of(initPath, ".", fileName));
                writeToRootFile(fileName, DIRECTORY_INDICATOR);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            throw new RuntimeException(Commands.DIR_EXISTS_ERROR);
        }
    }
    private static void removeDirectory(String name){
        String fileName = translateToPathInRoot(name);
        if(checkFileExist(fileName)) {
            try {
                Files.delete(Path.of(initPath, ".", fileName));
                removeFromRootFile(fileName, DIRECTORY_INDICATOR);
                removeDirectoryFromRootFile(fileName);
                removeDirectoryFromRoot(fileName);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            throw new RuntimeException(Commands.DIR_EXISTS_ERROR);
        }
    }
    private static void moveDirectory(String source, String target){
        String targetFolder = target.equals(initPath)? "" : target.substring(initPath.length() + 1);
        if(!targetFolder.equals("")) targetFolder += "-";
        String sourceFolder = source.substring(initPath.length() + 1);
        String[] sourceByParts = source.split("-");
        String currentFolderName = sourceByParts[sourceByParts.length - 1];
        if(target.contains(source)){
            throw new RuntimeException(Commands.FOLDER_CYCLE_ERROR);
        }
        try {
            List<String> lines = Files.readAllLines(rootFilePath, StandardCharsets.UTF_8);
            for (String line : lines){
                if(line.contains(sourceFolder)){
                    String id = line.charAt(0) == 'F'? FILE_INDICATOR : DIRECTORY_INDICATOR;
                    String oldFileName = line.substring(1);
                    String fileName = line.substring(sourceFolder.length() + 1);
                    String newFileName  = targetFolder + currentFolderName + fileName;
                    Files.move(Path.of(initPath,".", oldFileName), Path.of(initPath, ".", newFileName));
                    writeToRootFile(newFileName, id);
                    removeFromRootFile(oldFileName, id);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private static void printDirectory(String name) {
        String fileName = name.equals(initPath)? "" : translateToPathInRoot(name);
        if(!checkFileExist(fileName) && !name.equals(initPath)) {
            throw new RuntimeException(Commands.FIND_DIR_ERROR);
        }
        String folderPathInRoot = name.equals(initPath)? "" : name.substring(initPath.length() + 1);
        try {
            List<String> lines = Files.readAllLines(rootFilePath, StandardCharsets.UTF_8);
            lines.sort(Collections.reverseOrder());
            for (String line : lines) {
                String label = line.startsWith(FILE_INDICATOR) ? "File: " : "Directory: ";
                String itemName = !name.equals(initPath)? line.substring(1) : line;
                if(itemName.startsWith(folderPathInRoot)){
                    if(itemName.equals(folderPathInRoot)) continue;
                    itemName = itemName.substring(folderPathInRoot.length() + 1);
                    String[] parts = itemName.split("-");
                    if(parts.length == 1){
                        System.out.println(label + parts[0]);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading directory: " + e.getMessage());
        }
    }
    private static String translateToPathInRoot(String name){
        return name.substring(initPath.length() + 1);
    }

    private static boolean checkFileExist(String fileName){
        return Files.exists(Path.of(initPath, ".", fileName));
    }

    private static String getNameOfFile(String fileName){
        String[] directories = fileName.split("-");
        if(directories.length == 1){
            return fileName;
        }
        else{
            return  directories[directories.length - 1];
        }
    }

    private static void writeToRootFile(String name, String begin) {
        try {
            Path rootFilePath = Path.of(initPath, ".", initPath + rootFileExtension);
            String entry = begin + name + "\n";
            Files.writeString(rootFilePath, entry, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private static void removeFromRootFile(String fileName, String begin) {
        try {
            List<String> lines = Files.readAllLines(rootFilePath, StandardCharsets.UTF_8);
            List<String> modifiedLines = lines.stream()
                    .filter(line ->!line.equals(begin + fileName))
                    .collect(Collectors.toList());
            Files.write(rootFilePath, modifiedLines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void removeDirectoryFromRootFile(String directoryPath) {
        try {
            List<String> lines = Files.readAllLines(rootFilePath, StandardCharsets.UTF_8);
            List<String> modifiedLines = lines.stream()
                    .filter(line ->!line.contains(FILE_INDICATOR + directoryPath))
                    .collect(Collectors.toList());
            Files.write(rootFilePath, modifiedLines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private static void removeDirectoryFromRoot(String directoryName) {
        try (Stream<Path> paths = Files.walk(Path.of(initPath))) {
            paths
                    .filter(path -> path.getFileName().toString().contains(directoryName))
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            System.err.println(e.getMessage());
                        }
                    });
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
    public void deleteRoot(){
        Path rootPath = Path.of(initPath);
        try (Stream<Path> paths = Files.walk(rootPath)) {
            paths
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        System.err.println(e.getMessage());
                    }
                });
            Files.delete(rootPath);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
    private static void initRootFolder(){
        Path rootPath = Path.of(initPath);
        if(!Files.exists(rootPath)){
            try {
                Files.createDirectory(rootPath);
                Files.createFile(Paths.get(initPath,".", initPath + rootFileExtension));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}