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

/**
 * This class removes CPDs that are above a specified threshold
 *
 * @author pierpaolo
 */
public class FilterCPD {

    /**
     * args[0]=CPD file, args[1]=output file, args[2]=threshold
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length == 3) {
            try {
                double th = Double.parseDouble(args[2]);
                BufferedReader reader = new BufferedReader(new FileReader(args[0]));
                BufferedWriter writer = new BufferedWriter(new FileWriter(args[1]));
                while (reader.ready()) {
                    String[] split = reader.readLine().split("\t");
                    StringBuilder sb = new StringBuilder();
                    sb.append(split[0]);
                    int i = 1;
                    int c = 0;
                    while (i < split.length) {
                        if (Double.parseDouble(split[i + 1]) <= th) {
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
                Logger.getLogger(FilterCPD.class.getName()).log(Level.SEVERE, null, ioex);
            }
        }
    }

}
