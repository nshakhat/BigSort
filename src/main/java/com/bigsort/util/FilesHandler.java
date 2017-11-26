package com.bigsort.util;

import java.io.*;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The util class for working with files
 *
 * @author Nadya Shakhat
 */

public class FilesHandler {

    /**
     * Parse all file names from {@code paths} list. It is not
     * allowed to have any kind of recursion.
     *
     * @param paths is a strings representing paths to files
     * @return the list of files which were not deleted successfully
     * @throws java.lang.IllegalAccessException   if there is no read access to any file
     * @throws java.nio.file.NoSuchFileException  if any file doesn't exist
     * @throws java.lang.IllegalArgumentException is any file is a nested directory
     */

    public static List<File> getAllFiles(String[] paths) throws IllegalAccessException,
            NoSuchFileException, IllegalArgumentException {
        ArrayList<File> allFiles = new ArrayList<>();

        for (String s : paths) {
            File file = new File(s);

            if (!file.exists())
                throw new NoSuchFileException("File " + s + " cannot be found");
            if (!file.canRead())
                throw new IllegalAccessException("No read access to " + s + " file");

            if (file.isFile()) {
                allFiles.add(file);
                continue;
            }

            // file is a directory
            File[] children = file.listFiles();
            if (children == null) continue; // Failed to read the directory, skip

            for (File child : children) {
                if (child.isDirectory())
                    throw new IllegalArgumentException("Directory " + s + " contains directories");
                allFiles.add(child);
            }
        }
        return allFiles;
    }

    /**
     * Construct {@code BufferedReader} for each file in the list of {@code files}.
     *
     * @param files is a list of files
     * @return list of BufferedReaders for the given files
     */
    public static BufferedReader[] getBufferedReaders(List<File> files) {
        if (files == null) return null;
        BufferedReader[] readers = new BufferedReader[files.size()];
        for (int i = 0; i < files.size(); ++i) {
            try {
                readers[i] = new BufferedReader(new FileReader(files.get(i)));
            } catch (FileNotFoundException e) {
                System.err.println(e.getMessage());
                return null;
            }
        }
        return readers;
    }

    /**
     * Create a temporary file with a prefix {@code prefix} in the directory
     * {@code dir}.
     *
     * @param prefix is a prefix for a temporary file
     * @param dir    is a directory where a file should be created
     * @return the created file or {@code null} if the file was not created
     */

    public static File createTempFile(String prefix, String dir) {
        try {
            return File.createTempFile(prefix, "", new File(dir));

        } catch (Exception e) {
            System.err.println("Cannot create a temporary file:");
            System.err.println(e.getMessage());
            return null;
        }
    }

    /**
     * Returns a list of files whose names have {@code prefix} prefix.
     *
     * @param searchDir is a directory where the search will be done
     * @param prefix    is a prefix which should match
     * @return the list of the matched files
     */
    public static List<File> getMatchedFiles(String searchDir, final String prefix) {
        File dir = new File(searchDir);
        return Arrays.asList(dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.startsWith(prefix);
            }
        }));
    }

    /**
     * Merge the sorted files.
     * <p>
     * Indeed Min Priority Queue structure is used for this purpose.
     * {@code} queue is operated in the following way. During construction it should
     * be defined how many indexes it should contain. In this case, each file is an index.
     * Thus, one line is read from each file and is inserted it into the queue using
     * {@code queue.insert(i, s)}. This is the initial state of the queue.
     * <p>
     * To retrieve the next item of result, the method {@code queue.minKey()} is called.
     * It doesn't delete the minimum value from the queue. To delete the minimum item,
     * {@code queue.delMin()} should be used. It returns the index of the minimum value,
     * i.e. at this point it is known which fie contained the minimum value. To continue,
     * the next line from this file should be put in the queue with the corresponding index.
     * <p>
     * The process finishes when the queue is empty.
     *
     * @param files      is an array of files to process
     * @param resultFile is a File where the result should be stored
     * @return {@code true} if the merge stage is successful. {@code false} otherwise
     */

    public static Boolean mergeSortedFiles(List<File> files, File resultFile) {
        if (files == null) return false;

        IndexMinPQ<String> queue = new IndexMinPQ<>(files.size());
        BufferedReader[] readers = FilesHandler.getBufferedReaders(files);
        if (readers == null) return false;
        BufferedWriter out = null;
        try {
            // Queue initialization
            for (int i = 0; i < files.size(); ++i) {
                String s;
                if ((s = readers[i].readLine()) != null)
                    queue.insert(i, s);
            }

            out = new BufferedWriter(new FileWriter(resultFile));

            while (!queue.isEmpty()) {
                out.write(queue.minKey());
                out.newLine();

                int i = queue.delMin();
                String s;
                if ((s = readers[i].readLine()) != null) {
                    queue.insert(i, s);
                }
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return false;
        } finally {
            try {
                if (out != null) out.close();
            } catch (IOException io) {
                System.err.println(io.getMessage());
            }
        }

        return true;
    }


    /**
     * Delete files from {@code directory} whose names have {@code prefix} prefix.
     *
     * @param directory is a directory where files are located
     * @param prefix    is the start index of the range, inclusively
     * @return the list of files which were not deleted successfully
     */

    public static ArrayList<File> cleanUp(String directory, String prefix) {
        List<File> toDelete = getMatchedFiles(directory, prefix);
        ArrayList<File> notDeleted = new ArrayList<>();
        for (File f : toDelete) {
            boolean res = f.delete();
            if (!res) {
                notDeleted.add(f);
            }
        }
        return notDeleted;
    }


    /**
     * Write error messages to System.err about not deleted files
     *
     * @param notDeleted a List to fill up with not deleted files
     */

    public static void errorHandler(ArrayList<File> notDeleted) {
        if (notDeleted.size() > 0) {
            System.err.println("The following files were not deleted: ");
            for (File f : notDeleted) {
                System.err.println(f.getAbsolutePath());
            }
        } else {
            System.err.println("All temporary files were successfully deleted.");
        }
    }

    /**
     * Validate that the file exists.
     *
     * @param path to file
     * @throws NoSuchFileException is the corresponding file doesn't exist
     */

    public static void validateExistence(String path) throws NoSuchFileException {
        File f = new File(path);
        if (!f.exists())
            throw new NoSuchFileException(path + " doesn't exist");
    }
}
