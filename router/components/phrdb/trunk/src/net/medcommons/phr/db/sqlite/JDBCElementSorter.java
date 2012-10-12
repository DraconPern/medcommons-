/*
 * $Id$
 * Created on 04/04/2007
 */
package net.medcommons.phr.db.sqlite;

import java.util.Comparator;

import net.medcommons.phr.db.sqlite.JDBCPHRElement.Disposition;

import org.apache.log4j.Logger;

/**
 * A comparator for ordering PHRDB entries based on the 
 * structured sequence number.  
 * 
 * @author ssadedin
 */
public class JDBCElementSorter implements Comparator {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(JDBCElementSorter.class);

    public int compare(Object o1, Object o2) {
        JDBCPHRElement e1 = (JDBCPHRElement) o1;
        JDBCPHRElement e2 = (JDBCPHRElement) o2;
        
        String s1 = e1.getSeq();
        String s2 = e2.getSeq();
        int result =  compareSequences(s1, s2);
        //log.debug(s1 + " : " + s2 + " => " + result);
        
        // If they are equal by sequence, return child elements first
        // This allows parsing algorithm to know an element will arrive before it's meta data
        if(result == 0) {
            if(e1.getDisposition() == e2.getDisposition())
                return 0;
            else 
                return  e1.getDisposition() == Disposition.CHILD ? -1  : 1;
        }
        else
            return result;
    }

    /**
     * Compares sequences per normal compare() semantics
     * <p/>
     * This implementation looks complicated, but that is because it goes to great 
     * lengths to avoid creating any new strings or other objects to maximize performance. 
     * 
     * @param seq1
     * @param seq2
     * @return
     */
    public static int compareSequences(String seq1, String seq2) {
        final int l1 = seq1.length();
        final int l2 = seq2.length();

        // Find the first char that does not match
        for(int i=0; i<l1 && i<l2 ;++i) {            
            if(seq1.charAt(i) != seq2.charAt(i)) { // character mismatch, we can compare just this segment
                // find where the segment ends in each case
                int dot1 = seq1.indexOf('.', i);
                if(dot1<0)
                    dot1=l1;
                int dot2 = seq2.indexOf('.', i);
                if(dot2<0)
                    dot2=l2;
                
                if(dot1 == dot2) { // segment is same length, we have to look at it
                    do {
                        int delta = seq1.charAt(i)-seq2.charAt(i);
                        if(delta!=0)
                            return delta;
                    } while(++i<dot1);
                    return 0; // equal
                }
                else
                    return dot1 - dot2; // not same length, we don't need to examine contents, longer segment is bigger
            }
        }
        return l1-l2; // one is an exact substring of the other, eg.  "0" vs "0.1" - longer one wins.
    }

}
