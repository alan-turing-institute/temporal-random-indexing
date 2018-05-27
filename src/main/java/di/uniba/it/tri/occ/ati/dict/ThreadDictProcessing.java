/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.occ.ati.dict;

import di.uniba.it.tri.tokenizer.Filter;
import di.uniba.it.tri.tokenizer.TriTokenizer;
import di.uniba.it.tri.tokenizer.TriWhitespaceTokenizer;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

/**
 *
 * @author pierpaolo
 */
public class ThreadDictProcessing extends Thread {

    private final ConcurrentLinkedQueue<DictMsg> processingQueue;

    private final Properties props;

    private boolean run = true;

    private static final Logger LOG = Logger.getLogger(ThreadDictProcessing.class.getName());

    private int nb;

    private long bytes;

    private static final int MIN_OCC = 5;

    private static final int MAX_TEXT_LENGTH = 5 * 1024 * 1024; //5 Mbyte

    public ThreadDictProcessing(ConcurrentLinkedQueue<DictMsg> processingQueue, Properties props) {
        this.processingQueue = processingQueue;
        this.props = props;
    }

    @Override
    public void run() {
        while (run) {
            DictMsg msg = processingQueue.poll();
            if (msg != null) {
                if (msg.isValid()) {
                    File tmpFile = null;
                    try {
                        int nidx = msg.getBlob().getName().lastIndexOf("/");
                        LOG.log(Level.INFO, "Download blob {0}", msg.getBlob().getName());
                        tmpFile = new File(props.getProperty("dir.tmp") + msg.getBlob().getName().substring(nidx));
                        msg.getBlob().downloadToFile(tmpFile.getAbsolutePath());
                        LOG.log(Level.INFO, "Build dict for file {0}", tmpFile.getName());
                        BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(tmpFile))));
                        TriTokenizer tokenizer = new TriWhitespaceTokenizer();
                        Filter filter = (Filter) Class.forName("di.uniba.it.tri.tokenizer." + props.getProperty("occ.filter")).newInstance();
                        Map<String, Integer> map = new Object2IntOpenHashMap<>();
                        while (reader.ready()) {
                            try {
                                String text = reader.readLine();
                                if (text.length() > MAX_TEXT_LENGTH) {
                                    text = text.substring(0, MAX_TEXT_LENGTH);
                                }
                                List<String> tokens = tokenizer.getTokens(text);
                                filter.filter(tokens);
                                for (String token : tokens) {
                                    Integer value = map.get(token);
                                    if (value == null) {
                                        map.put(token, 1);
                                    } else {
                                        map.put(token, value + 1);
                                    }
                                }
                            } catch (Exception ex) {
                                LOG.log(Level.SEVERE, "Skip row " + tmpFile.getName(), ex);
                            }
                        }
                        reader.close();
                        nb++;
                        if (nb % 100 == 0) {
                            BuildDict.printDictSize();
                        }
                        bytes += msg.getBlob().getProperties().getLength();
                        for (Map.Entry<String, Integer> entry : map.entrySet()) {
                            if (entry.getValue() >= MIN_OCC) {
                                BuildDict.addToDict(entry.getKey(), entry.getValue());
                            }
                        }
                    } catch (Exception ex) {
                        LOG.log(Level.SEVERE, "Skip blob " + msg.getBlob().getName(), ex);
                    } finally {
                        if (tmpFile != null) {
                            tmpFile.delete();
                        }
                    }
                } else {
                    LOG.log(Level.INFO, "Stop thread, processed blobs {0}, bytes {1}.", new Object[]{nb, bytes});
                    run = false;
                }
            } else {
                try {
                    sleep(1000);
                } catch (InterruptedException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
            }
        }
    }

}
