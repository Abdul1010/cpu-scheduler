import java.lang.InterruptedException;


public class CPUScheduler implements Runnable {

private final Queue ready_q;
private final Queue io_q;
private double quantum;
public static double total_busy_time = 0.0;

CPUScheduler(Queue ready_q, Queue io_q, double quantum){
        this.ready_q = ready_q;
        this.io_q = io_q;
        this.quantum = quantum;
}
// scheduler
@Override
public void run(){
        try{
                Process p;

                while (!Thread.currentThread().isInterrupted()) {

                        // get the job next in queue
                        p = ready_q.dequeue(); //notifies
                        // log the time it was assign to processor
                        double assign_time = (double)System.currentTimeMillis();
                        p.assign_times.add(assign_time);
                        if (p.cpu_burst.size() == 1)
                                p.end_time = assign_time + p.cpu_burst.get(0);
                        // assign to cpu
                        execute(p);
                        // add process to log at position w.r.t id

                }
        }
        catch (InterruptedException e) {Thread.currentThread().interrupt();}

}

private void execute(Process p){
        double busy_start = (double)System.currentTimeMillis();;
        String scheduler_algo = ready_q.getAlgo();

        double execution_time;

        // assign process, update assign_time of process, after finish put in rq or iq depending on the algorithm
        double cpu_burst_time = p.cpu_burst.remove(0);

        // check if it will need io after cpu_burst
        boolean needs_io = p.needIo();

        try {
                if (scheduler_algo.equalsIgnoreCase("RR")) {
                        execution_time = quantum;

                        if (cpu_burst_time > quantum) {
                                cpu_burst_time -= quantum;

                                // EXECUTE <--------------------
                                Thread.sleep((long) execution_time);

                                // Cpu_Burst time left, enqueue to ready queue <--------------------
                                p.cpu_burst.add(0, cpu_burst_time);

                                p.arrival_times.add((double)System.currentTimeMillis());
                                total_busy_time += (double)System.currentTimeMillis() - busy_start;
                                ready_q.enqueue(p); //notifies

                        } else {
                                execution_time = cpu_burst_time;

                                // EXECUTE <--------------------
                                Thread.sleep((long) execution_time);
                                total_busy_time += (double)System.currentTimeMillis() - busy_start;

                                if (needs_io) {
                                        io_q.enqueue(p); //notifies
                                }

                        }

                } else {
                        execution_time = cpu_burst_time;

                        // EXECUTE <--------------------
                        Thread.sleep((long) execution_time);
                        total_busy_time += (double)System.currentTimeMillis() - busy_start;

                        if (needs_io) {
                                io_q.enqueue(p); //notifies
                        }
                }
        }
        catch(InterruptedException e) {Thread.currentThread().interrupt();}
}
}
