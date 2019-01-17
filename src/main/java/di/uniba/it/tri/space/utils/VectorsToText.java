/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.space.utils;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;
import di.uniba.it.tri.vectors.FileVectorReader;
import di.uniba.it.tri.vectors.ObjectVector;
import di.uniba.it.tri.vectors.RealVector;
import di.uniba.it.tri.vectors.VectorReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

/**
 *
 * @author pierpaolo
 */
public class VectorsToText {

    private static final String CONNECTION_URI = "https://diachronicukwac.blob.core.windows.net/ukwac-tri?sv=2018-03-28&ss=bf&srt=sco&sp=rwl&se=2019-12-31T00:10:12Z&st=2019-01-16T16:10:12Z&spr=https&sig=Mkyyxi%2FnWFYZqCLd%2BupOX8pXVsEs2PqwOi7Ik%2BrzYHI%3D";

    private static final Logger LOG = Logger.getLogger(VectorsToText.class.getName());

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length > 0) {
            try {
                String prefixPath = args[0];
                CloudBlobContainer mainContainer = new CloudBlobContainer(new URI(CONNECTION_URI));
                Iterable<ListBlobItem> listBlobs = mainContainer.listBlobs(prefixPath);
                for (ListBlobItem item : listBlobs) {
                    if (item instanceof CloudBlockBlob) {
                        CloudBlockBlob block = (CloudBlockBlob) item;
                        if (block.getName().endsWith(".vectors")) {
                            LOG.log(Level.INFO, "Download block: {0}", block.getName());
                            File inputFile = new File("./" + block.getName().substring(prefixPath.length()));
                            block.downloadToFile(inputFile.getAbsolutePath());
                            LOG.log(Level.INFO, "Processing block: {0}", inputFile.getName());
                            VectorReader vr = new FileVectorReader(inputFile);
                            vr.init();
                            File outFile = new File("./" + block.getName().substring(prefixPath.length()) + ".txt.gz");
                            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(outFile))));
                            out.append(String.valueOf(vr.getSize())).append(" ").append(String.valueOf(vr.getDimension()));
                            out.newLine();
                            Iterator<ObjectVector> allVectors = vr.getAllVectors();
                            while (allVectors.hasNext()) {
                                ObjectVector ov = allVectors.next();
                                out.append(ov.getKey());
                                float[] coordinates = ((RealVector) ov.getVector()).getCoordinates();
                                for (float f : coordinates) {
                                    out.append(" ").append(String.valueOf(f).replace(',', '.'));
                                }
                                out.newLine();
                            }
                            vr.close();
                            out.close();
                            LOG.log(Level.INFO, "Upload block: {0}", outFile.getName());
                            CloudBlockBlob newBlock = mainContainer.getBlockBlobReference(block.getName() + ".txt.gz");
                            newBlock.uploadFromFile(outFile.getAbsolutePath());
                            LOG.info("Clean...");
                            inputFile.delete();
                            outFile.delete();
                        }
                    }
                }
            } catch (StorageException | URISyntaxException | IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
    }

}
