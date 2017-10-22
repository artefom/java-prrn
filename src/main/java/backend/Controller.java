package backend;

import java.io.IOException;

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
        tshed.set_process_count(10);
    }

    public void add_file(String path) throws IOException {
        gbuilder.add_file(path);
    }

    public void set_process_count(int n) {
        tshed.set_process_count(n);
    }

    public void execute() {
        tshed.execute();
    }

    public void save_graph(String filename) {
        gbuilder.save_graph_dot(filename);
    }

}
