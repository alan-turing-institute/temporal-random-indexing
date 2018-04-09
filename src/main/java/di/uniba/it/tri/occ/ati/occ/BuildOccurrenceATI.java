/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.occ.ati.occ;

import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;
import di.uniba.it.tri.occ.ati.AtiUtils;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

/**
 *
 * @author pierpaolo
 */
public class BuildOccurrenceATI {

    private static final Logger LOG = Logger.getLogger(BuildOccurrenceATI.class.getName());

    private static Properties props;

    private static CloudBlobContainer container;

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

    private static Set<String> loadValidTerms(File dictFile, int minOcc) throws IOException, NumberFormatException {
        LOG.log(Level.INFO, "Load dict {0}", dictFile.getName());
        Set<String> set = new ObjectOpenHashSet<>();
        BufferedReader reader = null;
        if (dictFile.getName().endsWith(".gz")) {
            reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(dictFile))));
        } else {
            reader = new BufferedReader(new FileReader(dictFile));
        }
        while (reader.ready()) {
            String[] split = reader.readLine().split("\t");
            if (Integer.parseInt(split[1]) >= minOcc) {
                set.add(split[0]);
            }
        }
        reader.close();
        LOG.log(Level.INFO, "Dict size {0}", set.size());
        return set;
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
            final ConcurrentLinkedQueue<OccThreadMsg> queue = new ConcurrentLinkedQueue<>();
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
            final Set<String> validSet = loadValidTerms(new File(props.getProperty("dict.path")), Integer.parseInt(props.getProperty("dict.minOcc")));
            int nt = Integer.parseInt(props.getProperty("thread.n"));
            List<Thread> lt = new ArrayList<>();
            for (int i = 0; i < nt; i++) {
                Thread t = new OccurrenceThread(queue, validSet, props);
                t.start();
                lt.add(t);
            }
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
                                    queue.offer(new OccThreadMsg(blob, true));
                                }
                            } else {
                                queue.offer(new OccThreadMsg(blob, true));
                            }
                        }
                    }
                }
                sc.add(Calendar.MONTH, 1);
            }
            for (int i = 0; i < nt; i++) {
                queue.offer(new OccThreadMsg(null, false));
            }
            LOG.info("Wait for threads...");
            for (int i = 0; i < nt; i++) {
                try {
                    lt.get(i).join();
                } catch (InterruptedException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

}
