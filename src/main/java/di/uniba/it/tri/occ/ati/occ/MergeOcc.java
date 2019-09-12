/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.occ.ati.occ;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * This class marges co-occurrences that belong to the same time period. This
 * class analyzes a single folder that represents a specific time period.
 *
 * @author pierpaolo
 */
public class MergeOcc {

    private static final Logger LOG = Logger.getLogger(MergeOcc.class.getName());

    /**
     * @param args the command line arguments args[0]=input dir, args[1]=out
     * file, args[2] delete files true/false
     */
    public static void main(String[] args) {
        if (args.length == 3) {
            try {
                File mainDir = new File(args[0]);
                int tid = 0;
                BiMap<String, Integer> bimap = HashBiMap.create();
                Map<Integer, Map<Integer, Integer>> coocc = new Int2ObjectOpenHashMap<>();
                File[] files = mainDir.listFiles();
                boolean deleteFile = Boolean.parseBoolean(args[2]);
                for (File file : files) {
                    if (file.getName().endsWith(".gz")) {
                        try {
                            LOG.log(Level.INFO, "Merge file: {0}", file.getName());
                            BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));
                            while (reader.ready()) {
                                String[] split = reader.readLine().split("\t");
                                Integer pvtid = bimap.get(split[0]);
                                if (pvtid == null) {
                                    pvtid = tid;
                                    bimap.put(split[0], pvtid);
                                    tid++;
                                }
                                Map<Integer, Integer> bag = coocc.get(pvtid);
                                if (bag == null) {
                                    bag = new Int2IntOpenHashMap();
                                    coocc.put(pvtid, bag);
                                }
                                int k = 1;
                                while (k < split.length) {
                                    Integer ctid = bimap.get(split[k]);
                                    if (ctid == null) {
                                        ctid = tid;
                                        bimap.put(split[k], ctid);
                                        tid++;
                                    }
                                    k++;
                                    Integer count = bag.get(ctid);
                                    if (count == null) {
                                        bag.put(ctid, Integer.parseInt(split[k]));
                                    } else {
                                        bag.put(ctid, count + Integer.parseInt(split[k]));
                                    }
                                    k++;
                                }
                            }
                            reader.close();
                        } catch (Exception ex) {
                            LOG.log(Level.SEVERE, null, ex);
                        }
                        if (deleteFile) {
                            file.delete();
                        }
                    }
                }
                LOG.log(Level.INFO, "Save merge: {0}", args[1]);
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(args[1]))));
                for (String word : bimap.keySet()) {
                    Integer wordId = bimap.get(word);
                    Map<Integer, Integer> occ = coocc.get(wordId);
                    if (occ != null) {
                        writer.append(word);
                        for (Map.Entry<Integer, Integer> e : occ.entrySet()) {
                            writer.append("\t").append(bimap.inverse().get(e.getKey())).append("\t").append(e.getValue().toString());
                        }
                        writer.newLine();
                    }
                }
                writer.close();
            } catch (Exception ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
    }

}
