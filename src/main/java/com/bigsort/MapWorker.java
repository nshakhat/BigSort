package com.bigsort;

import com.bigsort.util.FilesHandler;

import java.io.*;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Each instance of {@code MapWorker} class consumes files from a shared thread-safe
 * LinkedQueue. This class implements Callable interface, i.e. MapWorker instances
 * can be run simultaneously.
 * <p>
 * Each MapWorker removes a file from the queue and processes this file using batches.
 * The algorithm of reading a file is the following:
 * <p>
 * 1. MapWorker reads only maxItems Strings from a file
 * <p>
 * 2. This batch of Strings becomes sorted and the result is written to the directory {@code dst}
 * into the temporary file with the prefix JobID
 * <p>
 * 3. If the file is not empty, start with the step 1 again. If the file is empty, remove the
 * next file from the queue
 * <p>
 * The process stops if the shared queue is empty.
 *
 * @author Nadya Shakhat
 */

public class MapWorker implements Callable<Boolean> {

    private ConcurrentLinkedQueue<File> q;
    private int maxItems;
    private String dst;
    private String jobID;

    /**
     * Initializes a MapWorker which uses {@code q} as a source of files to sort.
     *
     * @param q        the source of files to process
     * @param maxItems is the maximum amount of lines which can be retrieved from a file
     * @param dst      the working directory where the sorted resulting files are stored
     * @param jobID    the prefix of the resulting files
     */
    public MapWorker(ConcurrentLinkedQueue<File> q, int maxItems, String dst, String jobID) {
        if (maxItems < 1) throw new IllegalArgumentException("maxItems should be positive.");
        this.maxItems = maxItems;
        this.q = q;
        this.dst = dst;
        this.jobID = jobID;
    }

    /**
     * Entry point for each worker. Each {@code MapWorker} retrieves next File to process from the
     * queue until {@code NoSuchElementException} is caught. Each file is read by chunks. After a chunk
     * is read and sorted, the result is written to a temporary file.
     *
     * @return {@code true} if all files are successfully sorted and written to disk.
     * {@code false} otherwise
     */
    @Override
    public Boolean call() {
        try {
            while (true) {
                File file = q.remove();
                BufferedReader f = new BufferedReader(new FileReader(file));
                String[] toSort = new String[maxItems];
                int actualCount = 0;
                String nextLine;

                try {
                    while ((nextLine = f.readLine()) != null) {
                        toSort[actualCount++] = nextLine;

                        if (actualCount == maxItems) {
                            sort(toSort, actualCount);
                            if (!outToTempFile(toSort, actualCount))
                                return false;

                            actualCount = 0;
                        }
                    }
                    sort(toSort, actualCount);
                    // if actualCount <=0 there will be no attempt to create a new file because of "&&" operator
                    if (actualCount > 0 && !outToTempFile(toSort, actualCount))
                        return false;
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                    return false;
                }
            }
        } catch (NoSuchElementException e) {
            // Queue is empty, work has finished
            return true;
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }


    private void sort(String[] lines, int count) {
        Arrays.sort(lines, 0, count);
    }

    /**
     * This method writes {@code actualCount} lines from {@code lines} array to
     * a temporary file with the prefix {@code jobID} in the directory {@code dst}.
     *
     * @param lines       is array of Strings which is a source of lines to be written to
     *                    the temporary file
     * @param actualCount how many lines from {@code lines} array should be written
     *                    to the file. Count starts at 0, i.e. {@code lines[actualCount]}
     *                    is not included
     * @return {@code true} if {@code actualCount} of lines is written to the temporary file.
     * {@code false} otherwise
     */

    private boolean outToTempFile(String[] lines, int actualCount) {
        File tempOut = FilesHandler.createTempFile(jobID, dst);
        if (tempOut == null) return false;
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new FileWriter(tempOut));
            for (int i = 0; i < actualCount; ++i) {
                out.write(lines[i]);
                out.newLine();
            }
        } catch (IOException e) {
            System.err.println("Cannot write to a temp file: " + e.getMessage());
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
}
