/**
 * Copyright (c) 2014, the Temporal Random Indexing AUTHORS.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the University of Bari nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * GNU GENERAL PUBLIC LICENSE - Version 3, 29 June 2007
 *
 */
package di.uniba.it.tri.space;

import di.uniba.it.tri.vectors.MemoryVectorReader;
import di.uniba.it.tri.vectors.Vector;
import di.uniba.it.tri.vectors.VectorFactory;
import di.uniba.it.tri.vectors.VectorStoreUtils;
import di.uniba.it.tri.vectors.VectorType;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 *
 * @author pierpaolo
 */
public class SpaceBuilderDict {

    private static final Logger LOG = Logger.getLogger(SpaceBuilderDict.class.getName());

    private int dimension = 200;

    private int seed = 10;

    private File startingDir;

    private int size = 100000;

    private boolean self = false;

    private long totalOcc = 0;

    private int minOcc = 0;

    private boolean idf = false;

    private Map<String, Double> idfMap;

    private double t = 0.001;

    private File dictFile = null;

    private File elemFile = null;

    /**
     *
     * @param startingDir
     */
    public SpaceBuilderDict(File startingDir) {
        this.startingDir = startingDir;
    }

    /**
     *
     * @param startingDir
     * @param dimension
     */
    public SpaceBuilderDict(File startingDir, int dimension) {
        this.startingDir = startingDir;
        this.dimension = dimension;
    }

    /**
     *
     * @param startingDir
     * @param dimension
     * @param seed
     */
    public SpaceBuilderDict(File startingDir, int dimension, int seed) {
        this.startingDir = startingDir;
        this.dimension = dimension;
        this.seed = seed;
    }

    public boolean isIdf() {
        return idf;
    }

    public void setIdf(boolean idf) {
        this.idf = idf;
    }

    /**
     *
     * @return
     */
    public int getDimension() {
        return dimension;
    }

    /**
     *
     * @param dimension
     */
    public void setDimension(int dimension) {
        this.dimension = dimension;
    }

    /**
     *
     * @return
     */
    public int getSeed() {
        return seed;
    }

    /**
     *
     * @param seed
     */
    public void setSeed(int seed) {
        this.seed = seed;
    }

    /**
     *
     * @return
     */
    public File getStartingDir() {
        return startingDir;
    }

    /**
     *
     * @param startingDir
     */
    public void setStartingDir(File startingDir) {
        this.startingDir = startingDir;
    }

    /**
     *
     * @return
     */
    public int getSize() {
        return size;
    }

    /**
     *
     * @param size
     */
    public void setSize(int size) {
        this.size = size;
    }

    public boolean isSelf() {
        return self;
    }

    public void setSelf(boolean self) {
        this.self = self;
    }

    public double getT() {
        return t;
    }

    public void setT(double t) {
        this.t = t;
    }

    public File getDictFile() {
        return dictFile;
    }

    public void setDictFile(File dictFile) {
        this.dictFile = dictFile;
    }

    public int getMinOcc() {
        return minOcc;
    }

    public void setMinOcc(int minOcc) {
        this.minOcc = minOcc;
    }

    public File getElemFile() {
        return elemFile;
    }

    public void setElemFile(File elemFile) {
        this.elemFile = elemFile;
    }

    private double idf(String word, double wordOcc) {
        Double idf = idfMap.get(word);
        if (idf == null) {
            idf = Math.log((double) totalOcc / wordOcc) / Math.log(2);
            idfMap.put(word, idf);
        }
        return idf;
    }

    /**
     *
     * @param outputDir
     * @throws IOException
     */
    public void build(File outputDir) throws IOException {
        if (!outputDir.exists()) {
            outputDir.mkdir();
        }
        Map<String, Integer> dict = loadDict();
        LOG.log(Level.INFO, "Use self random vector: {0}", self);
        LOG.log(Level.INFO, "IDF score: {0}", idf);
        Map<String, Vector> elementalSpace = null;
        if (elemFile == null) {
            elementalSpace = new HashMap<>();
            //create random vectors space
            LOG.info("Building elemental vectors...");
            Random random = new Random();
            for (String word : dict.keySet()) {
                elementalSpace.put(word, VectorFactory.generateRandomVector(VectorType.REAL, dimension, seed, random));
            }
        } else {
            LOG.log(Level.INFO, "Loading random vectors from file {0}...", elemFile.getAbsolutePath());
            MemoryVectorReader vr = new MemoryVectorReader(elemFile);
            vr.init();
            elementalSpace = vr.getMemory();
        }
        LOG.log(Level.INFO, "Total occurrences {0}", totalOcc);
        LOG.log(Level.INFO, "Building spaces: {0}", startingDir.getAbsolutePath());
        File[] listFiles = startingDir.listFiles();
        for (File file : listFiles) {
            //init idf for each year
            idfMap = new HashMap<>();
            LOG.log(Level.INFO, "Space: {0}", file.getAbsolutePath());
            BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));
            DataOutputStream outputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputDir.getAbsolutePath() + "/" + file.getName() + ".vectors")));
            String header = VectorStoreUtils.createHeader(VectorType.REAL, dimension, seed);
            outputStream.writeUTF(header);
            String[] split;
            if (elementalSpace != null) {
                while (reader.ready()) {
                    split = reader.readLine().split("\t");
                    String token = split[0];
                    if (elementalSpace.containsKey(token)) {
                        Vector v;
                        if (self) {
                            v = elementalSpace.get(token).copy();
                        } else {
                            v = VectorFactory.createZeroVector(VectorType.REAL, dimension);
                        }
                        int i = 1;
                        while (i < split.length) {
                            String word = split[i];
                            Vector ev = elementalSpace.get(word);
                            if (ev != null) {
                                double f = dict.get(word).doubleValue() / (double) totalOcc; //downsampling
                                double p = 1;
                                if (f > t) { //if word frequency is greater than the threshold, compute the probability of consider the word 
                                    p = Math.sqrt(t / f);
                                }
                                double w = Double.parseDouble(split[i + 1]) * p;
                                if (idf) {
                                    w = w * idf(word, dict.get(word).doubleValue());
                                }
                                v.superpose(ev, w, null);
                            }
                            i = i + 2;
                        }
                        if (!v.isZeroVector()) {
                            v.normalize();
                            outputStream.writeUTF(token);
                            v.writeToStream(outputStream);
                        }
                    }
                }
                reader.close();
                outputStream.close();
            }
        }
        if (elemFile == null && elementalSpace != null) {
            LOG.log(Level.INFO, "Save elemental vectors in dir: {0}", outputDir.getAbsolutePath());
            VectorStoreUtils.saveSpace(new File(outputDir.getAbsolutePath() + "/vectors.elemental"), elementalSpace, VectorType.REAL, dimension, seed);
        }
    }

    private Map<String, Integer> loadDict() throws IOException {
        LOG.log(Level.INFO, "Building dictionary: {0}", startingDir.getAbsolutePath());
        Map<String, Integer> dict = new Object2IntOpenHashMap();
        LOG.log(Level.INFO, "Load dict {0}", dictFile.getName());
        BufferedReader reader = null;
        if (dictFile.getName().endsWith(".gz")) {
            reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(dictFile))));
        } else {
            reader = new BufferedReader(new FileReader(dictFile));
        }
        totalOcc = 0;
        while (reader.ready()) {
            String[] split = reader.readLine().split("\t");
            int occ = Integer.parseInt(split[1]);
            if (occ >= minOcc) {
                dict.put(split[0], occ);
                totalOcc += occ;
            }
        }
        reader.close();
        LOG.log(Level.INFO, "Dict size {0}", dict.size());
        return dict;
    }

    static Options options;

    static CommandLineParser cmdParser = new BasicParser();

    static {
        options = new Options();
        options.addOption("c", true, "The directory containing the co-occurrences matrices")
                .addOption("o", true, "Output directory where WordSpaces will be stored")
                .addOption("d", true, "The vector dimension (optional, default is 300)")
                .addOption("s", true, "The number of seeds (optional, default is 10)")
                .addOption("dict", true, "The dictionary file")
                .addOption("elem", true, "Random vectors files (optional)")
                .addOption("min-occ", true, "This will discard words that appear less than <int> times (optional, default is 5)")
                .addOption("idf", true, "Enable IDF (optinal, default is false)")
                .addOption("self", true, "Inizialize using random vector (optinal, default is false)")
                .addOption("t", true, "Threshold for downsampling frequent words (optinal, default is 0.001)");
    }

    /**
     * Build WordSpace using Temporal Random Indexing
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            CommandLine cmd = cmdParser.parse(options, args);
            if (cmd.hasOption("c") && cmd.hasOption("o") && cmd.hasOption("dict")) {
                try {
                    SpaceBuilderDict builder = new SpaceBuilderDict(new File(cmd.getOptionValue("c")));
                    builder.setDimension(Integer.parseInt(cmd.getOptionValue("d", "300")));
                    builder.setSeed(Integer.parseInt(cmd.getOptionValue("s", "10")));
                    builder.setSize(Integer.parseInt(cmd.getOptionValue("v", "100000")));
                    builder.setIdf(Boolean.parseBoolean(cmd.getOptionValue("idf", "false")));
                    builder.setSelf(Boolean.parseBoolean(cmd.getOptionValue("self", "false")));
                    builder.setT(Double.parseDouble(cmd.getOptionValue("t", "0.001")));
                    builder.setDictFile(new File(cmd.getOptionValue("dict")));
                    builder.setMinOcc(Integer.parseInt(cmd.getOptionValue("min-occ", "5")));
                    if (cmd.hasOption("elem")) {
                        builder.setElemFile(new File(cmd.getOptionValue("elem")));
                    }
                    builder.build(new File(cmd.getOptionValue("o")));
                } catch (IOException | NumberFormatException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
            } else {
                HelpFormatter helpFormatter = new HelpFormatter();
                helpFormatter.printHelp("Build WordSpace using Temporal Random Indexing", options, true);
            }
        } catch (ParseException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }
}
