/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.changepoint;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pierpaolo
 */
public class CPDBaseline {

    private static final Logger LOG = Logger.getLogger(CPDBaseline.class.getName());

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(args[0]));
            BufferedWriter writer = new BufferedWriter(new FileWriter(args[1]));
            String line = reader.readLine();
            String[] headline = line.split(";");
            while (reader.ready()) {
                line = reader.readLine();
                String[] values = line.split(";");
                String word = values[1];
                double prec = -Double.MAX_VALUE;
                for (int i = 2; i < values.length; i++) {
                    double v = Double.parseDouble(values[i]);
                    if (v < prec) {
                        writer.append(word).append("\t").append(headline[i]);
                        writer.newLine();
                    }
                    prec = v;
                }
            }
            writer.close();
            reader.close();
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

}
