package backend.processes;

import backend.tasks.ITaskProvider;

public interface IRRNProcess {

    void set_task_provider(ITaskProvider prov);
    ITaskProvider get_task_provider();

    void start();

    void join();

    boolean is_running();

}
