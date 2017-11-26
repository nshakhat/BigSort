package com.bigsort;

import com.bigsort.util.FilesHandler;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

/**
 * FilesSorter sorts the content of {@code files} having only {@code batch} items in RAM as a maximum.
 * For this purpose two steps needed:
 * 1. Sort stage. FilesSorter creates a queue of files to process and runs {@code workers} of consumers
 * to do the sort of Strings from each file. The consumers sort the files in parallel. Each consumer is
 * allowed to have only {@code batch / workers} lines in RAM. It means that every input file may produce
 * several sorted files as an output.
 * 2. Merge stage. FilesSorter merges at most {@code batch} files at once. This process is not multi-threaded because
 * of the high IO-load.
 * <p>
 * During the process, temporary files are created in the directory {@code destinationDir}. Each FilesSorter instance
 * has its own prefix for temporary files. All the temporary files are removed from the {@code destinationDir} after
 * FilesSorter finishes its work.
 *
 * @author Nadya Shakhat
 */

public class FilesSorter {

    public static final String PROJECT_PREFIX = "big.sort.";
    public static final int MAX_FILES = 10000;
    private int workers;
    private int maxItemsPerWorker;
    private ConcurrentLinkedQueue<File> unsortedFilesQueue;
    private int batch;
    private String jobID;
    private String destinationDir;
    private String resultFile;
    private int max_opened_files;

    /**
     * Initializes a FilesSorter which sorts all {@code files} having {@code batch}
     * items as a maximum in RAM. Each FilesSorted has a unique {@code jobID} which
     * is used as a prefix for all temporary files created by each FilesSorter.
     *
     * @param files      an array of files to sort
     * @param workers    the maximum amount of workers which work in parallel during the sort stage
     * @param batch      the maximum lines in RAM
     * @param dst        the working directory where the sorted resulting files are stored
     * @param resultFile the path to the result file
     * @param maxFiles   maximum amount of opened files
     */

    public FilesSorter(List<File> files, int workers, int batch, String dst, String resultFile, int maxFiles) {
        this.workers = calculateOptimalAmountOfWorkers(workers, batch, files.size());
        this.jobID = constructThePrefix(PROJECT_PREFIX);
        this.destinationDir = dst;
        this.resultFile = resultFile;
        this.max_opened_files = maxFiles;
        this.batch = batch;
        maxItemsPerWorker = batch / this.workers;
        unsortedFilesQueue = new ConcurrentLinkedQueue<>();
        unsortedFilesQueue.addAll(files);
    }

    public FilesSorter(List<File> files, int workers, int batch, String dst, String resultFile) {
        this(files, workers, batch, dst, resultFile, MAX_FILES);
    }

    /**
     * Despite the fact that the constructor receives {@code workers}, this value may be
     * not optimally defined. For example, if there is only one file to be processed, there is
     * no need to start more than one worker. Also, it is required to have only batch items
     * in RAM in total, thus it is not allowed to run more than {@code batch} workers. If no files
     * are to be processed, one worker will be initialized.
     *
     * @return optimal amount of workers
     */

    private int calculateOptimalAmountOfWorkers(int workersAmount, int batch, int length) {
        if (length == 0) return 1;
        return Math.min(Math.min(workersAmount, batch), length);
    }

    /**
     * Do the sort stage. {@code workers} workers sort files simultaneously.
     * If a sort stage finishes successfully, the directory {@code destinationDir}
     * contains sorted files.
     *
     * @return {@code true} if the sort stage is successful. {@code false} otherwise
     */
    private boolean doTheSortJob() {
        Boolean sortResult = true;
        ExecutorService executorService = Executors.newFixedThreadPool(workers);
        List<Future<Boolean>> res = new ArrayList<>();

        for (int i = 0; i < workers; ++i) {
            res.add(executorService.submit(
                    new MapWorker(unsortedFilesQueue, maxItemsPerWorker, destinationDir, jobID)));
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
            for (Future<Boolean> futureRes : res) {
                while (!futureRes.isDone()) {
                    //Wait for completion
                }
                sortResult &= futureRes.get();
            }
        } catch (InterruptedException ex) {
            System.err.println("Interrupted sort stage execution.");
            return false;
        } catch (ExecutionException e) {
            System.err.println("Interrupted sort stage execution.");
            return false;
        }

        return sortResult;
    }

    /**
     * Do the merge stage. This stage is single threaded. Because of the fact that a lot of files
     * are being read simultaneously, there is a configurable limit of opened files.
     * <p>
     * IndexedPriorityQueue is used to merge files.{@code Math.min(max_opened_files, batch)} are merged
     * at once and there is at most one line from each file in the IndexedPriorityQueue. If it is not
     * possible to merge all files because of small buffer, several stages are required. The process is
     * finished when there is only one file left. This file is the result and is copied to the
     * {@code resultFile}.
     *
     * @return {@code true} if the merge stage is successful. {@code false} otherwise
     */
    private boolean doTheMergeJob() {
        List<File> sortedFiles = FilesHandler.getMatchedFiles(destinationDir, jobID);
        if(sortedFiles.size() == 0) {
            System.err.println("No files has been found after the sort stage." +
                    " Please check that your input is not empty.");
            return true;
        }
        // We may want not to open too many files at once
        int maxItemsInMemory = Math.min(max_opened_files, batch);

        while (sortedFiles.size() > 1) {
            ArrayList<File> mergeResult = new ArrayList<>();

            for (int i = 0; i < sortedFiles.size(); i += maxItemsInMemory) {
                File resultTempFile = FilesHandler.createTempFile(jobID, destinationDir);
                if (resultTempFile == null) return false;

                if (!FilesHandler.mergeSortedFiles(
                        sortedFiles.subList(i, Math.min(i + maxItemsInMemory, sortedFiles.size())), resultTempFile))
                    return false;
                mergeResult.add(resultTempFile);
            }
            sortedFiles = mergeResult;
        }

        return sortedFiles.get(0).renameTo(new File(resultFile));
    }

    /**
     * Do the sort. Clean up of {@code destinationDir} is required in any case.
     *
     * @return {@code true} if the sort is successful. {@code false} otherwise
     */

    public boolean sort() {
        boolean result = doTheSortJob();

        if (!result) {
            System.err.println("The sort stage has failed.");
            FilesHandler.errorHandler(FilesHandler.cleanUp(destinationDir, jobID));
            return false;
        }
        System.out.println("The sort stage is finished");
        result = doTheMergeJob();
        if (!result) {
            System.err.println("The merge stage has failed.");
            FilesHandler.errorHandler(FilesHandler.cleanUp(destinationDir, jobID));
            return false;
        }
        System.out.println("The merge stage is finished");
        FilesHandler.cleanUp(destinationDir, jobID);
        return true;
    }

    /**
     * A helper method for unique file prefix construction.
     *
     * @param projectId is an id specific for a project
     * @return a unique String which has a prefix equals to {@code projectId} followed by a
     * timestamp (hours, minutes, seconds and milliseconds are taken into account)
     */

    private String constructThePrefix(String projectId) {
        return new SimpleDateFormat("'" + projectId + "'" + "hhmmssSSS").format(new Date());
    }
}
