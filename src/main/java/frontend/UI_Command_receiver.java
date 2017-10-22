package frontend;

import java.io.IOException;

public interface UI_Command_receiver {

    void add_file(String s) throws IOException;
    void execute();
    void set_process_count(int n);
    void save_graph(String filename);

    boolean is_running();
    double get_progress();
    void join();
}
