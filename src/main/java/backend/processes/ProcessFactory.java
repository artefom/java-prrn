package backend.processes;

public class ProcessFactory {

    public static IRRNProcess get_process() {
        return new RRNProcess();
    }

}
