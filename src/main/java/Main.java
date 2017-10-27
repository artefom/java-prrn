import backend.Controller;
import backend.rasterio.RasterDataset;
import backend.rasterio.RasterGrid;
import frontend.*;

import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Main {

    private static Logger log = Logger.getLogger(Main.class.getName());
    private static Controller backend;
    private static UI ui;

    static class CmdPipe implements UI_Command_receiver {

        @Override
        public void add_file(String s) throws IOException {
            backend.add_file(s);
        }

        @Override
        public void execute() {
            backend.execute();
        }

        @Override
        public void set_process_count(int n) {
            backend.set_process_count(n);
        }

        @Override
        public void save_graph(String filename) {
            backend.save_graph(filename);
        }

        @Override
        public boolean is_running() {
            return backend.is_running();
        }

        @Override
        public double get_progress() {
            return backend.get_progress();
        }

        @Override
        public void join() {
            backend.join();
        }
    }

    public static void main(String [] args) {

        // Load logging properties from file!
        // Note: logging levels
        //        SEVERE (highest value)
        //        WARNING
        //        INFO
        //        CONFIG
        //        FINE
        //        FINER
        //        FINEST (lowest value)
        try {
            LogManager lm = LogManager.getLogManager();
            InputStream res_stream = Main.class.getResourceAsStream("/logging.properties");
            LogManager.getLogManager().readConfiguration(
                    Main.class.getResourceAsStream("/logging.properties"));
        } catch (IOException e) {
            System.err.println("Could not setup logger configuration: " + e.toString());
        }

        log.fine("Setting up backend...");
        //Setup backend
        backend = new Controller();

        log.fine("Setting up frontend...");
        //Setup frontend

        ui = new UI();
        CmdPipe pipe = new CmdPipe();
        ui.setReciever(pipe);

        // Process arguments
        ui.processArguments(args);

        // exit;
        System.exit(0);
    }



}
