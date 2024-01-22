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
    private static final String rootFolder = "root";
    private static final String rootFileExtension = ".mfs";
    private static final char DIRECTORY_INDICATOR = 'D';
    private static final char FILE_INDICATOR = 'F';

    public static void main(String[] args) {
        MFS mfs = new MFS();
        mfs.mainInternal(args);
    }

    void mainInternal(String... args) {
        initRootFolder();
        for (int i = 0; i < args.length; i++) {
            String command = args[i];
            switch (command) {
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

    private static void createFile(String name) {
        Path curPath = translateToPath(name);
        if (!Files.exists(curPath)) {
            try {
                Files.createFile(curPath);
                writeToRootFile(findRootFilePath(name), getNameOfFile(name), FILE_INDICATOR);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void removeFile(String name) {
        Path filePath = translateToPath(name);
        if (Files.exists(filePath)) {
            try {
                Files.delete(filePath);
                removeFromRootFile(findRootFilePath(name), getNameOfFile(name), FILE_INDICATOR);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void writeToFile(String name, String info) {
        Path filePath = translateToPath(name);
        if (!Files.exists(filePath)) {
            throw new RuntimeException(Commands.FIND_FILE_ERROR);
        }
        try {
            Files.writeString(filePath, info, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void moveFile(String source, String target) {
        Path sourcePath = translateToPath(source);
        Path targetPath = translateToPath(target + "-" + getNameOfFile(source));
        try {
            Files.move(sourcePath, targetPath);
            writeToRootFile(translateToPathMfs(target), getNameOfFile(source), FILE_INDICATOR);
            removeFromRootFile(findRootFilePath(source), getNameOfFile(source), FILE_INDICATOR);
        } catch (IOException e) {
            throw new RuntimeException(Commands.FILE_EXISTS_ERROR);
        }
    }

    private static void copyFile(String source, String target) {
        Path sourcePath = translateToPath(source);
        Path targetPath = translateToPath(target + "-" + getNameOfFile(source));
        try {
            Files.copy(sourcePath, targetPath);
            writeToRootFile(translateToPathMfs(target), getNameOfFile(source), FILE_INDICATOR);
        } catch (IOException e) {
            throw new RuntimeException(Commands.FILE_EXISTS_ERROR);
        }
    }

    private static void printFile(String name) {
        Path filePath = translateToPath(name);
        if (!Files.exists(filePath)) {
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

    private static void createDirectory(String name) {
        Path curPath = translateToPathMfs(name);
        if (Files.exists(curPath)) {
            throw new RuntimeException(Commands.DIR_EXISTS_ERROR);
        }
        try {
            Files.createFile(curPath);
            writeToRootFile(findRootFilePath(name), getNameOfFile(name), DIRECTORY_INDICATOR);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private static void removeDirectory(String name) {
        Path mfsFilePath = translateToPathMfs(name);
        if (Files.exists(mfsFilePath)) {
            List<String> lines;
            try {
                lines = Files.readAllLines(mfsFilePath, StandardCharsets.UTF_8);
                for (String line : lines) {
                    if(line.charAt(0) == FILE_INDICATOR) removeFile(name + "-" + line.substring(1));
                    else removeDirectory(name + "-" + line.substring(1));
                }
                Files.delete(mfsFilePath);
                removeFromRootFile(findRootFilePath(name), getNameOfFile(name), DIRECTORY_INDICATOR);
            } catch (IOException e) {
                throw new RuntimeException(Commands.FIND_DIR_ERROR);
            }
        } else {
            throw new RuntimeException(Commands.FIND_DIR_ERROR);
        }
    }

    private static void moveDirectory(String source, String target) {
        if (target.contains(source)) {
            throw new RuntimeException(Commands.FOLDER_CYCLE_ERROR);
        }
        Path mfsFilePath = translateToPathMfs(source);
        if (Files.exists(mfsFilePath)) {
            List<String> lines;
            try {
                lines = Files.readAllLines(mfsFilePath, StandardCharsets.UTF_8);
                String newName = target + "-" + getNameOfFile(source);
                createDirectory(newName);
                for (String line : lines) {
                    if(line.charAt(0) == FILE_INDICATOR) moveFile(source + "-" + line.substring(1), newName);
                    else moveDirectory(source + "-" + line.substring(1), newName);
                }
                Files.delete(mfsFilePath);
                removeFromRootFile(findRootFilePath(source), getNameOfFile(source), DIRECTORY_INDICATOR);
            } catch (IOException e) {
                throw new RuntimeException(Commands.FIND_DIR_ERROR);
            }
        } else {
            throw new RuntimeException(Commands.FIND_DIR_ERROR);
        }
    }

    private static void printDirectory(String name) {
        Path path = translateToPathMfs(name);
        List<String> lines;
        try {
            lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            Collections.sort(lines);
            for (String line : lines) {
                String label = line.charAt(0) == FILE_INDICATOR ? "File: " : "Directory: ";
                String nameWithoutLabel = line.substring(1);
                System.out.println(label + nameWithoutLabel);
            }
        } catch (IOException e) {
            throw new RuntimeException(Commands.FIND_DIR_ERROR);
        }
    }

    private static Path translateToPath(String name) {
        return Paths.get(rootFolder,".", name);
    }
    private static Path translateToPathMfs(String name) {
        return Paths.get(rootFolder,".", name + rootFileExtension);
    }

    private static void writeToRootFile(Path rootFilePath, String name, char begin) {
        try {
            String entry = begin + name + "\n";
            Files.writeString(rootFilePath, entry, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void removeFromRootFile(Path rootFilePath, String name, char begin) {
        try {
            List<String> lines = Files.readAllLines(rootFilePath, StandardCharsets.UTF_8);
            List<String> modifiedLines = lines.stream()
                    .filter(line -> !line.equals(begin + name))
                    .collect(Collectors.toList());
            Files.write(rootFilePath, modifiedLines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Path findRootFilePath(String name) {
        String[] nameByDir = name.split("-");
        String nameOfFile = nameByDir[nameByDir.length - 1];
        String rootFile = name.substring(0, name.length() - nameOfFile.length() - 1);
        return Paths.get(rootFolder, ".", rootFile + rootFileExtension);
    }

    private static String getNameOfFile(String path){
        String[] nameByDir = path.split("-");
        return nameByDir[nameByDir.length - 1];
    }

    private static void initRootFolder() {
        Path rootPath = Paths.get(rootFolder);
        if (!Files.exists(rootPath)) {
            try {
                Files.createDirectory(rootPath);
                Files.createFile(Path.of(rootFolder,".", rootFolder + rootFileExtension));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public static void deleteInitFolder(){
        try {
            Stream<Path> files = Files.list(Paths.get(rootFolder));
            files.forEach(file -> {
                try {
                    Files.delete(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
