import com.bigsort.util.FilesHandler;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;


public class TestFilesHandler {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();


    @Test
    public void testGetBufferedReadersWithEmptyFiles() throws IOException {
        assertNull(FilesHandler.getBufferedReaders(null));
    }

    @Test
    public void testGetBufferedReadersWithNotExistingFiles() throws IOException {
        File notExisting = new File(folder.getRoot() + "/temp.txt");
        File existing = folder.newFile("temp2.txt");
        assertNull(FilesHandler.getBufferedReaders(Arrays.asList(notExisting, existing)));
    }

    @Test
    public void testGetBufferedReadersSuccess() throws IOException {
        assertNotNull(FilesHandler.getBufferedReaders(Arrays.asList(folder.newFile("temp.txt"))));
    }

    @Test
    public void testCreateTempFilePrefixIsShort() throws IOException {
        folder.newFile("temp.txt");
        FilesHandler.createTempFile("", folder.getRoot().getAbsolutePath());
    }

    @Test
    public void testCreateTempFileSuccess() throws IOException {
        folder.newFile("ABACABAtemp.txt");
        FilesHandler.createTempFile("ABACABA", folder.getRoot().getAbsolutePath());
        assertEquals(2, folder.getRoot().listFiles((dir, name) -> name.startsWith("")).length);
    }

    @Test
    public void testCreateTempFileManySuccess() throws IOException {
        for (int i = 0; i < 100; ++i) {
            FilesHandler.createTempFile("ABACABA", folder.getRoot().getAbsolutePath());
        }
        assertEquals(100, folder.getRoot().listFiles((dir, name) -> name.startsWith("")).length);
    }

    @Test
    public void testGetMatchedFiles() throws IOException {
        for (int i = 0; i <= 9; ++i) {
            folder.newFile("temp" + i + ".txt");
        }
        assertEquals(10, FilesHandler.getMatchedFiles(folder.getRoot().getAbsolutePath(), "temp").size());
        for (int i = 0; i <= 9; ++i) {
            assertEquals(1, FilesHandler.getMatchedFiles(folder.getRoot().getAbsolutePath(), "temp" + i).size());
        }
    }

    @Test
    public void testMergeSortedFiles() throws IOException {
        List<File> files = new ArrayList<>();
        for (int i = 0; i <= 9; ++i) {
            File f = folder.newFile("temp" + i + ".txt");
            BufferedWriter w = new BufferedWriter(new FileWriter(f));
            w.write(String.valueOf(i));
            w.flush();
            w.close();
            files.add(f);
        }
        File resultFile = folder.newFile("temp" + ".txt");
        assertTrue(FilesHandler.mergeSortedFiles(files, resultFile));

        ArrayList<String> result = new ArrayList<>();
        String next;
        BufferedReader reader = new BufferedReader(new FileReader(resultFile));
        while ((next = reader.readLine()) != null) {
            result.add(next);
        }

        ArrayList<String> expected = new ArrayList<>();
        expected.addAll(result);

        Collections.sort(expected);
        assertEquals(expected, result);
    }


    @Test
    public void testMergeSortedFilesOneByOne() throws IOException {
        List<File> files = new ArrayList<>();
        for (int i = 0; i < 10; ++i) {
            File f = folder.newFile("temp" + i + ".txt");
            BufferedWriter w = new BufferedWriter(new FileWriter(f));
            w.write(String.valueOf(i));
            w.close();
            files.add(f);
        }

        for (int i = 0; i < 9; ++i) {
            File resultFile = folder.newFile("tempres" + i + ".txt");
            assertTrue(FilesHandler.mergeSortedFiles(files.subList(i, i + 2), resultFile));
            BufferedReader reader = new BufferedReader(new FileReader(resultFile));
            String next;
            ArrayList<String> result = new ArrayList<>();
            while ((next = reader.readLine()) != null) {
                result.add(next);
            }
            ArrayList<String> expected = new ArrayList<>();
            expected.addAll(result);
            Collections.sort(expected);
            assertEquals(expected, result);
            assertEquals(2, result.size());
        }
    }

    @Test
    public void testCleanUp() throws IOException {
        for (int i = 0; i < 10; ++i) {
            folder.newFile("temp" + i + ".txt");
        }
        assertEquals(0, FilesHandler.cleanUp(folder.getRoot().getAbsolutePath(), "temp").size());
        assertEquals(0, folder.getRoot().listFiles((dir, name) -> name.startsWith("temp")).length);
    }

    @Test
    public void testCleanUpNotingIsFound() throws IOException {
        folder.newFile("demp.txt");
        assertEquals(0, FilesHandler.cleanUp(folder.getRoot().getAbsolutePath(), "temp").size());
        assertEquals(1, folder.getRoot().listFiles((dir, name) -> name.startsWith("")).length);
    }
}


