/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.space;

import di.uniba.it.tri.api.Tri;
import di.uniba.it.tri.vectors.FileVectorReader;
import di.uniba.it.tri.vectors.ObjectVector;
import di.uniba.it.tri.vectors.Vector;
import di.uniba.it.tri.vectors.VectorReader;
import di.uniba.it.tri.vectors.VectorStoreUtils;
import di.uniba.it.tri.vectors.VectorType;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pierpaolo
 */
public class MergeSpace {

    private static final Logger LOG = Logger.getLogger(MergeSpace.class.getName());

    /**
     * args[0]=main TRI directory args[1]=output directory
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length == 2) {
            try {
                Tri tri = new Tri();
                tri.setMaindir(args[0]);
                List<String> years = tri.year(-Integer.MIN_VALUE, Integer.MAX_VALUE);
                Collections.sort(years);
                Set<String> yearprefix = new TreeSet<>();
                for (String y : years) {
                    yearprefix.add(y.substring(0, 4));
                }
                for (String yp : yearprefix) {
                    LOG.log(Level.INFO, "Merge year: {0}", yp);
                    File spacedir = new File(args[0]);
                    File[] listFiles = spacedir.listFiles();
                    Map<String, Vector> space = new Object2ObjectOpenHashMap<>();
                    int d = 0;
                    for (File file : listFiles) {
                        if (file.isFile() && file.getName().contains("D-" + yp)) {
                            LOG.log(Level.INFO, "Add space: {0}", file.getName());
                            VectorReader vr = new FileVectorReader(file);
                            vr.init();
                            d = vr.getDimension();
                            Iterator<ObjectVector> allVectors = vr.getAllVectors();
                            while (allVectors.hasNext()) {
                                ObjectVector ov = allVectors.next();
                                Vector v = space.get(ov.getKey());
                                if (v == null) {
                                    space.put(ov.getKey(), ov.getVector().copy());
                                } else {
                                    v.superpose(ov.getVector(), 1, null);
                                }
                            }
                        }
                    }
                    LOG.log(Level.INFO, "Save year: {0}", yp);
                    DataOutputStream outputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(args[1] + "/D-" + yp + "-merge.vectors")));
                    String header = VectorStoreUtils.createHeader(VectorType.REAL, d, -1);
                    outputStream.writeUTF(header);
                    int nv = 0;
                    for (Map.Entry<String, Vector> e : space.entrySet()) {
                        outputStream.writeUTF(e.getKey());
                        e.getValue().normalize();
                        e.getValue().writeToStream(outputStream);
                        nv++;
                    }
                    outputStream.close();
                    LOG.log(Level.INFO, "Merge year {0}, total vectors {1}", new Object[]{yp, nv});
                }
            } catch (Exception ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
    }

}
