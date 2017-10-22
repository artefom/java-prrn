package backend.processes;

import backend.tasks.ITaskProvider;

import java.util.logging.Logger;

/**
 * Analog if Future class. Provides necessary methods for async task execution
 */
class RRNProcess implements IRRNProcess {

    Logger log = Logger.getLogger(RRNProcess.class.getName());
    ITaskProvider task_provider;

    Thread t;

    @Override
    public void set_task_provider(ITaskProvider prov) {
        this.task_provider = prov;
    }

    @Override
    public ITaskProvider get_task_provider() {
        return null;
    }

    @Override
    public void start() {
        log.info("Process start");

        // Spawn process and thread
        t = new Thread(new RRNThread(task_provider));
        t.start();
    }

    @Override
    public void join() {
        if (t == null) return;

        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        log.info("Process joined");
    }

    @Override
    public boolean is_running() {
        if (t == null) return false;
        return t.isAlive();
    }
}
