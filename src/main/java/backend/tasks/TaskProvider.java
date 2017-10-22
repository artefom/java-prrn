package backend.tasks;

import java.security.InvalidParameterException;
import java.util.*;

public class TaskProvider implements ITaskProvider {

    private List<IRRNTask> total_tasks;
    private Queue<IRRNTask> input_queue;
    private List<IRRNTask> processing_list;
    private Queue<IRRNTask> output_queue;

    public TaskProvider() {
    }

    @Override
    synchronized public IRRNTask grab() {
        if (input_queue.isEmpty()) return null;
        IRRNTask next_task = input_queue.poll();
        processing_list.add(next_task);
        return next_task;
    }

    @Override
    synchronized public void release(IRRNTask task) {
        if (!processing_list.remove(task)) {
            throw new InvalidParameterException();
        }
        output_queue.add(task);
    }

    @Override
    public int num_tasks_total() {
        return total_tasks.size();
    }

    @Override
    public int num_tasks_queued() {
        return input_queue.size();
    }

    @Override
    public int num_tasks_processing() {
        return processing_list.size();
    }

    @Override
    public int num_tasks_processed() {
        return output_queue.size();
    }

    @Override
    public void set_tasks(Collection<IRRNTask> tasks) {
        total_tasks = new ArrayList<>(tasks.size());
        input_queue = new ArrayDeque<>(tasks.size());
        processing_list = new ArrayList<>();
        output_queue = new ArrayDeque<>(tasks.size());
        for (IRRNTask t : tasks) {
            total_tasks.add(t);
            input_queue.add(t);
        }
    }
}
