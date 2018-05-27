/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.changepoint;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

/**
 *
 * @author pierpaolo
 */
public class MergeYearOcc {

    private static final Logger LOG = Logger.getLogger(MergeYearOcc.class.getName());

    private static String[] getYear(String[] values) {
        Set<String> set = new HashSet<>();
        for (int i = 2; i < values.length; i++) {
            set.add(values[i].substring(2, 6));
        }
        String[] r = set.toArray(new String[set.size()]);
        Arrays.sort(r);
        return r;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            BufferedReader reader = null;
            if (args[0].endsWith(".gz")) {
                reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(args[0]))));
            } else {
                reader = new BufferedReader(new FileReader(args[0]));
            }
            BufferedWriter writer = new BufferedWriter(new FileWriter(args[1]));
            String[] header = reader.readLine().split(";");
            String[] years = getYear(header);
            writer.append("id;word");
            for (String y : years) {
                writer.append(";").append(y);
            }
            writer.newLine();
            String[] split;
            while (reader.ready()) {
                split = reader.readLine().split(";");
                writer.append(split[0]).append(";").append(split[1]);
                String yearPrev = years[0];
                int k = 2;
                long occ = 0;
                while (k < split.length) {
                    String year=header[k].substring(2, 6);
                    if (year.equals(yearPrev)) {
                        occ += Long.parseLong(split[k]);
                    } else {
                        writer.append(";").append(String.valueOf(occ));
                        occ = Long.parseLong(split[k]);
                    }
                    yearPrev = year;
                    k++;
                }
                writer.newLine();
            }
            reader.close();
            writer.close();
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

}
