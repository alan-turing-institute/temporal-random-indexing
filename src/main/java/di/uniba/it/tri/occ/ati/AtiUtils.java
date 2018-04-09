/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.occ.ati;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author pierpaolo
 */
public class AtiUtils {

    public static Map<String, Long> loadDataSize(String filename, double ratio) throws IOException {
        return loadDataSize(new File(filename), ratio);
    }

    public static Map<String, Long> loadDataSize(File file, double ratio) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        Map<String, Long> map = new HashMap<>();
        while (reader.ready()) {
            String[] split = reader.readLine().split("\t");
            map.put(split[0], Math.round(Double.parseDouble(split[1]) * ratio));
        }
        reader.close();
        return map;
    }

}
