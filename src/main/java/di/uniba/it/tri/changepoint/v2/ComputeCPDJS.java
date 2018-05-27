/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.changepoint.v2;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

/**
 *
 * @author pierpaolo
 */
public class ComputeCPDJS {

    private static final Logger LOG = Logger.getLogger(ComputeCPDJS.class.getName());

    static Options options;

    static CommandLineParser cmdParser = new BasicParser();

    static {
        options = new Options();
        options.addOption("i", true, "Input file")
                .addOption("o", true, "Output file")
                .addOption("d", true, "Variance devation from the mean (default 1)");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            CommandLine cmd = cmdParser.parse(options, args);
            if (cmd.hasOption("i") && cmd.hasOption("o")) {
                try {
                    double dev = Double.parseDouble(cmd.getOptionValue("d", "1"));
                    LOG.info("Compute mean and dev...");
                    Reader in = new FileReader(cmd.getOptionValue("i"));
                    CSVFormat format = CSVFormat.DEFAULT.withDelimiter(';').withFirstRecordAsHeader().withFirstRecordAsHeader();
                    Iterable<CSVRecord> records = format.parse(in);
                    int l = 0;
                    double n = 0;
                    double[] sum = null;
                    double[] sumP = null;
                    for (CSVRecord record : records) {
                        if (sum == null) {
                            sum = new double[record.size() - 3];
                            sumP = new double[record.size() - 3];
                        }
                        for (int i = 3; i < record.size(); i++) {
                            double dp = 1 - Double.parseDouble(record.get(i));
                            sum[i - 3] += dp;
                            sumP[i - 3] += Math.pow(dp, 2);
                        }
                        n++;
                        l++;
                        if (l % 1000 == 0) {
                            System.out.print(".");
                            if (l % 10000 == 0) {
                                System.out.println(l);
                            }
                        }
                    }
                    in.close();
                    System.out.println(l);
                    double[] mean = new double[sum.length];
                    double[] var = new double[sum.length];
                    for (int i = 0; i < sum.length; i++) {
                        mean[i] = sum[i] / n;
                        var[i] = 1 / n * Math.sqrt(n * sumP[i] - Math.pow(sum[i], 2));
                    }
                    LOG.info("Searching for CP...");
                    l = 0;
                    in = new FileReader(cmd.getOptionValue("i"));
                    format = CSVFormat.DEFAULT.withDelimiter(';').withFirstRecordAsHeader();
                    records = format.parse(in);
                    BufferedWriter writer = new BufferedWriter(new FileWriter(cmd.getOptionValue("o")));
                    for (CSVRecord record : records) {
                        StringBuilder sb = new StringBuilder(record.get(1));
                        boolean found = false;
                        for (int i = 3; i < record.size(); i++) {
                            double dp = 1 - Double.parseDouble(record.get(i));
                            if (dp < 1) {
                                if ((mean[i - 3] - dp) >= dev * var[i - 3]) {
                                    found = true;
                                    sb.append("\t").append(String.valueOf(dp)).append("\t").append(String.valueOf(i - 3));
                                }
                            }
                        }
                        if (found) {
                            writer.write(sb.toString());
                            writer.newLine();
                        }
                        l++;
                        if (l % 1000 == 0) {
                            System.out.print(".");
                            if (l % 10000 == 0) {
                                System.out.println(l);
                            }
                        }
                    }
                    writer.close();
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
            } else {
                HelpFormatter helpFormatter = new HelpFormatter();
                helpFormatter.printHelp("Compute change points detection (version 2)", options, true);
            }
        } catch (ParseException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

}
