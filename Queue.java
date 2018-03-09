import java.util.LinkedList;
import java.util.Collections;

public class Queue {

private LinkedList<Process> q;
private String algo;

Queue(String algo){
        q = new LinkedList<>();
        this.algo = algo;

}

private void sort(){
        // stable: equal elements will not be reordered as a result of the sort.
        Collections.sort(this.q);
}


public String getAlgo() {
        return algo;
}

// Input thread enqueues
public synchronized void enqueue(Process p){

        this.q.add(p);
        notifyAll();
}

// dequeue in SJF|PR based on priority, sorting linked list at the time of deque
public synchronized Process dequeue() throws InterruptedException {

        while(q.size() == 0) {
                wait();
        } // wait till empty

        if ((algo.equalsIgnoreCase("SJF") || algo.equalsIgnoreCase("PR"))) {

                if (algo.equalsIgnoreCase("SJF")) {
                        for (Process p: q) {
                                p.updatePriority();
                        }
                }

                sort(); // max priority first
        }
        Process p = q.removeFirst();
        notifyAll();
        return p;
}

}
