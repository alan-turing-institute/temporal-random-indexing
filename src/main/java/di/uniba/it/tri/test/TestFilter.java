/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.test;

import di.uniba.it.tri.tokenizer.Filter;
import di.uniba.it.tri.tokenizer.StandardFilterNoNumber;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pierpaolo
 */
public class TestFilter {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            Filter filter=new StandardFilterNoNumber();
            List<String> tokens=new ArrayList<>();
            //tokens.add("23");
            //tokens.add("23");
            filter.filter(tokens);
            System.out.println(tokens);
        } catch (Exception ex) {
            Logger.getLogger(TestFilter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
