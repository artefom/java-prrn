package backend.pmanagement;

import backend.processes.IRRNProcess;

import java.util.Collection;
import java.util.logging.Logger;

public class TaskScheduler {

    // Logger
    private static Logger log = Logger.getLogger(backend.pmanagement.TaskScheduler.class.getName());
    private Collection<IRRNProcess> processes;

    public TaskScheduler() {

    }

    public void set_processes(Collection<IRRNProcess> processes) {
        this.processes = processes;
    }

    public void execute() {
        if (processes == null) return;

        for (IRRNProcess p : processes) {
            p.start();
        }
    }

    public boolean is_running() {
        boolean running = false;

        for (IRRNProcess p : processes) {
            running = running||p.is_running();
        }

        return running;
    }

    public void join() {
        for (IRRNProcess p : processes) {
            p.join();
        }
    }

}
