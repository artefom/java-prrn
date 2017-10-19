package frontend;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
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
        HashSet<String> inputfiles = new HashSet<>();

        for (String fpath : input_args) {
            inputfiles.addAll(getFilePaths(fpath));
        }

        for (String fpath : inputfiles) {
            reciever.add_file(fpath);
        }

        output_path = output_file_path;

    }
}
