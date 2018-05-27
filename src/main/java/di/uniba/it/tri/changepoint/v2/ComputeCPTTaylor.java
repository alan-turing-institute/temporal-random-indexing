/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.changepoint.v2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.apache.commons.math.stat.StatUtils;

/**
 *
 * @author pierpaolo
 */
public class ComputeCPTTaylor {

    private double[] buildCumulativeSum(double[] datapoint) {
        double[] cumsum = new double[datapoint.length + 1];
        cumsum[0] = 0;
        double mean = StatUtils.mean(datapoint);
        for (int i = 0; i < datapoint.length; i++) {
            cumsum[i + 1] = cumsum[i] + datapoint[i] - mean;
        }
        return cumsum;
    }

    private double sdiff(double[] cumsum) {
        return StatUtils.max(cumsum) - StatUtils.min(cumsum);
    }

    // Implementing Fisher–Yates shuffle
    private void shuffleArray(double[] ar) {
        // If running on Java 6 or older, use `new Random()` on RHS here
        Random rnd = new Random();
        for (int i = ar.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            double a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }

    public BootstrappingResult bootstrapping(double[] datapoint, int n) {
        double[] cumsum = buildCumulativeSum(datapoint);
        double sdiff = sdiff(cumsum);
        double c = 0;
        for (int k = 0; k < n; k++) {
            double[] sample = Arrays.copyOf(datapoint, datapoint.length);
            shuffleArray(sample);
            double[] sampleCumsum = buildCumulativeSum(sample);
            double sdiffSample = sdiff(sampleCumsum);
            if (sdiffSample < sdiff) {
                c++;
            }
        }
        double max = -Double.MAX_VALUE;
        int cp = -1;
        for (int k = 0; k < cumsum.length; k++) {
            if (Math.abs(cumsum[k]) > max) {
                max = Math.abs(cumsum[k]);
                cp = k;
            }
        }
        return new BootstrappingResult(c / n, max, cp - 1);
    }

    public void changePointDetection(double[] datapoint, double conf, int n, List<BootstrappingResult> points, int offset) {
        BootstrappingResult bootstrapping = bootstrapping(datapoint, n);
        if (bootstrapping.getConfidence() >= conf) {
            int j = bootstrapping.getSeriesIdx();
            bootstrapping.setSeriesIdx(j + offset);
            points.add(bootstrapping);
            changePointDetection(Arrays.copyOfRange(datapoint, 0, j), conf, n, points, 0);
            changePointDetection(Arrays.copyOfRange(datapoint, j + 1, datapoint.length), conf, n, points, j + 1);
        }
    }

    public static void main(String[] args) {
        double[] s = {0.0, 0.0, 0.6167343438497666, 0.6721268469897537, 0.6819336585091741, 0.7643597078439557, 0.34182373525574195, 0.8079833025290295, 0.8337103798509679, 0.5098600012987607, 0.7810417765407618, 0.6839321927331681, 0.6460253175415968, 0.554740569449048, 0.7146222818004221, 0.8294897363498751, 0.885375972052699, 0.8521212010798292};
        ComputeCPTTaylor cpt = new ComputeCPTTaylor();
        List<BootstrappingResult> points = new ArrayList<>();
        cpt.changePointDetection(s, 0.9, 10000, points, 0);
        for (BootstrappingResult r : points) {
            System.out.println(r);
        }
    }

}
