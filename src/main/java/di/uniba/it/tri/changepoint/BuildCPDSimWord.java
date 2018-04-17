/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.changepoint;

import di.uniba.it.tri.api.Tri;
import di.uniba.it.tri.vectors.ObjectVector;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pierpaolo
 */
public class BuildCPDSimWord {

    /**
     * args[0]=TRI main dir, args[1]=input file, args[2]=output file,
     * args[3]=neighborhood size
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length == 4) {
            try {
                Tri tri = new Tri();
                tri.setMaindir(args[0]);
                List<String> years = tri.year(-Integer.MAX_VALUE, Integer.MAX_VALUE);
                Collections.sort(years);
                BufferedReader reader = new BufferedReader(new FileReader(args[1]));
                BufferedWriter writer = new BufferedWriter(new FileWriter(args[2]));
                int n = Integer.parseInt(args[3]);
                while (reader.ready()) {
                    String[] split = reader.readLine().split("\t");
                    String word = split[0];
                    int i = 1;
                    while (i < split.length) {
                        int idx = Integer.parseInt(split[i]);
                        String yyyymm = years.get(idx);
                        writer.append(word).append("\t").append(yyyymm);
                        if (n > 0) {
                            if (!tri.containsStore(yyyymm)) {
                                tri.load("file", yyyymm, yyyymm, false);
                            }
                            if (!tri.containsVector(yyyymm + "_" + word)) {
                                tri.get(yyyymm, yyyymm + "_" + word, word);
                            }
                            List<ObjectVector> near = tri.near(yyyymm, yyyymm + "_" + word, n);
                            for (int k = 0; k < near.size(); k++) {
                                writer.append("\t").append(near.get(k).getKey()).append("\t").append(String.valueOf(near.get(k).getScore()));
                            }
                        }
                        writer.newLine();
                        i = i + 2;
                    }
                }
                reader.close();
                writer.close();
            } catch (Exception ex) {
                Logger.getLogger(BuildCPDSimWord.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
