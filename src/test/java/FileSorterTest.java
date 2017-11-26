import com.bigsort.FilesSorter;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class FileSorterTest {

    public static final String OUTPUT = "output.txt";
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testDoTheSortJobSingleWorker() throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = FilesSorter.class.getDeclaredMethod("doTheSortJob", new Class<?>[]{});
        method.setAccessible(true);
        FilesSorter sorter = new FilesSorter(createUnsortedFiles(1), 1, 2,
                folder.getRoot().getAbsolutePath(), getOutputPath());
        assertTrue((Boolean) method.invoke(sorter, new Object[]{}));
        assertEquals(5, folder.getRoot().listFiles((dir, name) -> name.startsWith(FilesSorter.PROJECT_PREFIX)).length);
    }

    @Test
    public void testDoTheSortJobMoreWorkersThanBatch() throws IOException, NoSuchMethodException,
            InvocationTargetException, IllegalAccessException {
        Method method = FilesSorter.class.getDeclaredMethod("doTheSortJob", new Class<?>[]{});
        method.setAccessible(true);
        FilesSorter sorter = new FilesSorter(createUnsortedFiles(1), 5, 2,
                folder.getRoot().getAbsolutePath(), getOutputPath());
        assertTrue((Boolean) method.invoke(sorter, new Object[]{}));
    }

    @Test
    public void testDoTheSortJobTwoWorkers() throws IOException, NoSuchMethodException,
            InvocationTargetException, IllegalAccessException {
        Method method = FilesSorter.class.getDeclaredMethod("doTheSortJob", new Class<?>[]{});
        method.setAccessible(true);
        FilesSorter sorter = new FilesSorter(createUnsortedFiles(2), 2, 5,
                folder.getRoot().getAbsolutePath(), getOutputPath());
        assertTrue((Boolean) method.invoke(sorter, new Object[]{}));
    }

    @Test
    public void testDoTheSortJobTwoWorkersTwoFiles() throws IOException, NoSuchMethodException,
            InvocationTargetException, IllegalAccessException {
        Method method = FilesSorter.class.getDeclaredMethod("doTheSortJob", new Class<?>[]{});
        method.setAccessible(true);
        FilesSorter sorter = new FilesSorter(createUnsortedFiles(2), 1, 5,
                folder.getRoot().getAbsolutePath(), getOutputPath());
        assertTrue((Boolean) method.invoke(sorter, new Object[]{}));
        File[] resFiles = folder.getRoot().listFiles((dir, name) -> name.startsWith(FilesSorter.PROJECT_PREFIX));
        assertEquals(4, resFiles.length);

        ArrayList<String> result = new ArrayList<>();
        for (int i = 0; i < resFiles.length; ++i) {
            BufferedReader reader = new BufferedReader(new FileReader(resFiles[i]));
            String next;
            while ((next = reader.readLine()) != null) {
                result.add(next);
            }
        }

        assertEquals(20, result.size());
    }

    @Test
    public void testSortEasy() throws IOException, NoSuchMethodException, InvocationTargetException,
            IllegalAccessException {
        FilesSorter sorter = new FilesSorter(createUnsortedFiles(3), 1, 2,
                folder.getRoot().getAbsolutePath(), getOutputPath());
        assertTrue(sorter.sort());
        File[] output = folder.getRoot().listFiles((dir, name) -> name.startsWith(OUTPUT));
        assertTrue(output.length == 1);
        BufferedReader reader = new BufferedReader(new FileReader(output[0]));

        ArrayList<String> result = new ArrayList<>();
        ArrayList<String> expected = new ArrayList<>();
        String next;
        while ((next = reader.readLine()) != null) {
            result.add(next);
            expected.add(next);
        }

        Collections.sort(expected);
        assertEquals(expected, result);
        assertEquals(30, result.size());
        assertEquals(0, folder.getRoot().listFiles((dir, name) -> name.startsWith(FilesSorter.PROJECT_PREFIX)).length);
    }

    @Test
    public void testSortHeavy() throws IOException, NoSuchMethodException, InvocationTargetException,
            IllegalAccessException {
        FilesSorter sorter = new FilesSorter(createUnsortedFiles(30), 5, 9,
                folder.getRoot().getAbsolutePath(), getOutputPath());
        assertTrue(sorter.sort());
        File[] output = folder.getRoot().listFiles((dir, name) -> name.startsWith(OUTPUT));
        assertTrue(output.length == 1);
        BufferedReader reader = new BufferedReader(new FileReader(output[0]));

        ArrayList<String> result = new ArrayList<>();
        ArrayList<String> expected = new ArrayList<>();
        String next;
        while ((next = reader.readLine()) != null) {
            result.add(next);
            expected.add(next);
        }

        Collections.sort(expected);
        assertEquals(expected, result);
        assertEquals(300, result.size());
        assertEquals(0, folder.getRoot().listFiles((dir, name) -> name.startsWith(FilesSorter.PROJECT_PREFIX)).length);
    }


    /**
     * Creates {@code count} file in the {@code folder}. This file has 10 lines with numbers 9 down to 0.
     */

    private List<File> createUnsortedFiles(int count) throws IOException {
        List<File> res = new ArrayList<>();
        for (int i = 0; i < count; ++i) {
            File f = folder.newFile("temp" + i + ".txt");
            BufferedWriter w = new BufferedWriter(new FileWriter(f));
            for (int j = 0; j <= 9; ++j) {
                w.write(String.valueOf(9 - j));
                w.newLine();
            }
            w.close();
            res.add(f);
        }

        return res;
    }

    private String getOutputPath() {
        return folder.getRoot().getAbsolutePath() + File.separator + OUTPUT;
    }
}
