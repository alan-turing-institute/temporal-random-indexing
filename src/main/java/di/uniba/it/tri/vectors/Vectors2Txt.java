/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.vectors;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pierpaolo
 */
public class Vectors2Txt {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length > 1) {
            try {
                String type = "file";
                if (args.length > 2) {
                    type = args[2];
                }
                System.out.println("Type: " + type);
                VectorReader vr = null;
                if (type.equalsIgnoreCase("mem")) {
                    vr = new MemoryVectorReader(new File(args[0]));
                } else if (type.equalsIgnoreCase("file")) {
                    vr = new FileVectorReader(new File(args[0]));
                } else {
                    System.err.println("No valid type: " + type);
                    System.exit(1);
                }
                vr.init();
                NumberFormat nf = NumberFormat.getInstance(Locale.ITALY);
                nf.setMaximumFractionDigits(6);
                BufferedWriter out = new BufferedWriter(new FileWriter(args[1]));
                out.append(String.valueOf(vr.getSize())).append(" ").append(String.valueOf(vr.getDimension()));
                out.newLine();
                Iterator<ObjectVector> allVectors = vr.getAllVectors();
                while (allVectors.hasNext()) {
                    ObjectVector ov = allVectors.next();
                    out.append(ov.getKey());
                    float[] coordinates = ((RealVector) ov.getVector()).getCoordinates();
                    for (float f : coordinates) {
                        out.append(" ").append(nf.format(f));
                    }
                    out.newLine();
                }
                vr.close();
                out.close();
            } catch (IOException ex) {
                Logger.getLogger(Vectors2Txt.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

}
