import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class MFSTest {
    private final MFS mfs = new MFS();
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    @BeforeEach
    public void setUpStreams() {
        System.setOut(new PrintStream(outputStream));
    }
    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
    }
    @AfterAll
    static void cleanUp() {
        MFS.deleteInitFolder();
    }

    @Test
    @Order(1)
    //Add file
    public void test1() {
        String command = "touch root-firstFile! ls root";
        mfs.mainInternal(command.split(" "));
        String consoleOutput = outputStream.toString().trim();
        System.out.println(consoleOutput);
        String expectedOutput = "File: firstFile!";
        assertEquals(expectedOutput, consoleOutput);
    }
    @Test
    @Order(2)
    //Add folder
    public void test2() {
        String command = "mkdir root-firstDirectory! ls root";
        mfs.mainInternal(command.split(" "));
        String consoleOutput = outputStream.toString().trim();
        System.out.println(consoleOutput);
        String expectedOutput = "Directory: firstDirectory!\r\n" +
                "File: firstFile!";
        assertEquals(expectedOutput, consoleOutput);
    }
    @Test
    @Order(3)
    //Delete folder
    public void test3() {
        String filePath = "target/firstFolder";
        long timeoutMillis = 2000;
        long startTime = System.currentTimeMillis();
        while (!Files.exists(Path.of(filePath))) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - startTime > timeoutMillis) {
                break;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        String command = "rmdir root-firstDirectory! ls root";
        mfs.mainInternal(command.split(" "));
        String consoleOutput = outputStream.toString().trim();
        System.out.println(consoleOutput);
        String expectedOutput = "File: firstFile!";
        assertEquals(expectedOutput, consoleOutput);
    }
    @Test
    @Order(4)
    //Move folder
    public void test4() {
        String command = "mkdir root-firstDirectory mkdir root-secondDirectory mvdir root-secondDirectory root-firstDirectory ls root-firstDirectory-secondDirectory";
        mfs.mainInternal(command.split(" "));
        String consoleOutput = outputStream.toString().trim();
        System.out.println(consoleOutput);
        String expectedOutput = "";
        assertEquals(expectedOutput, consoleOutput);
    }
    @Test
    @Order(5)
    //Print folder
    public void test5() {
        String command = "ls root";
        mfs.mainInternal(command.split(" "));
        String consoleOutput = outputStream.toString().trim();
        System.out.println(consoleOutput);
        String expectedOutput = "Directory: firstDirectory\r\n" +
                "File: firstFile!";
        assertEquals(expectedOutput, consoleOutput);
    }
    @Test
    @Order(6)
    //Write to file
    public void test6() {
        String command = "echo root-firstFile! hello,Jack cat root-firstFile!";
        mfs.mainInternal(command.split(" "));
        String consoleOutput = outputStream.toString().trim();
        System.out.println(consoleOutput);
        String expectedOutput = "hello,Jack";
        assertEquals(expectedOutput, consoleOutput);
    }
    @Test
    @Order(7)
    //Copy file
    public void test7() {
        String command = "copy root-firstFile! root-firstDirectory ls root-firstDirectory";
        mfs.mainInternal(command.split(" "));
        String consoleOutput = outputStream.toString().trim();
        System.out.println(consoleOutput);
        String expectedOutput = "Directory: secondDirectory\r\n" +
                "File: firstFile!";
        assertEquals(expectedOutput, consoleOutput);
    }
    @Test
    @Order(8)
    //remove file
    public void test8() {
        String command = "delete root-firstFile! ls root";
        mfs.mainInternal(command.split(" "));
        String consoleOutput = outputStream.toString().trim();
        System.out.println(consoleOutput);
        String expectedOutput = "Directory: firstDirectory";
        assertEquals(expectedOutput, consoleOutput);
    }
}