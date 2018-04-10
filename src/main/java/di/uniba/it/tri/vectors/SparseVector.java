/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.vectors;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pierpaolo
 */
public class SparseVector {

    private int[] coordinates;

    private float[] values;

    private final int dimension;

    public SparseVector(int dimension) {
        this.dimension = dimension;
    }

    public SparseVector(int dimension, int[] coordinates, float[] values) {
        this.dimension = dimension;
        this.coordinates = coordinates;
        this.values = values;
    }

    public Vector getDenseVector() {
        float[] fv = new float[dimension];
        Arrays.fill(fv, 0f);
        for (int i = 0; i < coordinates.length; i++) {
            fv[coordinates[i]] = values[i];
        }
        return new RealVector(fv);
    }

    public void setDenseVector(float[] v) {
        int[] tc = new int[dimension];
        float[] tv = new float[dimension];
        int n = 0;
        for (int i = 0; i < v.length; i++) {
            if (v[i] != 0) {
                tc[n] = i;
                tv[n] = v[i];
                n++;
            }
        }
        coordinates = Arrays.copyOf(tc, n);
        values = Arrays.copyOf(tv, n);
    }

    public void readFromStream(DataInputStream inputStream) {
        try {
            int n = inputStream.readInt();
            coordinates = new int[n];
            values = new float[n];
            for (int i = 0; i < n; i++) {
                coordinates[i] = inputStream.readInt();
                values[i] = Float.intBitsToFloat(inputStream.readInt());
            }
        } catch (IOException ex) {
            Logger.getLogger(SparseVector.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void writeToStream(DataOutputStream outputStream) {
        try {
            outputStream.writeInt(coordinates.length);
            for (int i = 0; i < coordinates.length; i++) {
                outputStream.writeInt(coordinates[i]);
                outputStream.writeInt(Float.floatToIntBits(values[i]));
            }
        } catch (IOException ex) {
            Logger.getLogger(SparseVector.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {
        SparseVector v1 = new SparseVector(20, new int[]{0, 4, 6, 13, 19}, new float[]{0.43f, 0.21f, -1.23f, 3.2f, -3.12f});
        System.out.println(v1.getDenseVector());
        SparseVector v2 = new SparseVector(10);
        v2.setDenseVector(new float[]{4, -2, 6, 21, -5, 32, 12, 54, 23, -21});
        System.out.println(v2.getDenseVector());
        File tmpFile = new File("sv.tmp");
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(tmpFile));
            v2.writeToStream(out);
            out.close();
            SparseVector v3=new SparseVector(10);
            DataInputStream in=new DataInputStream(new FileInputStream(tmpFile));
            v3.readFromStream(in);
            System.out.println(v3.getDenseVector());
            tmpFile.delete();
        } catch (IOException ex) {
            Logger.getLogger(SparseVector.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
