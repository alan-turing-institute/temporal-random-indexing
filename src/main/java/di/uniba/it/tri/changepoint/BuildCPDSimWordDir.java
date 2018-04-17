/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.changepoint;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pierpaolo
 */
public class BuildCPDSimWordDir {

    /**
     * args[0]=TRI main dir, args[1]=main dir, args[2]=neighborhood size
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length == 3) {
            try {
                File dir = new File(args[1]);
                if (dir.isDirectory()) {
                    File[] listFiles = dir.listFiles();
                    for (File file : listFiles) {
                        if (file.isFile() && file.getName().endsWith(".csv")) {
                            Logger.getLogger(BuildCPDSimWordDir.class.getName()).log(Level.INFO, "Working on file {0}", file.getAbsolutePath());
                            String outputFilename = file.getAbsolutePath().replace(".csv", "") + "_label.csv";
                            BuildCPDSimWord.main(new String[]{args[0], file.getAbsolutePath(), outputFilename, args[2]});
                        }
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(BuildCPDSimWordDir.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
