package frontend;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.*;

public class UI {

    UI_Command_receiver reciever;
    Logger log = Logger.getLogger(UI.class.getName());
    String output_path;

    public UI() {
    }

    public void setReciever(UI_Command_receiver i_reciever){
        reciever = i_reciever;
    }

    private Vector<String> getFilePaths(String path) {
        // Check if file is csv
        if (path.toLowerCase().endsWith(".txt")) {
            Vector<String> ret = new Vector<>();

            BufferedReader br = null;
            String line = "";

            try {
                br = new BufferedReader(new FileReader(path));
                while ((line = br.readLine()) != null) {
                    ret.add(line);
                }
                return ret;

            } catch (IOException ex) {
                log.severe(ex.getMessage());
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        log.severe(e.getMessage());
                    }
                }
            }
        }

        // seems like we've encountered some geospatial image format
        return new Vector<String>(Arrays.asList(path));
    }

    private void save_graph() {

    }

    public void processArguments(String[] args) {

        Options options = new Options();

        Option input = new Option("i","input", true, "paths to geospatial dataset or " +
                                    "plain .txt file with paths on each line");
        input.setRequired(true);
        input.setArgs(Option.UNLIMITED_VALUES);
        options.addOption(input);

        Option output = new Option("o", "output",true, "output file");
        output.setRequired(true);
        options.addOption(output);

        Option graph_output_option = new Option("g", "graph_output",true, " .dot output of graph file");
        graph_output_option.setRequired(false);
        options.addOption(graph_output_option);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);

            System.exit(1);
            return;
        }

        String[] input_args = cmd.getOptionValues("input");
        String output_file_path = cmd.getOptionValue("output");
        String graph_output_file = cmd.getOptionValue("graph_output");

        log.info("Graph output: "+graph_output_file );

        HashSet<String> inputfiles = new HashSet<>();

        for (String fpath : input_args) {
            inputfiles.addAll(getFilePaths(fpath));
        }

        int max_input_files = 20;
        int i = 0;
        for (String fpath : inputfiles) {
            try {
                reciever.add_file(fpath);
                i += 1;
                if (i > max_input_files) {
                    log.severe( "LIMITING INPUT FILES TO "+max_input_files );
                    break;
                }
            } catch (IOException ex) {
                log.severe(ex.getMessage());
                log.log(Level.FINE,"Stack trace: ", ex);
            }
        }

        output_path = output_file_path;

        if (graph_output_file != null) {
            reciever.save_graph(graph_output_file);
        }

        System.out.println("Executing operations");
        reciever.execute();

        try {
            System.out.println();
            while (reciever.is_running()) {
                Thread.sleep(10);
                print_progress_bar("Processing...",reciever.get_progress());
            }
            final_progress_bar("Success!     ",reciever.get_progress());
        } catch (InterruptedException ignored) {};

        reciever.join();
    }

    private static final int progress_bar_size = 50;
    private static void print_progress_bar(String caption, double progress) {
        int ticks = (int)Math.round(progress*(float)progress_bar_size);
        int ticks_remaining = progress_bar_size-ticks;
        System.out.print("\r"+caption+" |");
        for (int i = 0; i != ticks; ++i) {
            System.out.print('#');
        }
        for (int i = 0; i != ticks_remaining; ++i) {
            System.out.print('.');
        }
        System.out.print("|");
    }
    private static void final_progress_bar(String caption, double progress) {
        print_progress_bar(caption,progress);
        System.out.println();
    }
}
