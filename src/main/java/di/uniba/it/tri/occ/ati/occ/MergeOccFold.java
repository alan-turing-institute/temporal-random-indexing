/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.occ.ati.occ;

import java.io.File;

/**
 * This class marges co-occurrences that belong to the same time period. This
 * class analyzes all sub folders where each sub folder represents a specific time period.
 * 
 * @author pierpaolo
 */
public class MergeOccFold {

    /**
     * @param args the command line arguments args[0]=starting dir,
     * args[1]=delete
     */
    public static void main(String[] args) {
        if (args.length == 2) {
            File mainDir = new File(args[0]);
            if (mainDir.isDirectory()) {
                File[] dirs = mainDir.listFiles();
                for (File dir : dirs) {
                    if (dir.isDirectory() && dir.getName().startsWith("D-")) {
                        System.out.println("Merge: " + dir.getName());
                        MergeOcc.main(new String[]{dir.getAbsolutePath(), mainDir.getAbsolutePath() + "/" + dir.getName() + "_merge_occ.gz", args[1]});
                    }
                }
            }
        }
    }

}
