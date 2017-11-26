------------------
Build the project
------------------
The project can be build using Maven. Please run:

mvn clean compile assembly:single

-----------------
Run the project
-----------------

java -jar /path/to/jar/bigsort-0.1-jar-with-dependencies.jar <options>

BigSot has the following parameters:

 -b,--batch <arg>             Maximum number of lines in RAM. Min value is
                              2
 -i,--input <arg>             Input file(s) or directory{s}. Nested dirs
                              are not allowed
 -mf,--maxOpenedFiles <arg>   How many files can be opened at once for
                              reading. Min value is 2
 -o,--output <arg>            A path for the result file
 -w,--workers <arg>           Maximum number of workers to be running. Min
                              value is 1
 -wd,--workingDir <arg>       Directory for temporary files

 --batch, --input and --output are required. You can specify as many input files as
 you want. Each file may be a directory or a file. The only requirement is that a directory
 cannot be nested.
 --batch specifies how many lines can be in RAM at once.

The execution is done in two stages: sort and merge.
During the sort stage, workers read lines from files, sort them and write to disk. You can specify
how many workers should be running. Note though that the more workers are running, the less lines
can be read from each file (batch/workers, to be more precise). By default, 1 worker is used.
Merge stage is always single-threaded. During this stage, batch files are opened and are being read
simultaneously. However, the tool will not open more than --maxOpenedFiles. If this value is not
specified, the default value of 10000 is used.
Using --workingDir you can specify the directory where all temporary files will be stored. By default,
a system temporary folder is used.



