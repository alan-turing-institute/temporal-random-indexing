/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.occ.ati.occ;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 *
 * @author pierpaolo
 */
public class MergeOccYearBlob {

    private static final String CONNECTION_URI = "https://diachronicukwac.blob.core.windows.net/ukwac-tri?sv=2018-03-28&ss=bf&srt=sco&sp=rwl&se=2019-12-31T00:10:12Z&st=2019-01-16T16:10:12Z&spr=https&sig=Mkyyxi%2FnWFYZqCLd%2BupOX8pXVsEs2PqwOi7Ik%2BrzYHI%3D";

    private static final Logger LOG = Logger.getLogger(MergeOccYearBlob.class.getName());

    private static String extractId(String name) {
        int i1 = name.indexOf("D");
        return name.substring(i1 + 2, i1 + 6);
    }

    private static void saveOcc(BiMap<String, Integer> bimap, Map<Integer, Map<Integer, Integer>> coocc, File file) throws IOException {
        int dictSize = 0;
        long totOcc = 0;
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(file))));
        for (String word : bimap.keySet()) {
            Integer wordId = bimap.get(word);
            Map<Integer, Integer> occ = coocc.get(wordId);
            if (occ != null) {
                dictSize++;
                writer.append(word);
                for (Map.Entry<Integer, Integer> e : occ.entrySet()) {
                    totOcc += e.getValue();
                    writer.append("\t").append(bimap.inverse().get(e.getKey())).append("\t").append(e.getValue().toString());
                }
                writer.newLine();
            }
        }
        writer.close();
        LOG.log(Level.INFO, "ID={0}\tDictSize={1}\tTotOcc={2}", new Object[]{file.getName(), dictSize, totOcc});
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length > 1) {
            try {
                String prefixPath = args[0];
                String uploadPrefixPath = args[1];
                LOG.log(Level.INFO, "Prefix path: {0}", prefixPath);
                LOG.log(Level.INFO, "Upload prefix path: {0}", uploadPrefixPath);
                CloudBlobContainer mainContainer = new CloudBlobContainer(new URI(CONNECTION_URI));
                Iterable<ListBlobItem> listBlobs = mainContainer.listBlobs(prefixPath);
                List<String> blockNames = new ArrayList<>();
                for (ListBlobItem item : listBlobs) {
                    if (item instanceof CloudBlockBlob) {
                        CloudBlockBlob block = (CloudBlockBlob) item;
                        if (block.getName().endsWith("occ.gz")) {
                            blockNames.add(block.getName());
                        }
                    }
                }
                Collections.sort(blockNames);
                LOG.log(Level.INFO, "Total blobs: {0}", blockNames.size());
                String pid = "";
                int tid = 0;
                BiMap<String, Integer> bimap = null;
                Map<Integer, Map<Integer, Integer>> coocc = null;
                for (String blockName : blockNames) {
                    CloudBlockBlob ref = mainContainer.getBlockBlobReference(blockName);
                    String id = extractId(ref.getName());
                    if (!id.equals(pid)) {
                        if (bimap != null && coocc != null) {
                            LOG.log(Level.INFO, "Store for id: {0}", pid);
                            File mergeOccFile = new File("D-" + pid + "_merge_occ.gz");
                            saveOcc(bimap, coocc, mergeOccFile);
                            LOG.info("Upload...");
                            CloudBlockBlob refUp = mainContainer.getBlockBlobReference(uploadPrefixPath + "D-" + pid + "_merge_occ.gz");
                            refUp.uploadFromFile(mergeOccFile.getAbsolutePath());
                            LOG.info("Delete...");
                            mergeOccFile.delete();
                        }
                        bimap = HashBiMap.create();
                        coocc = new Int2ObjectOpenHashMap<>();
                        System.gc();
                        tid = 0;
                        pid = id;
                    }
                    File downFile = new File("./tmp_occ.gz");
                    LOG.log(Level.INFO, "Download: {0}", ref.getName());
                    ref.downloadToFile(downFile.getAbsolutePath());
                    LOG.log(Level.INFO, "Merge file: {0}", downFile.getName());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(downFile))));
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
                    LOG.info("Delete tmp...");
                    downFile.delete();
                }
            } catch (StorageException | URISyntaxException ex) {
                LOG.log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(MergeOccYearBlob.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
