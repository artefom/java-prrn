package backend.tasks;

public class TaskFactory {

    public static IRRNTask get_task() {
        return new RRNTask();
    }

}
