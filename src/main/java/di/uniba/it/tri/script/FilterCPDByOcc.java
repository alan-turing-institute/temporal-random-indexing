/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.script;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class removes CPDs associated with words that are below a specified
 * occurrence threshold
 *
 * @author pierpaolo
 */
public class FilterCPDByOcc {
    
    private static Set<String> loadWordset(File file, int th) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(file));
        Set<String> set = new ObjectOpenHashSet<>();
        while (in.ready()) {
            String[] split = in.readLine().split("\t");
            if (Integer.parseInt(split[1]) >= th) {
                set.add(split[0]);
            }
        }
        in.close();
        return set;
    }

    /**
     * args[0]=total occ. stat., args[1]=CPD file, args[2]=output file,
     * args[3]=threshold
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length == 4) {
            try {
                Set<String> wordset = loadWordset(new File(args[0]), Integer.parseInt(args[3]));
                BufferedReader reader = new BufferedReader(new FileReader(args[1]));
                BufferedWriter writer = new BufferedWriter(new FileWriter(args[2]));
                while (reader.ready()) {
                    String[] split = reader.readLine().split("\t");
                    StringBuilder sb = new StringBuilder();
                    sb.append(split[0]);
                    int i = 1;
                    int c = 0;
                    while (i < split.length) {
                        if (wordset.contains(split[i])) {
                            c++;
                            sb.append("\t").append(split[i]).append("\t").append(split[i + 1]);
                        }
                        i = i + 2;
                    }
                    if (c != 0) {
                        writer.write(sb.toString());
                        writer.newLine();
                    }
                }
                reader.close();
                writer.close();
            } catch (IOException ioex) {
                Logger.getLogger(FilterCPDByOcc.class.getName()).log(Level.SEVERE, null, ioex);
            }
        }
    }
    
}
