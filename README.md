# CPU Scheduler Algorithms [FIFO|SJF|PR|RR]

[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg?style=plastic)](https://opensource.org/licenses/Apache-2.0) ![Contributions welcome](https://img.shields.io/badge/contributions-welcome-brightgreen.svg?style=plastic)

A simple CPU Scheduling simulation: A multithreaded program that will allow us to measure the performance
                           (i.e., CPU utilization, Throughput, Turnaround time, Waiting time, and Response time)
                            of the four basic CPU scheduling algorithms (namely, FIFO, SJF, PR, and RR) by simulating the processes
                            whose priority, sequence of CPU burst time(ms) and I/O burst time(ms) will be given in an input file.

### Assumptions:

1.  All scheduling algorithms except RR will be non-preemptive, and all scheduling algorithms except
    PR will ignore process priorities (i.e., all processes have the same priority in FIFO, SJF and RR).
2.  There is only one IO device and all IO requests will be served using that device in a FIFO manner.

### Compile:

  `javac prog.java`

### Run:

  `java prog -alg [FIFO|SJF|PR|RR]-quantum [integer(ms)]] -input [file name]`

### Example:

1.  `java prog -alg FIFO -quantum 0 -input input.txt`
2.  `java prog -alg RR -quantum 300 -input input.txt`
