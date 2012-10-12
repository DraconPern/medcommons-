/*
 * $Id$
 * Created on 30/03/2007
 */
package net.medcommons.phr;

import java.util.ArrayList;
import java.util.List;

public class ValidationResult {
    public List<Exception> errors = new ArrayList<Exception>();
    public List<Exception> fatal = new ArrayList<Exception>();
    public List<Exception> warnings = new ArrayList<Exception>();
    
    public boolean isClear() {
        return errors.isEmpty() && fatal.isEmpty() && warnings.isEmpty();
    }
    
    public boolean isPassed() {
        return errors.isEmpty() && fatal.isEmpty();
    }
    
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("\n\nFatals: \n");
        for (Exception e : fatal) {
            b.append(e.getMessage());
        }
         b.append("Errors: \n");
        for (Exception e : errors) {
            b.append(e.getMessage());
        }
        b.append("\n\nWarnings: \n");
        for (Exception e : warnings) {
            b.append(e.getMessage());
        }
        return b.toString();
    }
}
