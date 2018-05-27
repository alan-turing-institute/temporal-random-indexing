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
package di.uniba.it.tri.vectors;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 *
 * @author pierpaolo
 */
public class MemorySparseVectorReader implements VectorReader {

    private final Map<String, SparseVector> memory = new HashMap<>();

    private final File inputFile;

    private int dimension;

    /**
     *
     * @param inputFile
     */
    public MemorySparseVectorReader(File inputFile) {
        this.inputFile = inputFile;
    }

    /**
     *
     * @throws IOException
     */
    public void init() throws IOException {
        memory.clear();
        DataInputStream inputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(inputFile)));
        Properties props = VectorStoreUtils.readHeader(inputStream.readUTF());
        dimension = Integer.parseInt(props.getProperty("-dim"));
        while (inputStream.available() > 0) {
            String key = inputStream.readUTF();
            SparseVector vector = new SparseVector(dimension);
            vector.readFromStream(inputStream);
            memory.put(key, vector);
        }
        inputStream.close();
        //logger.log(Level.INFO, "Total vectors: {0}", memory.size());
    }

    public Map<String, SparseVector> getMemory() {
        return memory;
    }
    
    public int getMemorySize() {
        return memory.size();
    }
    
    public boolean containsKey(String key) {
        return memory.containsKey(key);
    }

    /**
     *
     * @param key
     * @return
     * @throws IOException
     */
    @Override
    public Vector getVector(String key) throws IOException {
        return memory.get(key).getDenseVector();
    }

    /**
     *
     * @return @throws IOException
     */
    @Override
    public Iterator<String> getKeys() throws IOException {
        return memory.keySet().iterator();
    }

    /**
     *
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        memory.clear();
    }

    /**
     *
     * @return @throws IOException
     */
    @Override
    public Iterator<ObjectVector> getAllVectors() throws IOException {
        return new MemorySparseVectorIterator(memory);
    }

    /**
     *
     * @return
     */
    @Override
    public int getDimension() {
        return this.dimension;
    }

    @Override
    public int getSize() throws IOException {
        return this.memory.size();
    }

    /**
     *
     */
    public final class MemorySparseVectorIterator implements Iterator<ObjectVector> {

        private final Map<String, SparseVector> memory;

        private final Iterator<String> internalIterator;

        /**
         *
         * @param memory
         */
        public MemorySparseVectorIterator(Map<String, SparseVector> memory) {
            this.memory = memory;
            this.internalIterator = this.memory.keySet().iterator();
        }

        @Override
        public boolean hasNext() {
            return this.internalIterator.hasNext();
        }

        @Override
        public ObjectVector next() {
            String key = this.internalIterator.next();
            Vector v = memory.get(key).getDenseVector();
            return new ObjectVector(key, v);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }
}
