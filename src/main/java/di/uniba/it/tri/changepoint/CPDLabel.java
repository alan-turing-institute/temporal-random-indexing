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
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pierpaolo
 */
public class CPDLabel {

    private static final Logger LOG = Logger.getLogger(CPDLabel.class.getName());

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(args[0]));
            String[] headline = reader.readLine().split(";");
            headline = Arrays.copyOfRange(headline, 2, headline.length);
            reader.close();
            reader = new BufferedReader(new FileReader(args[1]));
            BufferedWriter writer = new BufferedWriter(new FileWriter(args[2]));
            while (reader.ready()) {
                String[] values = reader.readLine().split("\t");
                for (int i = 1; i < values.length; i = i + 2) {
                    writer.append(values[0]).append("\t").append(headline[Integer.parseInt(values[i])]);
                    writer.newLine();
                }
            }
            reader.close();
            writer.close();
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

}
