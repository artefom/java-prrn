package backend;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

public class TaskScheduler {

    // Logger
    private static Logger log = Logger.getLogger(TaskScheduler.class.getName());


    // Entity that provides tasks for TaskScheduler
    TaskProvider tprov;

    // Factory for our processes
    ProcessFactoryBase pfact;

    private ArrayList<RRNProcessBase> processes;
    private static final int MAX_N_PROCESSES = 1000;

    public TaskScheduler() {
        processes = new ArrayList<>();
    }

    /**
     * Set number of threads for calculation
     * @param n_processes
     */
    public void set_process_count(int n_processes) {
        if (n_processes < 0) {
            throw new IndexOutOfBoundsException("Number of processes cannot be below 0!");
        } else if (n_processes > MAX_N_PROCESSES) {
            throw new IndexOutOfBoundsException("Number of processes cannot be below 0!");
        }
        while (processes.size() > n_processes) {
            processes.remove(processes.size()-1);
        }
        while (processes.size() < n_processes) {
            processes.add( pfact.create_process() );
        }
    }

    /**
     * Executes processes and tasks
     */
    public void execute() {

        Collection<TaskBase> tasks = tprov.get_tasks();

        log.info(String.format("Master process executed for %d tasks with %d processes",tasks.size(),processes.size() ) );

        for (TaskBase tb : tasks) {
            tb.read_batch(10);
        }
    }

    public void set_task_provider(TaskProvider tprov) {
        this.tprov = tprov;
    }

    public void set_process_factory(ProcessFactoryBase i_pfact) {
        pfact = i_pfact;
    }

}
