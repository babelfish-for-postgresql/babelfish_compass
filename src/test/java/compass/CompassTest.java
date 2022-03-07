/*
Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
SPDX-License-Identifier: Apache-2.0
*/

package compass;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.*;

import static org.junit.jupiter.api.Assertions.*;

public class CompassTest {

    @TempDir
    static Path tmpPath;

    static Path validInputFilePath;
    static Path emptyInputDirPath;
    static Path invalidInputFilePath;
    static Path oneLevelInputDirPath;

    final PrintStream out = System.out;
    final PrintStream err = System.err;
    ByteArrayOutputStream stdOut;
    ByteArrayOutputStream stdErr;

    @BeforeAll
    static void setup() throws IOException {
        /*
            ├── a.sql
            ├── b.sql
            ├── dir1
            │   └── child1
            │       ├── c.txt
            │       ├── child2
            │       │   └── e.xml
            │       └── d.dat
            ├── dir2
            │   ├── f.sql
            |   ├── g.sql
            |   ├── h.sql
            │   └── invalid.docx
            └── invalid
        */
        String tmpDir = tmpPath.toString();
        Files.createDirectories(Paths.get(tmpDir, "dir1", "child1", "child2"));
        Files.createDirectories(Paths.get(tmpDir, "dir2"));
        validInputFilePath = Paths.get(tmpDir, "a.sql");
        emptyInputDirPath = Paths.get(tmpDir, "invalid");
        invalidInputFilePath = Paths.get(tmpDir, "dir2", "invalid.docx");
        oneLevelInputDirPath = Paths.get(tmpDir, "dir2");
        Files.createFile(validInputFilePath);
        Files.createFile(Paths.get(tmpDir, "b.sql"));
        Files.createFile(Paths.get(tmpDir, "dir1", "child1", "c.txt"));
        Files.createFile(Paths.get(tmpDir, "dir1", "child1", "d.dat"));
        Files.createFile(Paths.get(tmpDir, "dir1", "child1", "child2", "e.xml"));
        Files.createFile(Paths.get(tmpDir, "dir2", "f.sql"));
        Files.createFile(Paths.get(tmpDir, "dir2", "g.sql"));
        Files.createFile(Paths.get(tmpDir, "dir2", "h.sql"));
        Files.createDirectory(emptyInputDirPath);
        Files.createFile(invalidInputFilePath);
    }

    @BeforeEach
    void init() {
        CompassTestUtils.resetStatics();

        stdOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(stdOut));
        stdErr = new ByteArrayOutputStream();
        System.setErr(new PrintStream(stdErr));
    }

    @AfterEach
    void teardown() {
        System.setOut(out);
        System.setErr(err);
    }

    @Test
    @DisplayName("Main No arguments")
    void testMainNoArgs() throws Exception {
        Compass.main(new String[]{});
        String output = new String(stdOut.toByteArray());
        assertTrue(output.contains("No arguments specified. Try -help"));
    }

    @Test
    @DisplayName("Main invalid arg list")
    void testConstructorInvalidArgList() throws Exception {
        Compass.main(new String[]{"foobar"});
        String output = new String(stdOut.toByteArray());
        assertTrue(output.contains("Must specify input file(s), or -list/-analyze/-reportonly/-reportoption"));
    }

    @Test
    @DisplayName("Empty Constructor")
    void testConstructorEmpty() {
        assertThrows(NullPointerException.class, () -> new Compass(null));

        new Compass(new String[]{});
        String output = new String(stdErr.toByteArray());
        assertTrue(output.contains("Must specify arguments. Try -help"));
    }

    @Test
    @DisplayName("Constructor args passed from command line")
    void testConstrutorCmdLineArgs() {
        String[] args = new String[] {"one", "two", "three"};
        new Compass(args);
        for (String arg : args) {
            assertTrue(Compass.cmdFlags.contains(arg));
        }
    }

    @Test
    @DisplayName("Constructor -version arg")
    void testConstructorVersionArg() {
        new Compass(new String[]{"-version"});
        assertTrue(Compass.showVersion);
    }

    @Test
    @DisplayName("Add Input File")
    void testAddInputFile_NoRecursion_SingleValidFile() {
        Compass compass = new Compass(new String[]{"test"});
        compass.addInputFile(validInputFilePath.toString());
        assertEquals(1, Compass.inputFiles.size(), "Add single valid input file");
        assertTrue(Compass.inputFiles.contains(validInputFilePath.toString()));
    }

    @Test
    @DisplayName("Add Input File No Recursion")
    void testAddInputFile_NoRecursion_IgnoreDirectories() {
        Compass compass = new Compass(new String[]{"test"});
        compass.addInputFile(tmpPath.toString());
        assertTrue(Compass.inputFiles.isEmpty(), "Without -recursive command line arg, directories are ignored");
    }

    @Test
    @DisplayName("Add Input File Literal Glob No Recursion")
    void testAddInputFile_NoRecursion_Glob() {
        Compass compass = new Compass(new String[]{"test"});
        // Can't use Paths.get...toString on Windows while testing for literal glob characters
        String file = tmpPath.toString() + FileSystems.getDefault().getSeparator() + "*.sql";
        compass.addInputFile(file);
        assertEquals(2, Compass.inputFiles.size(), "Without -recursive command line arg, directories are ignored");
    }

    @Test
    @DisplayName("Add Input File Literal Glob With Recursion")
    void testAddInputFile_Recursion_Glob() {
        Compass compass = new Compass(new String[]{"test", "-recursive"});
        // Can't use Paths.get...toString on Windows while testing for literal glob characters
        String file = tmpPath.toString() + FileSystems.getDefault().getSeparator() + "*.*";
        compass.addInputFile(file);
        assertEquals(9, Compass.inputFiles.size(), "With -recursive command line arg, all files are found");
    }

    @Test
    @DisplayName("Add Input File Literal Glob For Extension With Recursion")
    void testAddInputFile_Recursion_Glob_Extension() {
        Compass compass = new Compass(new String[]{"test", "-recursive"});
        // Can't use Paths.get...toString on Windows while testing for literal glob characters
        String file = tmpPath.toString() + FileSystems.getDefault().getSeparator() + "*.sql";
        compass.addInputFile(file);
        assertEquals(5, Compass.inputFiles.size(), "With -recursive command line arg, all files with extension are found");
    }

    @Test
    @DisplayName("Add Input File Empty Directory")
    void testAddInputFile_Recursion_EmptyDirectory() {
        Compass compass = new Compass(new String[]{"test", "-recursive"});
        assertTrue(Compass.recursiveInputFiles);
        compass.addInputFile(emptyInputDirPath.toString());
        assertTrue(Compass.inputFiles.isEmpty(), "Empty directories are ignored");
    }

    @Test
    @DisplayName("Add Input File Single Excluded File")
    void testAddInputFile_Recursion_InvalidFile() {
        Compass compass = new Compass(new String[]{"test", "-exclude", "*.docx"});
        compass.addInputFile(invalidInputFilePath.toString());
        assertTrue(Compass.inputFiles.isEmpty(), "Invalid filename extensions are ignored");
    }

    @Test
    @DisplayName("Add Input File Recursive Directory")
    void testAddInputFile_Recursion_DirectoryWalk() {
        Compass compass = new Compass(new String[]{"test", "-recursive"});
        compass.addInputFile(tmpPath.toString());
        assertEquals(9, Compass.inputFiles.size(), "Add a top level directory to recursively add");
    }

    @Test
    @DisplayName("Add Input File Recursive Directory with Excludes")
    void testAddInputFile_Recursion_DirectoryWalk_Excludes() {
        Compass compass = new Compass(new String[]{"test", "-recursive", "-exclude", ".{dat,xml,txt,docx}"});
        compass.addInputFile(tmpPath.toString());
        assertEquals(5, Compass.inputFiles.size(), "Add a top level directory to recursively add");
    }

    @Test
    @DisplayName("Add Input File Recursive Directory with Includes")
    void testAddInputFile_Recursion_DirectoryWalk_Includes() {
        Compass compass = new Compass(new String[]{"test", "-recursive", "-include", "*.sql"});
        compass.addInputFile(tmpPath.toString());
        assertEquals(5, Compass.inputFiles.size(), "Add a top level directory to recursively add");
    }

    @Test
    @DisplayName("Add Input File Mix Files and Directories")
    void testAddInputFile_Recursion_MixFilesAndDirectory() {
        Compass compass = new Compass(new String[]{"test", "-recursive"});
        compass.addInputFile(Paths.get(tmpPath.toString(), "dir1").toString());
        compass.addInputFile(validInputFilePath.toString());
        assertEquals(4, Compass.inputFiles.size(), "Add a mix of individual files and directories");
    }

    @Test
    @DisplayName("Glob syntax pattern when input path is only 1 level deep")
    void testGlobSyntaxAndPattern_ExcludeSingleLevelPath() {
        Compass compass = new Compass(new String[]{"test"});
        assertEquals("glob:*.docx", Compass.globSyntaxAndPattern(".docx", Paths.get("foo")));
    }

    @Test
    @DisplayName("Glob syntax pattern input path is multiple levels deep")
    void testGlobSyntaxAndPattern_ExcludeMultiLevelPath() {
        Compass compass = new Compass(new String[]{"test"});
        assertEquals("glob:**.docx", Compass.globSyntaxAndPattern(".docx", Paths.get("foo/bar")));
    }

    @Test
    @DisplayName("Glob syntax pattern when processing input recursively")
    void testGlobSyntaxAndPattern_ExcludeRecursivePath() {
        Compass compass = new Compass(new String[]{"test", "-recursive"});
        assertEquals("glob:**.docx", Compass.globSyntaxAndPattern(".docx", Paths.get("foo")));
    }

    @Test
    @DisplayName("Input files validation")
    void testInputFilesValid() {
        assertTrue(Compass.inputFilesValid(), "Empty input files list is valid if not -add or -delete");
        Compass compass = new Compass(new String[]{"test"});
        compass.addInputFile(validInputFilePath.toString());
        assertTrue(Compass.inputFilesValid());
    }

    @Test
    @DisplayName("Input files validation with -add")
    void testInputFilesValid_Add() {
        Compass compass = new Compass(new String[]{"test", "-add"});
        assertFalse(Compass.inputFilesValid(), "Empty input files list not allowed with -add");
        String output = new String(stdOut.toByteArray());
        assertTrue(output.contains("With -add, must specify input file(s)"));

        compass.addInputFile(validInputFilePath.toString());
        assertTrue(Compass.inputFilesValid(), "Input files must not be empty with -add");
    }

    @Test
    @DisplayName("Input files validation with -delete")
    void testInputFilesValid_Delete() {
        Compass compass = new Compass(new String[]{"test", "-delete"});
        assertFalse(Compass.inputFilesValid(), "Empty input files list not allowed with -delete");
        String output = new String(stdOut.toByteArray());
        assertTrue(output.contains("With -delete, must specify input file(s)"));

        compass.addInputFile(validInputFilePath.toString());
        assertTrue(Compass.inputFilesValid(), "Input files must not be empty with -delete");
    }
}
