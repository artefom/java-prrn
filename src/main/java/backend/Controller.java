package backend;

public class Controller {

    private GraphBuilder gbuilder;
    private TaskScheduler tshed;
    private RRNTaskFactory tfact;
    private RRNProcessFactory pfact;

    /**
     * Initializes programm structure.
     */
    public Controller() {
        gbuilder = new GraphBuilder();
        tshed = new TaskScheduler();

        tfact = new RRNTaskFactory();
        pfact = new RRNProcessFactory();

        gbuilder.set_task_factory(tfact);
        tshed.set_process_factory(pfact);
        tshed.set_task_provider(gbuilder);
    }

    public void add_file(String path) {
        gbuilder.add_file(path);
    }

    public void set_process_count(int n) {
        tshed.set_process_count(n);
    }

    public void execute() {
        tshed.set_process_count(10);
        gbuilder.build_graph();
        tshed.execute();
    }

    public void save_graph(String filename) {
        gbuilder.save_graph_dot(filename);
    }

}
