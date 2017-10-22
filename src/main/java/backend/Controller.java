package backend;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

import backend.graph.*;
import backend.pmanagement.TaskScheduler;
import backend.processes.IRRNProcess;
import backend.processes.ProcessFactory;
import backend.rasterio.RasterDataset;
import backend.tasks.*;

public class Controller {

    private GraphBuilder graph;
    private Collection<IRRNTask> tasks;
    private Collection<IRRNProcess> processes;
    private ITaskProvider task_provider;
    private int process_count;
    private TaskScheduler task_scheduler;

    Logger log = Logger.getLogger(Controller.class.getName());

     /**
     * Initializes programm structure.
     */
    public Controller() {
        process_count = 4;
    }

    // ##################################### GRAPH #####################################

    public void add_file(String path) throws IOException {
        get_graph().add_file(RasterDataset.from_file(path));

        // invalidate (reset) or validate tasks
        invalidate_tasks();
    }

    private void invalidate_graph() {
        graph = null;
        invalidate_tasks();
    }

    /**
     * Initialize graph builder here
     */
    private void validate_graph() {
        graph = new GraphBuilder();
    }

    private boolean graph_valid() {
        return graph != null;
    }

    private GraphBuilder get_graph() {
        if (!graph_valid()) validate_graph();
        return graph;
    }

    public void save_graph(String filename) {
        get_graph().save_graph_dot(filename);
    }


    // ###################################### TASKS ######################################

    private boolean tasks_valid() {
        return tasks != null;
    }

    private void invalidate_tasks() {
        tasks = null;

        // task provider depends on tasks, so invalidate it
        invalidate_task_provider();
    }

    /**
     * Initialize tasks here
     */
    private void validate_tasks() {
        int task_num = get_graph().get_results_size();
        RasterDataset[] source = new RasterDataset[task_num];
        RasterDataset[] target = new RasterDataset[task_num];
        double[] weights = new double[task_num];
        get_graph().get_results(source,target,weights);
        tasks = new ArrayList<IRRNTask>();

        for (int i = 0; i != task_num; ++i) {
            IRRNTask t = TaskFactory.get_task();
            t.set_source(source[i]);
            t.set_target(target[i]);
            tasks.add(t);
        }
    }

    private Collection<IRRNTask> get_tasks() {
        if (!tasks_valid()) validate_tasks();
        return tasks;
    }

    // ################################## Task Provider ###################################

    private void invalidate_task_provider() {
        task_provider = null;
        // processes depend on task provider, so invalidate them
        invalidate_processes();
    }

    /**
     * Initialize task provider here
     */
    private void validate_task_provider() {
        task_provider = new TaskProvider();
        task_provider.set_tasks(get_tasks());
    }

    private boolean task_provider_valid() {
        return task_provider != null;
    }

    private ITaskProvider get_task_provider() {
        if (!task_provider_valid()) validate_task_provider();
        return task_provider;
    }

    // ##################################### PROCESSES #####################################

    private boolean processes_valid() {
        return processes != null;
    }

    private void invalidate_processes() {
        processes = null;
        // task scheduler depends on processes, so invalidate it
        invalidate_task_scheduler();
    }

    /**
     * Write code for creating processes in here
     */
    private void validate_processes() {
        processes = new ArrayList<>();

        for (int i = 0; i != get_process_count(); ++i) {
            IRRNProcess new_proc = ProcessFactory.get_process();
            new_proc.set_task_provider(get_task_provider());
            processes.add(new_proc);
        }
    }

    private Collection<IRRNProcess> get_processes() {
        if (!processes_valid()) validate_processes();
        return processes;
    }

    public void set_process_count(int n) {
        invalidate_processes();
        process_count = n;
    }

    public int get_process_count() {
        return process_count;
    }

    // ################################ PROCESSES MANAGEMENT ###############################

    private void invalidate_task_scheduler() {
        task_scheduler = null;
    }

    private void validate_task_scheduler() {
        task_scheduler = new TaskScheduler();
        task_scheduler.set_processes( get_processes() );
    }

    private boolean task_scheduler_valid() {
        return task_scheduler != null;
    }

    private TaskScheduler get_task_scheduler() {
        if (!task_provider_valid()) validate_task_scheduler();
        return task_scheduler;
    }

    // ##################################### INTERFACE #####################################

    /**
     * Execute current task scheduler
     */
    public void execute() {
        get_task_scheduler().execute();
    }

    public boolean is_running() {
        return get_task_scheduler().is_running();
    }

    public void join() {
        get_task_scheduler().join();
    }

    /**
     * Return current execution progress as fraction from 0 to 1
     * @return
     */
    public double get_progress() {
        int total_tasks = get_task_provider().num_tasks_total();
        int processed_tasks = get_task_provider().num_tasks_processed();
        return (double)processed_tasks/total_tasks;
    }
}
