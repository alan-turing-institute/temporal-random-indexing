/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.script;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

/**
 * This class builds the list of words with the number of total occurrences
 *
 * @author pierpaolo
 */
public class BuildTotalOccStat {

    /**
     * args[0]=occ statistics file, args[1]=output file
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length == 2) {
            try {
                BufferedReader in = new BufferedReader(new FileReader(args[0]));
                BufferedWriter out = new BufferedWriter(new FileWriter(args[1]));
                Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().withDelimiter(';').parse(in);
                for (CSVRecord record : records) {
                    String word = record.get(1);
                    int occ = 0;
                    for (int i = 2; i < record.size(); i++) {
                        occ += Integer.parseInt(record.get(i));
                    }
                    out.append(word).append("\t").append(String.valueOf(occ));
                    out.newLine();
                }
                in.close();
                out.close();
            } catch (IOException ioex) {
                Logger.getLogger(BuildTotalOccStat.class.getName()).log(Level.SEVERE, null, ioex);
            }
        }
    }

}
