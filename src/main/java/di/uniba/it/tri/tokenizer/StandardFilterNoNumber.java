/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.tokenizer;

import java.util.List;

/**
 *
 * @author pierpaolo
 */
public class StandardFilterNoNumber implements Filter {

    private final int n;

    private final static int MAX_LENGTH = 35;

    public StandardFilterNoNumber(int n) {
        this.n = n;
    }

    public StandardFilterNoNumber() {
        n = 2;
    }

    @Override
    public void filter(List<String> tokens) throws Exception {
        for (int i = tokens.size() - 1; i >= 0; i--) {
            if (tokens.get(i).length() > MAX_LENGTH || tokens.get(i).length() < n || !tokens.get(i).matches("^[A-Za-z]+$")) {
                tokens.remove(i);
            }
        }
    }

}
