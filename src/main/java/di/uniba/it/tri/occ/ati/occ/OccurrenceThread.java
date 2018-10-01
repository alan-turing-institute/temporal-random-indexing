/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.occ.ati.occ;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import di.uniba.it.tri.tokenizer.Filter;
import di.uniba.it.tri.tokenizer.TriTokenizer;
import di.uniba.it.tri.tokenizer.TriWhitespaceTokenizer;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 *
 * @author pierpaolo
 */
public class OccurrenceThread extends Thread {

    private static final Logger LOG = Logger.getLogger(OccurrenceThread.class.getName());

    private final ConcurrentLinkedQueue<OccThreadMsg> queue;

    private final Properties props;

    private boolean run = true;

    private int nb = 0;

    private long lbytes = 0;

    private final Set<String> validSet;

    private static final int MAX_TEXT_LENGTH = 5 * 1024 * 1024; //5 Mbyte

    public OccurrenceThread(ConcurrentLinkedQueue<OccThreadMsg> queue, Set<String> validSet, Properties props) {
        this.queue = queue;
        this.validSet = validSet;
        this.props = props;
    }

    @Override
    public void run() {
        while (run) {
            OccThreadMsg msg = queue.poll();
            if (msg != null) {
                if (msg.isValid()) {
                    Map<Integer, Map<Integer, Integer>> map = new Int2ObjectOpenHashMap<>();
                    BiMap<String, Integer> dict = HashBiMap.create();
                    int id = 0;
                    try {
                        CloudBlockBlob blob = (CloudBlockBlob) msg.getBlob();
                        File tmpFile = null;
                        if (blob.getName().endsWith(".gz")) {
                            int nidx = blob.getName().lastIndexOf("/");
                            try {
                                int winsize = Integer.parseInt(props.getProperty("winSize"));
                                LOG.log(Level.INFO, "Download blob for {0}", blob.getName());
                                tmpFile = new File(props.getProperty("dir.tmp") + blob.getName().substring(nidx));
                                blob.downloadToFile(tmpFile.getAbsolutePath());
                                LOG.log(Level.INFO, "Build co-occurrences for blob {0}", blob.getName());
                                BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(tmpFile))));
                                TriTokenizer tokenizer = new TriWhitespaceTokenizer();
                                Filter filter = (Filter) Class.forName("di.uniba.it.tri.tokenizer." + props.getProperty("occ.filter")).newInstance();
                                while (reader.ready()) {
                                    try {
                                        String text = reader.readLine();
                                        if (text.length() < MAX_TEXT_LENGTH) {
                                            List<String> tokens = tokenizer.getTokens(text);
                                            filter.filter(tokens);
                                            for (int i = 0; i < tokens.size(); i++) {
                                                if (validSet.contains(tokens.get(i))) {
                                                    int start = Math.max(0, i - winsize);
                                                    int end = Math.min(tokens.size(), i + winsize);
                                                    for (int j = start; j < end; j++) {
                                                        if (i != j) {
                                                            if (validSet.contains(tokens.get(j))) {
                                                                Integer tid = dict.get(tokens.get(i));
                                                                if (tid == null) {
                                                                    tid = id;
                                                                    dict.put(tokens.get(i), tid);
                                                                    id++;
                                                                }
                                                                Map<Integer, Integer> multiset = map.get(tid);
                                                                if (multiset == null) {
                                                                    multiset = new Int2IntOpenHashMap();
                                                                    map.put(tid, multiset);
                                                                }
                                                                Integer tjid = dict.get(tokens.get(j));
                                                                if (tjid == null) {
                                                                    tjid = id;
                                                                    dict.put(tokens.get(j), tjid);
                                                                    id++;
                                                                }
                                                                Integer coocc = multiset.get(tjid);
                                                                if (coocc == null) {
                                                                    multiset.put(tjid, 1);
                                                                } else {
                                                                    multiset.put(tjid, coocc + 1);
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } catch (Exception ex) {
                                        LOG.log(Level.SEVERE, "Skip row " + tmpFile.getName(), ex);
                                    }
                                }
                                reader.close();
                                lbytes += blob.getProperties().getLength();
                                nb++;
                            } catch (Exception ex) {
                                LOG.log(Level.SEVERE, "Skip block " + blob.getName(), ex);
                            } finally {
                                if (tmpFile != null) {
                                    tmpFile.delete();
                                }
                            }
                            if (dict.size() > 0) {
                                int ldidx = blob.getName().lastIndexOf("-");
                                String dateStr = blob.getName().substring(ldidx + 1, ldidx + 6 + 1);
                                File tmpDir = new File(props.getProperty("dir.out") + "/D-" + dateStr);
                                if (!tmpDir.exists()) {
                                    tmpDir.mkdirs();
                                }
                                File saveFile = new File(props.getProperty("dir.out") + "/D-" + dateStr + "/" + blob.getName().substring(nidx).replace(".gz", "_occ.gz"));
                                LOG.log(Level.INFO, "Save co-occurrences for blob {0}", saveFile.getName());
                                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(saveFile))));
                                Iterator<String> keys = dict.keySet().iterator();
                                while (keys.hasNext()) {
                                    String key = keys.next();
                                    Map<Integer, Integer> mset = map.get(dict.get(key));
                                    if (mset != null) {
                                        writer.append(key);
                                        Set<Map.Entry<Integer, Integer>> entrySet = mset.entrySet();
                                        for (Map.Entry<Integer, Integer> entry : entrySet) {
                                            writer.append("\t").append(dict.inverse().get(entry.getKey())).append("\t").append(entry.getValue().toString());
                                        }
                                        writer.newLine();
                                    }
                                }
                                writer.close();
                            }
                        }
                    } catch (IOException ex) {
                        LOG.log(Level.SEVERE, null, ex);
                    }
                    map.clear();
                    map = null;
                    dict.clear();
                    dict = null;
                    System.gc();
                } else {
                    LOG.log(Level.INFO, "Stop thread, processed block {0}, total bytes {1}", new Object[]{nb, lbytes});
                    run = false;
                }
            } else {
                try {
                    sleep(3 * 1000);
                } catch (InterruptedException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
            }
        }
    }

}
