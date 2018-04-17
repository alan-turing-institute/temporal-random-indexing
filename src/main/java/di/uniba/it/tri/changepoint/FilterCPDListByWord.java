/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.changepoint;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

/**
 *
 * @author pierpaolo
 */
public class FilterCPDListByWord {

    private static final Logger LOG = Logger.getLogger(FilterCPDListByWord.class.getName());

    private static Set<String> loadWordset(File file) throws IOException {
        BufferedReader in;
        if (file.getName().endsWith(".gz")) {
            in = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));
        } else {
            in = new BufferedReader(new FileReader(file));
        }
        Set<String> set = new ObjectOpenHashSet<>();
        while (in.ready()) {
            set.add(in.readLine().trim());
        }
        in.close();
        LOG.log(Level.INFO, "Valid set size: {0}", set.size());
        return set;
    }

    /**
     * args[0]=word list, args[1]=input file, args[2] output file
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length == 3) {
            try {
                Set<String> wordset = loadWordset(new File(args[0]));
                BufferedReader reader = new BufferedReader(new FileReader(args[1]));
                BufferedWriter writer = new BufferedWriter(new FileWriter(args[2]));
                while (reader.ready()) {
                    String line = reader.readLine();
                    String[] split = line.split("\t");
                    if (wordset.contains(split[0])) {
                        writer.write(line);
                        writer.newLine();
                    }
                }
                reader.close();
                writer.close();
            } catch (IOException ioex) {
                LOG.log(Level.SEVERE, null, ioex);
            }
        }
    }

}
