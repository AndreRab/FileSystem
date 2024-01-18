public class Commands {
    public static final String PRINT_CATALOG = "ls";
    public static final String MAKE_DIR = "mkdir";
    public static final String REMOVE_DIR = "rmdir";
    public static final String MOVE_DIR = "mvdir";
    public static final String CREATE_FILE = "touch";
    public static final String ADD_TO_FILE = "echo";
    public static final String PRINT_FILE = "cat";
    public static final String DELETE_FILE = "delete";
    public static final String COPY_FILE = "copy";
    public static final String MOVE_FILE = "mv";
    public static final String HELP = "help";
    public static final String FIND_SUBDIR_ERROR = "Subdirectories don't exist";
    public static final String FIND_DIR_ERROR = "Directory doesn't exist";
    public static final String FIND_FILE_ERROR = "File doesn't exist";
    public static final String DIR_EXISTS_ERROR = "Directory already exists";
    public static final String FILE_EXISTS_ERROR = "File already exists";
    public static final String FOLDER_CYCLE_ERROR = "Move will provide folder cycle";
    public static void displayAllCommandsWithDescription(){
        System.out.println("MFS (My File System) - Custom file system in a single directory");
        System.out.println("Supported commands:");
        System.out.println("ls [dir_name] - List the contents of a directory with the given name.");
        System.out.println("mkdir [dir_name] - Create a directory with the given name.");
        System.out.println("rmdir [dir_name] - Remove a directory with the given name and its contents.");
        System.out.println("mvdir [src_dir] [target_dir] - Move the directory src_dir to target_dir.");
        System.out.println("touch [file_name] - Create an empty file with the given name.");
        System.out.println("echo [file_name] [content] - Append the content parameter to a file.");
        System.out.println("cat [file_name] - Display the contents of a file.");
        System.out.println("delete [file_name] - Delete a file with the given name.");
        System.out.println("copy [src_file] [target_dir] - Copy the file src_file to the target directory.");
        System.out.println("mv [src_file] [target_dir] - Move the file src_file to the target directory.");
        System.out.println("help - Display this help.");
        System.out.println("\nNote: Directory and file names should be fully qualified, e.g., root-123.txt.");

    }
}
