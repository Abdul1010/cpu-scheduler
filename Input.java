import java.util.List;
import java.util.Scanner;
import java.util.regex.PatternSyntaxException;
import java.lang.InterruptedException;

public class Input implements Runnable {

private final Queue ready_q;
private Scanner scanner_obj;
private List<Process> log;

Input(Scanner scanner_obj, Queue ready_q, List<Process> log){
        this.ready_q = ready_q;
        this.scanner_obj = scanner_obj;
        this.log = log;
}

@Override
public void run() {
        String line;
        String [] splits;
        String command;

        int id = 0;

        while(scanner_obj.hasNextLine()) {

                line = scanner_obj.nextLine();

                try{
                        line = line.trim();
                        splits = line.split("\\s"); // will have the next step to do
                        command = splits[0];

                        if (command.equalsIgnoreCase("proc")) {

                                // Collect values for CPU Burst and IO Burst from input values
                                double [] values = new double[splits.length - 1];
                                for (int i=0; i<splits.length - 1; i++) {
                                        values[i] = Integer.parseInt(splits[i+1]);
                                        if (values[i] <= 0) {
                                                throw new RuntimeException("Burst time values cannot be <= zero/0, check input file.");
                                        }
                                }
                                // New PCB object
                                Process p = new Process(id, values);
                                // increment id
                                id += 1;

                                // ENQUEUE the Object to ready queue <--------------------------------------------------------
                                p.arrival_times.add((double)System.currentTimeMillis());

                                log.add(p.id, p);
                                this.ready_q.enqueue(p); // also notifies

                                System.out.println("Input thread added Process: " + p.id +" to the ready queue");
                        }

                        else if (command.equalsIgnoreCase("sleep")) {

                                long sleep_time = Integer.parseInt(splits[1]);
                                System.out.println("Input thread will sleep for : " + sleep_time +"ms");

                                try{Thread.sleep(sleep_time);}
                                catch(InterruptedException e) {Thread.currentThread().interrupt();}

                        }

                        else if (command.equalsIgnoreCase("stop")) {
                                return;
                        }

                        else {throw new RuntimeException("Invalid keyword: " + command + " in the input file. Allowed keywords are: proc | sleep | stop ");}
                }

                catch(PatternSyntaxException e) {
                        System.out.println("Input file is not in desired format, make sure the format is same as the example below:");
                        System.out.println("proc 1 10 20 10 50 20 40 10\nproc 1 50 10 30 20 40\nsleep 50\nproc 2 20 50 20\nstop");
                }
        }
        System.out.println("Input thread Ended..");
}
}
