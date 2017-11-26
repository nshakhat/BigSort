package com.bigsort;

import com.bigsort.util.FilesHandler;
import org.apache.commons.cli.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class Main {

    public static final Integer MIN_WORKERS = 1;
    public static final Integer MIN_OPENED_FILES = 2;
    public static final int MIN_BATCH = 2;
    public static final String PROJECT_NAME = "BigSort";
    // Required options names
    public static final String BATCH = "batch";
    public static final String INPUT = "input";
    public static final String OUTPUT = "output";
    // Optional options names
    public static final String WORKERS = "workers";
    public static final String WORKING_DIR = "workingDir";
    public static final String MAX_OPENED_FILES = "maxOpenedFiles";


    public static void main(String[] args) {
        Options options = new Options();

        buildRequiredOptions(options);
        buildOptionalOptions(options);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        int batch;
        int workers;
        int maxOpenedFiles;

        try {
            cmd = parser.parse(options, args);
            batch = parseAndValidateIntOption(BATCH, cmd.getOptionValue(BATCH), MIN_BATCH);
            workers = parseAndValidateIntOption(WORKERS,
                    cmd.getOptionValue(WORKERS, MIN_WORKERS.toString()), MIN_WORKERS);
            maxOpenedFiles = parseAndValidateIntOption(MAX_OPENED_FILES,
                    cmd.getOptionValue(MAX_OPENED_FILES, MIN_OPENED_FILES.toString()), MIN_OPENED_FILES);


        } catch (ParseException | IllegalArgumentException e) {
            System.out.println(e.getMessage());
            formatter.printHelp(PROJECT_NAME, options);
            System.exit(1);
            return;
        }

        try {
            List<File> files = FilesHandler.getAllFiles(cmd.getOptionValues(INPUT));

            String output = cmd.getOptionValue(OUTPUT);
            String workingDir = cmd.getOptionValue(WORKING_DIR, System.getProperty("java.io.tmpdir"));
            FilesHandler.validateExistence(workingDir);

            FilesSorter sorter = new FilesSorter(files, workers, batch, workingDir, output, maxOpenedFiles);
            sorter.sort();

            System.out.println("Work has been finished. Please find the result in " + cmd.getOptionValue(OUTPUT));

        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    public static int parseAndValidateIntOption(String optionName, String value, int min) throws IllegalArgumentException {
        int t;
        try {
            t = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(optionName + " has a wrong value " + value);
        }
        if (t < min)
            throw new IllegalArgumentException(optionName + " should be in range [" +
                    min + ", " + Integer.MAX_VALUE + "]");
        return t;
    }

    public static void buildRequiredOptions(Options options) {
        Option input = new Option("i", INPUT, true, "Input file(s) or directory{s}. Nested dirs are not allowed");
        input.setArgs(Option.UNLIMITED_VALUES);
        input.setRequired(true);
        options.addOption(input);

        Option batchOpt = new Option("b", BATCH, true, "Maximum number of lines in RAM. Min value is " + MIN_BATCH);
        batchOpt.setRequired(true);
        options.addOption(batchOpt);

        Option resultFile = new Option("o", OUTPUT, true, "A path for the result file");
        resultFile.setRequired(true);
        options.addOption(resultFile);
    }

    public static void buildOptionalOptions(Options options) {
        Option workersOpt = new Option("w", WORKERS, true, "Maximum number of workers to be running. " +
                "Min value is " + MIN_WORKERS);
        workersOpt.setRequired(false);
        options.addOption(workersOpt);

        Option workingDirOpt = new Option("wd", WORKING_DIR, true, "Directory for temporary files");
        workingDirOpt.setRequired(false);
        options.addOption(workingDirOpt);

        Option maxOpenedFilesOpt = new Option("mf", MAX_OPENED_FILES, true, "How many files can be " +
                "opened at once for reading. Min value is " + MIN_OPENED_FILES);
        maxOpenedFilesOpt.setRequired(false);
        options.addOption(maxOpenedFilesOpt);
    }
}
