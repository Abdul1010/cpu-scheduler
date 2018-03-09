import java.util.List;
import java.util.ArrayList;
import java.lang.Math;

// PCB: Holding meta-info about process, this info will be used to calculate: pu,tat,wt,rt
public class Process implements Comparable<Process> {

public static int process_count = 0;
public int id;
public double end_time;
private double priority;
public List<Double> cpu_burst;
public List<Double> io_burst;

// CPU
public List<Double> arrival_times;     // tat = exit_time(last cpu_burst done) - arrival_time
public List<Double> assign_times;


Process(int id, double[] values){

        this.id = id;
        this.priority = values[0];
        if (this.priority < 1 || this.priority > 10) throw new RuntimeException("Error for process number: " + this.id + " =>> Priority Values should be in range [1, 10]");

        // Initialize CPU and IO Burst times for this process
        cpu_burst = new ArrayList<>();
        io_burst = new ArrayList<>();
        arrival_times = new ArrayList<>();
        assign_times = new ArrayList<>();

        for(int i=1; i<values.length; i++) {
                if (i%2 != 0) cpu_burst.add(values[i]);
                else io_burst.add(values[i]);
        }

        process_count++;
}
@Override
public int compareTo(Process p) {
        // for reverse order: max priority first
        return (int)Math.signum(p.priority - this.priority);
}

public boolean needCpu(){
        return cpu_burst.size() > io_burst.size();
}

public boolean needIo(){
        return (cpu_burst.size() == io_burst.size()) && (io_burst.size() != 0);
}

public void updatePriority(){
        // updates the priority as 100/NextCpuBurst
        if (!cpu_burst.isEmpty()) {
                priority = 100 / cpu_burst.get(0);
        }
}

public int notDone(){
        // returns 1 if not done else 0
        boolean notDone = needCpu() || needIo();

        if (notDone) {
                return 1;
        }
        else{
                return 0;
        }

}


}
