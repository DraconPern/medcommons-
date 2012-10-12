/*
 * $Id$
 * Created on 23/01/2007
 */
package net.medcommons.modules.services.interfaces;

import java.util.EnumSet;

/**
 * Enumeration defining Rights that are understood by the Rights system. 
 * 
 * @author ssadedin
 */
public enum Rights {
    
    /**
     * Allows reading of the associated content, or from the associated account
     */
    READ("R"),
    
    /**
     * Allows creation of new content in the associated account
     */
    WRITE("W");
    
    /**
     * Convenience definition of string that includes all rights
     */
    public static final String ALL = join(Rights.values(), "");
    
    private String value;
    
    Rights(String r) {
        this.value = r;
    }

    @Override
    public String toString() {
        return this.value;
    }
    
    /**
     * Parse given string and create set of rights corresponding to letters in
     * string. 
     */
    public static EnumSet<Rights> toSet(String requiredRights) {
        // Legacy hack
        if("ALL".equals(requiredRights)) {
            return EnumSet.allOf(Rights.class);
        }
        
        // Build set from string
        EnumSet<Rights> result = EnumSet.noneOf(Rights.class);
        int length = requiredRights.length();
        for(int i=0; i<length; ++i) {
            switch(requiredRights.charAt(i)) {
	            case 'R':
	                result.add(Rights.READ);
	                break;
	            case 'W':
	                result.add(Rights.WRITE);
	                break;
	            default:
            }
        }
        return result;
    }
    
    /**
     * Inverse of split() operation  
     * 
     * @param values
     * @param separator
     * @return
     */
    public static String join(Object [] values, String separator) {
        StringBuilder result = new StringBuilder();
        for (Object object : values) {
            if(result.length()>0)
                result.append(separator);
            result.append(object.toString());
        }
        return result.toString();
    }       
}
