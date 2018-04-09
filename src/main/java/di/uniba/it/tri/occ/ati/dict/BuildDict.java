/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.occ.ati.dict;

import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;
import di.uniba.it.tri.data.DictionaryEntry;
import di.uniba.it.tri.occ.ati.AtiUtils;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

/**
 *
 * @author pierpaolo
 */
public class BuildDict {

    private static final Logger LOG = Logger.getLogger(BuildDict.class.getName());

    private static Properties props;

    private static CloudBlobContainer container;

    private static final Map<String, Integer> dict = new Object2IntOpenHashMap<>();

    private static String calendarToDateStr(Calendar calendar) {
        return String.valueOf(calendar.get(Calendar.YEAR)) + String.format("%02d", calendar.get(Calendar.MONTH) + 1);
    }

    private static Calendar dateStrToCalendar(String dateStr) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, Integer.parseInt(dateStr.substring(0, 4)));
        c.set(Calendar.MONTH, Integer.parseInt(dateStr.substring(4, 6)) - 1); //months begin from 0
        c.set(Calendar.DATE, 1);
        return c;
    }

    public static synchronized void addToDict(String word) {
        Integer value = dict.get(word);
        if (value == null) {
            dict.put(word, 1);
        } else {
            dict.put(word, value + 1);
        }
    }

    public static synchronized void addToDict(String word, int occ) {
        Integer value = dict.get(word);
        if (value == null) {
            dict.put(word, occ);
        } else {
            dict.put(word, value + occ);
        }
    }

    public static synchronized void printDictSize() {
        LOG.log(Level.INFO, "Dict size: {0}", dict.size());
    }

    private static synchronized void writeDict() throws IOException {
        LOG.info("Write dict...");
        printDictSize();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(props.getProperty("dict.path")))));
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
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            props = new Properties();
            props.load(new FileReader("config.properties"));
            final String uri = props.getProperty("storage.uri");
            container = new CloudBlobContainer(new URI(uri));
            final ConcurrentLinkedQueue<DictMsg> queue = new ConcurrentLinkedQueue<>();
            final boolean sampling = Boolean.parseBoolean(props.getProperty("sampling.enable"));
            Map<String, Long> datasize = null;
            Map<String, Long> dataread = null;
            if (sampling) {
                datasize = AtiUtils.loadDataSize(props.getProperty("sampling.file"), Double.parseDouble(props.getProperty("sampling.ratio")));
                dataread = new HashMap<>();
                for (String key : datasize.keySet()) {
                    dataread.put(key, 0L);
                }
            }
            int nt = Integer.parseInt(props.getProperty("thread.n"));
            List<Thread> lt = new ArrayList<>();
            for (int i = 0; i < nt; i++) {
                Thread t = new ThreadDictProcessing(queue, props);
                t.start();
                lt.add(t);
            }
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        writeDict();
                    } catch (Exception ex) {
                        LOG.log(Level.SEVERE, null, ex);
                    }
                }
            }));
            Calendar sc = dateStrToCalendar(props.getProperty("date.start"));
            Calendar ec = dateStrToCalendar(props.getProperty("date.end"));
            while (sc.before(ec)) {
                LOG.log(Level.INFO, "Process date {0}", calendarToDateStr(sc));
                String fullprefix = props.getProperty("storage.prefix") + "/D-" + calendarToDateStr(sc);
                Iterable<ListBlobItem> listBlobs = container.listBlobs(fullprefix + "/");
                for (ListBlobItem item : listBlobs) {
                    if (item instanceof CloudBlockBlob) {
                        CloudBlockBlob blob = (CloudBlockBlob) item;
                        if (blob.getName().endsWith(".gz")) {
                            while (queue.size() >= 1000) {
                                Thread.sleep(3 * 1000);
                            }
                            if (sampling) {
                                Long limit = datasize.get(fullprefix);
                                if (limit != null && dataread.get(fullprefix) < limit) {
                                    dataread.put(fullprefix, dataread.get(fullprefix) + blob.getProperties().getLength());
                                    queue.offer(new DictMsg(blob, true));
                                }
                            } else {
                                queue.offer(new DictMsg(blob, true));
                            }
                        }
                    }
                }
                sc.add(Calendar.MONTH, 1);
            }
            for (int i = 0; i < nt; i++) {
                queue.offer(new DictMsg(null, false));
            }
            LOG.info("Wait for processing threads...");
            for (int i = 0; i < nt; i++) {
                try {
                    lt.get(i).join();
                } catch (InterruptedException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
            }
            writeDict();
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

}
