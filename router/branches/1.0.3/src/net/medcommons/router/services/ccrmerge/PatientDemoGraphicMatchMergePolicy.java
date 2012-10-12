/*
 * $Id: PatientDemoGraphicMatchMergePolicy.java 3155 2008-12-23 11:02:16Z ssadedin $
 * Created on 27/03/2007
 */
package net.medcommons.router.services.ccrmerge;

import static net.medcommons.modules.utils.Str.blank;
import static net.medcommons.modules.utils.Str.equalNormalized;

import java.util.Calendar;

import net.medcommons.modules.utils.DateFormats;
import net.medcommons.modules.utils.Str;
import net.medcommons.phr.PHRException;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;

import org.apache.log4j.Logger;

/**
 * Only allows merge of top level documents if the patient demographics match.
 * 
 * @author ssadedin
 */
public class PatientDemoGraphicMatchMergePolicy implements MergePolicy {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(PatientDemoGraphicMatchMergePolicy.class);
    
    private static String [] requiredMatches = new String[] { "patientGivenName", "patientFamilyName", "patientSex" };
    
    public PolicyResult canMerge(CCRDocument mergeFrom, CCRDocument mergeTo) throws MergeException {
        
        DemoGraphicMatchResult result = null;
        
        try {
            for(final String attribute : requiredMatches) {
                // If the target CCR has the value and it does not match the original, refuse
                String toValue = mergeTo.getValue(attribute);
                String fromValue = mergeFrom.getValue(attribute);
                if(!blank(toValue) && !blank(fromValue)) {
                    if(!equalNormalized(mergeFrom.getValue(attribute), toValue)) {
                       if(result == null)
                           result = new DemoGraphicMatchResult(false, "Attribute " + attribute + " mismatches in patient demographics", attribute);
                        else
                           result.mismatches.add(attribute);
                       
                       log.info("Merge policy failure on attribute: " + attribute + " to="+toValue + ", from = " + fromValue);
                    }                
                }
            }
            
            if(!checkDateMatch(mergeFrom.getValue("patientExactDateOfBirth"), mergeTo.getValue("patientExactDateOfBirth"))) {
                if(result == null)
                    result = new DemoGraphicMatchResult(false, "Attribute patient date of birth mismatches in patient demographics", "patientExactDateOfBirth");
                else
                    result.mismatches.add("patientExactDateOfBirth");
            }
            
            // All matched
            if(result == null)
                result =  new DemoGraphicMatchResult(true, "Merge Allowed", null);
            
            return result;
        }
        catch (PHRException e) {
            throw new MergeException("Unable to test merge policy", e);
        }
    }

    /**
     * Perform a flexible match between the two dates, taking into account that they
     * may be specified in different formats containing variable levels of precision.
     */
    protected boolean checkDateMatch(String date1, String date2) {
        // Quick short cut for a common case - both identical
        if(Str.eq(date1,date2))
            return true;
        
        // If either is blank, consider it a match
        if(blank(date1) || blank(date2))
            return true;
        
        Calendar d1=null,d2=null;
        
        // Determine the precision level at which to perform the comparison
        DateFormats.Specificity s1=null, s2=null;
        for(DateFormats.Format format : DateFormats.all()) {
            d1 = format.parse(date1);
            if(d1 != null) {
                s1 = format.specificity;
                break;
            }
        }
        
        if(log.isDebugEnabled()) 
            log.debug("Date " + date1 + " has specificity " + s1);
        
        for(DateFormats.Format format : DateFormats.all()) {
            d2 = format.parse(date2);
            if(d2 != null) {
                s2 = format.specificity;
                break;
            }
        }
        
        if(d1 == null || d2 == null) {
            // Either or both of the dates are unparseable.  This is really an invalid CCR
            // treat it as a mismatch
            log.info("One of " + date1 + " and " + date2 + " is unparseable and hence treated as mismatch");
            return false;
        }
            
        
        if(log.isDebugEnabled())
            log.debug("Date " + date2 + " has specificity " + s2);
        DateFormats.Specificity s = s2;
        if(s1.ordinal() < s2.ordinal())
            s = s1;
        
        if(log.isDebugEnabled())
            log.debug("Overall specificity is " + s);
        switch(s) {
            case YEAR:
                return d1.get(Calendar.YEAR) == d2.get(Calendar.YEAR);
                
            case MONTH:
                return d1.get(Calendar.YEAR) == d2.get(Calendar.YEAR)
                && d1.get(Calendar.MONTH) == d2.get(Calendar.MONTH);
                
            case DAY:
                return d1.get(Calendar.YEAR) == d2.get(Calendar.YEAR)
                && d1.get(Calendar.MONTH) == d2.get(Calendar.MONTH)
                && d1.get(Calendar.DAY_OF_MONTH) == d2.get(Calendar.DAY_OF_MONTH);
        }
        
        assert false : "specificity did not match any known value";
        
        return false;
    }

}
