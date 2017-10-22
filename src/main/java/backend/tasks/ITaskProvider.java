package backend.tasks;

import java.util.Collection;

public interface ITaskProvider {

    void set_tasks(Collection<IRRNTask> tasks);

    /**
     * Should return NULL as a poison pill of no grab element!
     * @return
     */
    IRRNTask grab();

    void release(IRRNTask task);

    int num_tasks_total();
    int num_tasks_queued();
    int num_tasks_processing();
    int num_tasks_processed();

}
