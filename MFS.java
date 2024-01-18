import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.nio.file.attribute.BasicFileAttributes;

public class MFS {
    private static final String initPath = "root";
    private static final String rootFileExtension = ".mfs";
    private static final String DIRECTORY_INDICATOR = "D";
    private static final String FILE_INDOCTRINATOR = "F";

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
        Path curPath = translateToPath(name);
        if(!Files.exists(curPath)){
            try {
                Files.createFile(curPath);
                writeToRootFile(findRootFilePath(name), name, FILE_INDOCTRINATOR);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    private static void removeFile(String name){
        if(Files.exists(translateToPath(name))){
            Path filePath = translateToPath(name);
            try {
                Files.delete(filePath);
                removeFromRootFile(findRootFilePath(name), name, FILE_INDOCTRINATOR);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    private static void writeToFile(String name, String info){
        Path filePath = translateToPath(name);
        if(!Files.exists(filePath)){
            throw new RuntimeException(Commands.FIND_FILE_ERROR);
        }
        try {
            Files.writeString(filePath, info, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private static void moveFile(String source, String target){
        Path sourcePath = translateToPath(source);
        Path targetPath = Path.of(translateToPath(target).toString(),".", sourcePath.getFileName().toString());
        try {
            Files.move(sourcePath, targetPath);
            writeToRootFile(findRootFilePathToDirectory(target), source, FILE_INDOCTRINATOR);
            removeFromRootFile(findRootFilePath(source), source, FILE_INDOCTRINATOR);
        } catch (IOException e) {
            throw new RuntimeException(Commands.FILE_EXISTS_ERROR);
        }
    }
    private static void copyFile(String source, String target){
        Path sourcePath = translateToPath(source);
        Path targetPath = Path.of(translateToPath(target).toString(),".", sourcePath.getFileName().toString());
        try {
            Files.copy(sourcePath, targetPath);
            writeToRootFile(findRootFilePathToDirectory(target), source, FILE_INDOCTRINATOR);
        } catch (IOException e) {
            throw new RuntimeException(Commands.FILE_EXISTS_ERROR);
        }
    }
    private static void printFile(String name){
        Path filePath = translateToPath(name);
        if(!Files.exists(filePath)){
            throw new RuntimeException(Commands.FIND_FILE_ERROR);
        }
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
        Path curPath = translateToPath(name);
        if(Files.exists(curPath)){
            throw new RuntimeException(Commands.DIR_EXISTS_ERROR);
        }
        try {
            Files.createDirectory(curPath);
            writeToRootFile(findRootFilePath(name),name, DIRECTORY_INDICATOR);
            createMFSFile(curPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private static void removeDirectory(String name){
        if(Files.exists(translateToPath(name))){
            removeDirectoryFromSourceFolder(name);
            removeFromRootFile(findRootFilePath(name), name, DIRECTORY_INDICATOR);
        }
        else{
            throw new RuntimeException(Commands.FIND_DIR_ERROR);
        }
    }
    private static void moveDirectory(String source, String target){
        if(target.contains(source)){
            throw new RuntimeException(Commands.FOLDER_CYCLE_ERROR);
        }
        Path sourcePath = translateToPath(source);
        Path targetPath = Path.of(translateToPath(target).toString(),".", sourcePath.getFileName().toString());
        if(!Files.exists(sourcePath) || !Files.exists(translateToPath(target))){
            throw new RuntimeException(Commands.FIND_DIR_ERROR);
        }
        try {
            Files.move(sourcePath, targetPath);
            writeToRootFile(findRootFilePathToDirectory(target), source, DIRECTORY_INDICATOR);
            removeFromRootFile(findRootFilePath(source), source, DIRECTORY_INDICATOR);
        } catch (IOException e) {
            throw new RuntimeException(Commands.DIR_EXISTS_ERROR);
        }
    }
    private static void printDirectory(String name){
        Path path = (name.equals(initPath))? Paths.get(initPath,".", initPath + rootFileExtension) : findRootFilePathToDirectory(name);
        List<String> lines;
        try {
            lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            Collections.sort(lines);
            for (String line : lines) {
                String label = line.startsWith(FILE_INDOCTRINATOR) ? "File: " : "Directory: ";
                String nameWithoutLabel = line.substring(1);
                System.out.println(label + nameWithoutLabel);
            }
        } catch (IOException e) {
            throw new RuntimeException(Commands.FIND_DIR_ERROR);
        }
    }
    public static void removeDirectoryFromSourceFolder(String name){
        try {
            Files.walkFileTree(translateToPath(name), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private static Path translateToPath(String name) {
        String[] pathByDirectors = name.split("-");
        int indexOfDirectory = 0;
        Path curPath = Path.of(pathByDirectors[indexOfDirectory++]);
        while(pathByDirectors.length > indexOfDirectory){
            if(!Files.exists(curPath)){
                throw new RuntimeException(Commands.FIND_SUBDIR_ERROR);
            }
            curPath = Path.of(curPath.toString(), ".", pathByDirectors[indexOfDirectory++]);
        }
        return curPath;
    }

    private static void writeToRootFile(Path rootFilePath, String name, String begin) {
        try {
            String fileName = translateToPath(name).getFileName().toString();
            String entry = begin + fileName + "\n";
            Files.writeString(rootFilePath, entry, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private static void removeFromRootFile(Path rootFilePath, String name, String begin) {
        try {
            String fileName = translateToPath(name).getFileName().toString();
            List<String> lines = Files.readAllLines(rootFilePath, StandardCharsets.UTF_8);
            List<String> modifiedLines = lines.stream()
                    .filter(line ->!line.equals(begin + fileName))
                    .collect(Collectors.toList());
            Files.write(rootFilePath, modifiedLines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private static Path findRootFilePath(String name){
        String[] pathByDirectors = name.split("-");
        int indexOfDirectory = 0;
        Path curPath = Path.of(pathByDirectors[indexOfDirectory++]);
        while(pathByDirectors.length - 1 > indexOfDirectory){
            if(!Files.exists(curPath)){
                throw new RuntimeException(Commands.FIND_SUBDIR_ERROR);
            }
            curPath = Path.of(curPath.toString(), ".", pathByDirectors[indexOfDirectory++]);
        }
        String thisDirectoryName = pathByDirectors[indexOfDirectory - 1];
        return curPath.resolve( thisDirectoryName + rootFileExtension);
    }
    private static Path findRootFilePathToDirectory(String name){
        String[] pathByDirectors = name.split("-");
        int indexOfDirectory = 0;
        Path curPath = Path.of(pathByDirectors[indexOfDirectory++]);
        while(pathByDirectors.length > indexOfDirectory){
            if(!Files.exists(curPath)){
                throw new RuntimeException(Commands.FIND_SUBDIR_ERROR);
            }
            curPath = Path.of(curPath.toString(), ".", pathByDirectors[indexOfDirectory++]);
        }
        String thisDirectoryName = pathByDirectors[indexOfDirectory - 1];
        return curPath.resolve( thisDirectoryName + rootFileExtension);
    }
    private static void createMFSFile(Path path){
        String folderName = path.getFileName().toString();
        Path rootFilePath = Path.of(path.toString(), ".", folderName + rootFileExtension);
        try {
            Files.createFile(rootFilePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
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