import java.lang.InterruptedException;

public class IOSystem implements Runnable {

private final Queue ready_q;
private final Queue io_q;

IOSystem(Queue ready_q, Queue io_q){

        this.ready_q = ready_q;
        this.io_q = io_q;
}
@Override
public void run() {
        Process p;
        double io_burst_time;
        try{

                while(!Thread.currentThread().isInterrupted()) {

                        p = io_q.dequeue();
                        io_burst_time = p.io_burst.remove(0);

                        // EXECUTE <--------------------
                        Thread.sleep((long)io_burst_time);

                        if(p.needCpu()) {
                                p.arrival_times.add((double)System.currentTimeMillis());
                                ready_q.enqueue(p);
                        }
                }
        }
        catch(InterruptedException e) { Thread.currentThread().interrupt();}

}
}
