import com.bigsort.Main;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class MainTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testRequiredParams() throws IOException, ParseException {
        String[] args = new String[]{"-i", folder.getRoot().getAbsolutePath() + File.separator + "temp.txt",
                "-b", "5", "-w", "88",
                "-wd", folder.newFolder("wd").getAbsolutePath(),
                "-o", folder.getRoot().getAbsolutePath() + File.separator + "out.txt"};

        Options options = new Options();
        Main.buildRequiredOptions(options);
        Main.buildOptionalOptions(options);

        assertTrue(options.hasOption(Main.INPUT));
        assertTrue(options.hasOption(Main.OUTPUT));
        assertTrue(options.hasOption(Main.BATCH));

        CommandLineParser parser = new DefaultParser();
        parser.parse(options, args);
    }

    @Test(expected = ParseException.class)
    public void testInputMissed() throws IOException, ParseException {
        String[] args = new String[]{
                "-b", "5", "-w", "88",
                "-wd", folder.newFolder("wd").getAbsolutePath(),
                "-o", folder.getRoot().getAbsolutePath() + File.separator + "out.txt"};

        Options options = new Options();
        Main.buildRequiredOptions(options);
        Main.buildOptionalOptions(options);

        CommandLineParser parser = new DefaultParser();
        parser.parse(options, args);
    }

    @Test(expected = ParseException.class)
    public void testInputAndOutputMissed() throws IOException, ParseException {
        String[] args = new String[]{
                "-b", "5", "-w", "88",
                "-wd", folder.newFolder("wd").getAbsolutePath()};

        Options options = new Options();
        Main.buildRequiredOptions(options);
        Main.buildOptionalOptions(options);

        CommandLineParser parser = new DefaultParser();
        parser.parse(options, args);
    }

    @Test
    public void testOptionalMissed() throws IOException, ParseException {
        String[] args = new String[]{"-i", folder.getRoot().getAbsolutePath() + File.separator + "temp.txt",
                "-b", "5",
                "-o", folder.getRoot().getAbsolutePath() + File.separator + "out.txt"};

        Options options = new Options();
        Main.buildRequiredOptions(options);
        Main.buildOptionalOptions(options);

        assertTrue(options.hasOption(Main.INPUT));
        assertTrue(options.hasOption(Main.OUTPUT));
        assertTrue(options.hasOption(Main.BATCH));

        CommandLineParser parser = new DefaultParser();
        parser.parse(options, args);
    }

    private File createUnsortedFile() throws IOException {
        File f = folder.newFile("temp.txt");
        BufferedWriter w = new BufferedWriter(new FileWriter(f));
        for (int i = 0; i <= 9; ++i) {
            w.write(String.valueOf(9 - i));
            w.newLine();
        }
        w.close();
        return f;
    }
}
