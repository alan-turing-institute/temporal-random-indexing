/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.it.tri.occ.ati.occ;

import com.microsoft.azure.storage.blob.CloudBlockBlob;

/**
 *
 * @author pierpaolo
 */
public class OccThreadMsg {
    
    private CloudBlockBlob blob;
    
    private boolean valid;

    public OccThreadMsg(CloudBlockBlob blob) {
        this.blob = blob;
        this.valid=true;
    }

    public OccThreadMsg(CloudBlockBlob blob, boolean valid) {
        this.blob = blob;
        this.valid = valid;
    }

    public CloudBlockBlob getBlob() {
        return blob;
    }

    public void setBlob(CloudBlockBlob blob) {
        this.blob = blob;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }
    
}
