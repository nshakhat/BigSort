import com.bigsort.MapWorker;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MapWorkerTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testCallNotEvenBatch() throws IOException {
        ConcurrentLinkedQueue<File> q = new ConcurrentLinkedQueue<>();
        q.add(createUnsortedFile());
        MapWorker w = new MapWorker(q, 3, folder.getRoot().getAbsolutePath(), "testJob");
        assertTrue(w.call());
        // There is 10 lines and the batch equals to 3, i.e. we expect 4 files to be created
        File[] resultFiles = folder.getRoot().listFiles((dir, name) -> name.startsWith("testJob"));
        assertEquals(4, resultFiles.length);
        // Test that all these files are sorted
        List<String> allStrings = new ArrayList<>();
        for (File f : resultFiles) {
            BufferedReader reader = new BufferedReader(new FileReader(f));
            String next;
            ArrayList<String> result = new ArrayList<>();
            while ((next = reader.readLine()) != null) {
                result.add(next);
                allStrings.add(next);
            }
            ArrayList<String> expected = new ArrayList<>();
            expected.addAll(result);
            Collections.sort(expected);
            assertEquals(expected, result);
        }
        // Assert that nothing is lost
        assertEquals(10, allStrings.size());
    }

    @Test
    public void testCallEvenBatch() throws IOException {
        ConcurrentLinkedQueue<File> q = new ConcurrentLinkedQueue<>();
        q.add(createUnsortedFile());
        MapWorker w = new MapWorker(q, 5, folder.getRoot().getAbsolutePath(), "testJob");
        assertTrue(w.call());
        // There is 10 lines and the batch equals to 5, i.e. we expect 2 files to be created
        File[] resultFiles = folder.getRoot().listFiles((dir, name) -> name.startsWith("testJob"));
        assertEquals(2, resultFiles.length);
        // Test that all these files are sorted
        List<String> allStrings = new ArrayList<>();
        for (File f : resultFiles) {
            BufferedReader reader = new BufferedReader(new FileReader(f));
            String next;
            ArrayList<String> result = new ArrayList<>();
            while ((next = reader.readLine()) != null) {
                result.add(next);
                allStrings.add(next);
            }
            ArrayList<String> expected = new ArrayList<>();
            expected.addAll(result);
            Collections.sort(expected);
            assertEquals(expected, result);
        }
        // Assert that nothing is lost
        assertEquals(10, allStrings.size());
    }

    @Test
    public void testCallBigBatch() throws IOException {
        ConcurrentLinkedQueue<File> q = new ConcurrentLinkedQueue<>();
        q.add(createUnsortedFile());
        MapWorker w = new MapWorker(q, 30, folder.getRoot().getAbsolutePath(), "testJob");
        assertTrue(w.call());
        // There is 10 lines and the batch equals to 30, i.e. we expect 1 files to be created
        File[] resultFiles = folder.getRoot().listFiles((dir, name) -> name.startsWith("testJob"));
        assertEquals(1, resultFiles.length);
        // Test that all these files are sorted
        List<String> allStrings = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(resultFiles[0]));
        String next;
        ArrayList<String> result = new ArrayList<>();
        while ((next = reader.readLine()) != null) {
            result.add(next);
            allStrings.add(next);
        }
        ArrayList<String> expected = new ArrayList<>();
        expected.addAll(result);
        Collections.sort(expected);
        assertEquals(expected, result);
        // Assert that nothing is lost
        assertEquals(10, allStrings.size());
    }

    @Test
    public void testCallSmallBatch() throws IOException {
        ConcurrentLinkedQueue<File> q = new ConcurrentLinkedQueue<>();
        q.add(createUnsortedFile());
        MapWorker w = new MapWorker(q, 1, folder.getRoot().getAbsolutePath(), "testJob");
        assertTrue(w.call());
        // There is 10 lines and the batch equals to 1, i.e. we expect 10 files to be created
        File[] resultFiles = folder.getRoot().listFiles((dir, name) -> name.startsWith("testJob"));
        assertEquals(10, resultFiles.length);
        // Test that all these files are sorted
        List<String> allStrings = new ArrayList<>();
        for (File f : resultFiles) {
            BufferedReader reader = new BufferedReader(new FileReader(f));
            String next;
            while ((next = reader.readLine()) != null) {
                allStrings.add(next);
            }
        }
        // Assert that nothing is lost
        assertEquals(10, allStrings.size());
    }


    private File createUnsortedFile() throws IOException {
        File f = folder.newFile("temp.txt");
        BufferedWriter w = new BufferedWriter(new FileWriter(f));
        for (int i = 0; i <= 9; ++i) {
            w.write(String.valueOf(9 - i));
            w.newLine();
        }
        w.flush();
        w.close();
        return f;
    }
}

