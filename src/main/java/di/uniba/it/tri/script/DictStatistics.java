/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.script;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

/**
 *
 * @author pierpaolo
 */
public class DictStatistics {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(args[0]))));
            long tot = 0;
            int l = 0;
            int vs = 0;
            while (in.ready()) {
                String line = in.readLine();
                String[] split = line.split("\t");
                long a = Long.parseLong(split[1]);
                if (a < 0) {
                    a = Integer.MAX_VALUE;
                }
                if (a >= 4500) {
                    tot += a;
                    vs++;
                }
                l++;
            }
            in.close();
            System.out.println("Lines: " + l);
            System.out.println("Voc size: " + vs);
            System.out.println("Tot. occ. " + tot);
        } catch (IOException ex) {
            Logger.getLogger(DictStatistics.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
