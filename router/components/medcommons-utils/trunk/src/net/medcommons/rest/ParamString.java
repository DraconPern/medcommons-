package net.medcommons.rest;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class designed to assist in constructing parameter strings.
 * 
 * @author ssadedin
 */
public class ParamString {
    
    List<String[]> params = new ArrayList<String[]>();
    
    public ParamString() {
    }
   
    public ParamString add(String name, String value) {
        this.params.add(new String[] { name, value });
        return this;
    }
    
    /**
     * Return one dimensional array with alternating parameters and values
     */
    public String[] flatten() {
        String [] result = new String[params.size() * 2];
        int i = 0;
        for(String[] p : params) {
            result[i++] = p[0];
            result[i++] = p[1];
        }
        return result;
    }
}
