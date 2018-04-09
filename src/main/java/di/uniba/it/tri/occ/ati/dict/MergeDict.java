/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.occ.ati.dict;

import di.uniba.it.tri.data.DictionaryEntry;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * This class merges dictionaries
 *
 * @author pierpaolo
 */
public class MergeDict {

    private static final Logger LOG = Logger.getLogger(MergeDict.class.getName());

    private static synchronized void writeDict(Map<String, Integer> dict, File outputFile) throws IOException {
        LOG.info("Write dict...");
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(outputFile))));
        List<DictionaryEntry> list = new ObjectArrayList<>(dict.size());
        for (Map.Entry<String, Integer> entry : dict.entrySet()) {
            list.add(new DictionaryEntry(entry.getKey(), entry.getValue()));
        }
        Collections.sort(list, Collections.reverseOrder());
        for (DictionaryEntry de : list) {
            writer.append(de.getWord()).append("\t").append(String.valueOf(de.getCounter()));
            writer.newLine();
        }
        writer.close();
    }

    /**
     * Merges args.length-1 dictionaries and saves the merged dictionary in
     * args.length-1
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Map<String, Integer> dict = new Object2IntOpenHashMap();
        for (int i = 0; i < args.length - 1; i++) {
            try {
                File dictFile = new File(args[i]);
                LOG.log(Level.INFO, "Load dict {0}", dictFile.getName());
                BufferedReader reader = null;
                if (dictFile.getName().endsWith(".gz")) {
                    reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(dictFile))));
                } else {
                    reader = new BufferedReader(new FileReader(dictFile));
                }
                while (reader.ready()) {
                    String[] split = reader.readLine().split("\t");
                    Integer v = dict.get(split[0]);
                    int c = Integer.parseInt(split[1]);
                    if (v == null) {
                        dict.put(split[0], c);
                    } else {
                        dict.put(split[0], v + c);
                    }

                }
                reader.close();
                LOG.log(Level.INFO, "Loaded dict size {0}", dict.size());
            } catch (IOException | NumberFormatException ex) {
                Logger.getLogger(MergeDict.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        LOG.log(Level.INFO, "Main dict size {0}", dict.size());
        try {
            writeDict(dict, new File(args[args.length - 1]));
        } catch (IOException ex) {
            Logger.getLogger(MergeDict.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
