/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.changepoint;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pierpaolo
 */
public class FilterCPDMeanByBaseline {

    private static final Logger LOG = Logger.getLogger(FilterCPDMeanByBaseline.class.getName());

    private static Map<String, List<Integer>> loadCPD(File file) throws IOException {
        Map<String, List<Integer>> map = new HashMap<>();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        while (reader.ready()) {
            String[] split = reader.readLine().split("\t");
            List<Integer> get = map.get(split[0]);
            if (get == null) {
                get = new ArrayList<>();
                map.put(split[0], get);
            }
            get.add(Integer.parseInt(split[1]));
        }
        reader.close();
        return map;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            Map<String, List<Integer>> map = loadCPD(new File(args[0]));
            BufferedReader reader = new BufferedReader(new FileReader(args[1]));
            BufferedWriter writer = new BufferedWriter(new FileWriter(args[2]));
            while (reader.ready()) {
                String line=reader.readLine();
                String[] split = line.split("\t");
                List<Integer> get = map.get(split[0]);
                if (get!=null && get.contains(Integer.parseInt(split[1]))) {
                    writer.append(line);
                    writer.newLine();
                }
            }
            reader.close();
            writer.close();
        } catch (Exception ex) {
            Logger.getLogger(BuildCPDSimWord.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
