/*
 * CPU Scheduling simulation: A multithreaded program that will allow us to measure the performance
                            (i.e., CPU utilization, Throughput, Turnaround time, Waiting time, and Response time)
                             of the four basic CPU scheduling algorithms (namely, FIFO, SJF, PR, and RR) by simulating the processes
                             whose priority, sequence of CPU burst time(ms) and I/O burst time(ms) will be given in an input file.

 * Assumptions:  1. All scheduling algorithms except RR will be non-preemptive, and all scheduling algorithms except
                PR will ignore process priorities (i.e., all processes have the same priority in FIFO, SJF and RR).
                2. There is only one IO device and all IO requests will be served using that device in a FIFO manner.

 * Execution:  java prog -alg [FIFO|SJF|PR|RR] [-quantum [integer(ms)]] -input [file name]

 * Sample Input file: proc 1 10 20 10 50 20 40 10
                     proc 1 50 10 30 20 40
                     sleep 50
                     proc 2 20 50 20
                     stop

 * Output format in output.txt:

   Input File Name     : file name
   CPU Scheduling Alg  : FIFO|SJF|PR|RR (quantum)
   CPU utilization     : ....
   Throughput          : ....
   Turnaround time     : ....
   Waiting time        : ....
   Response time       : ....

 */

import java.io.*;
import java.util.Arrays;
import java.util.Collections;

import java.util.List;
import java.util.ArrayList;

import java.util.Map;
import java.util.HashMap;

import java.util.Scanner;
import java.lang.InterruptedException;

public class prog {

public static void main(String[] args) throws InterruptedException, IOException {

        // Get args
        Map<String, String> arguments = new HashMap<>();

        // To log all the processes in current simulation: used to calculate performance metrics
        List<Process> log = new ArrayList<>();

        // Collect args
        String type = "";
        for (String s:args) {
                if (s.charAt(0) == '-')
                        type = s;
                else{
                        if (type.equals(""))
                                throw new RuntimeException("Invalid Arguments, Format: prog -alg [FIFO|SJF|PR|RR] [-quantum [integer(ms)]] -input [file name] ");
                        arguments.put(type, s);
                        type = "";
                }
        }

        // Input File to Scanner obj.
        File file_obj;
        Scanner scanner_obj;

        try{
                file_obj = new File(arguments.get("-input"));
                scanner_obj = new Scanner(file_obj);

                // Check args
                String alg = arguments.get("-alg");

                if (!(alg.equals("RR") || alg.equals("PR") || alg.equals("SJF") || alg.equals("FIFO"))) {
                        throw new RuntimeException("Algorithm(-alg) argument invalid, Case Sensitive Format: prog -alg [FIFO|SJF|PR|RR] [-quantum [integer(ms)]] -input [file name]");
                }

                // Check for RR
                double quantum = 0;

                if (alg.equalsIgnoreCase("RR")) {
                        String qvalue = arguments.getOrDefault("-quantum", "");
                        if (qvalue.equals(""))
                                throw new RuntimeException("Quantum is required for RR algorithm, Format: prog -alg [FIFO|SJF|PR|RR] [-quantum [integer(ms)]] -input [file name] ");

                        // assign quantum
                        quantum = Double.parseDouble(qvalue);
                }

                // Create ready_q and io_q: shared resources
                Queue ready_q = new Queue(alg);
                Queue io_q = new Queue("FIFO");

                // Create Input, CPUScheduler, IOSystem thread
                Thread input_thread = new Thread(new Input(scanner_obj, ready_q, log));
                Thread cpu_thread = new Thread(new CPUScheduler(ready_q, io_q, quantum));
                Thread io_thread = new Thread(new IOSystem(ready_q, io_q));

                // SIMULATE CPU SCHEDULING
                input_thread.start();
                double cpu_start_time = (double)System.currentTimeMillis();

                cpu_thread.start();

                io_thread.start();

                // wait till input is over
                input_thread.join();

                // wait till both the queues are empty
                int wait = 1;
                while (wait != 0) {
                        wait = 0; // give a chance to finish
                        for (Process p: log) {
                                wait += p.notDone(); // will return 1 if not done, chance lost
                        }
                }
                double cpu_life = (double)System.currentTimeMillis() - cpu_start_time;


                // End the cpu, io operations
                cpu_thread.interrupt();
                io_thread.interrupt();

                double total_busy_time = CPUScheduler.total_busy_time;

                // output performance metrics
                performanceMetrics(log, arguments, cpu_life, total_busy_time);

                System.out.println("Total Process executed: " + Process.process_count);
                System.out.println(Collections.singletonList(arguments));

        }

        catch (FileNotFoundException e) {System.out.println(arguments.get("-input") + " was not found!!");}

}

private static void performanceMetrics(List<Process> log, Map arguments, double cpu_life, double total_busy_time) throws IOException {

        // Get the absolute start time: used as reference or a 0 on gantt
        double start_time = log.get(0).arrival_times.get(0);

        // To collect performance metrics
        double[] arrival_times, assign_times, wt;
        double each_wt;
        double process_end_time;
        double total_wt = 0;
        double total_rt = 0;
        double total_tat = 0;
        double max_end_time = 0;

        // Collect performance metrics
        for(Process p: log) {

                System.out.println("Process: " + p.id);

                // end time of this process
                process_end_time = p.end_time - start_time;

                // to collect all arrival(at ready queue) times at ready queue of this process
                arrival_times = new double[p.arrival_times.size()];
                // to collect all assign (got cpu) times of this process
                assign_times = new double[p.assign_times.size()];
                // to collect all wait times of tis process
                wt = new double[p.assign_times.size()];

                // last process end time
                max_end_time = (process_end_time> max_end_time) ? process_end_time : max_end_time;

                // calc relative arrival times for each process
                for(int i=0; i<p.arrival_times.size(); i++) {

                        arrival_times[i] = p.arrival_times.get(i) - start_time;

                        if(i==0) { // sum tats
                                total_tat += process_end_time - arrival_times[i]; // end_time - first arrival
                        }
                }
                System.out.println("Arrival times: " + Arrays.toString(arrival_times));

                // calc relative assign times for each process
                for(int i=0; i<p.assign_times.size(); i++) {
                        assign_times[i] = p.assign_times.get(i) - start_time;
                        wt[i] = assign_times[i] - arrival_times[i]; // calc all WTs for this process
                }
                System.out.println("Assign times: "  + Arrays.toString(assign_times));

                each_wt = 0;
                // sum of all wts for each process
                for(int i=0; i<wt.length; i++) {
                        each_wt += wt[i];

                        if (i==0) { // first assign - first arrival
                                total_rt += wt[i];
                        }
                }

                total_wt += each_wt;
                System.out.println("WT: "  + each_wt);
                System.out.println();
        }

        int size = log.size();

        System.out.println("-----------------------------------------");
        System.out.println("Input File Name    : " + arguments.get("-input"));
        if (arguments.get("-alg").equals("RR"))
                System.out.println("CPU Scheduling Alg : " + arguments.get("-alg") +"("+arguments.get("-quantum")+")");
        else
                System.out.println("CPU Scheduling Alg : " + arguments.get("-alg"));
        System.out.println("CPU utilization    : " + total_busy_time * 100 /cpu_life +"%");
        System.out.println("Throughput         : " + size/max_end_time);
        System.out.println("Turnaround Time    : " + total_tat/size);
        System.out.println("Waiting Time       : " + total_wt/size);
        System.out.println("Response Time      : " + total_rt/size);
        System.out.println("-----------------------------------------");
        System.out.println();


        PrintWriter out = new PrintWriter(new FileWriter("output.txt"));
        out.println("Input File Name    : " + arguments.get("-input"));

        if (arguments.get("-alg").equals("RR"))
                out.println("CPU Scheduling Alg : " + arguments.get("-alg") +"("+arguments.get("-quantum")+")");
        else
                out.println("CPU Scheduling Alg : " + arguments.get("-alg"));

        out.println("CPU utilization    : " + total_busy_time * 100 /cpu_life +"%");
        out.println("Throughput         : " + size/max_end_time);
        out.println("Turnaround Time    : " + total_tat/size);
        out.println("Waiting Time       : " + total_wt/size);
        out.println("Response Time      : " + total_rt/size);
        out.close();

}
}
