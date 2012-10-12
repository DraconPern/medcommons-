package net.medcommons.modules.utils;

import static net.medcommons.modules.utils.DateFormats.Specificity.DAY;
import static net.medcommons.modules.utils.DateFormats.Specificity.MONTH;
import static net.medcommons.modules.utils.DateFormats.Specificity.YEAR;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * Utilities for working with and parsing dates.
 * 
 * @author sdoyle
 * @author ssadedin
 */
public class DateFormats {

	public static final String EXACT_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	
    public enum Specificity {
        YEAR,MONTH,DAY
    }
    
    /**
     * Encapsulates a known date format along with the specificity of 
     * information it provides.
     * @author ssadedin
     */
    public static class Format {
        public Specificity specificity;
        public String format;
        
        public Calendar parse(String value) {
            SimpleDateFormat f = new SimpleDateFormat(format);
            try {
                f.parse(value);
                return f.getCalendar();
            }
            catch (ParseException e) {
                return null;
            }
        }
    }
    
    public static Format dateFormat(Specificity specificity, String format) {
        Format result = new Format();
        result.specificity = specificity;
        result.format = format;
        return result;
    }
    
    /**
     * All formats considered acceptable for parsing dates.  Note that not 
     * all of these are necessarily valid dates in CCRs (not sure).
     */
    static List<Format> ALL_FORMATS = Arrays.asList(
                                             dateFormat(DAY, EXACT_DATE_TIME_FORMAT),
                                             dateFormat(DAY,"yyyy-MM-dd"), 
                                             dateFormat(DAY,"MM/dd/yyyy"), 
                                             dateFormat(MONTH,"yyyy-MM"), 
                                             dateFormat(MONTH,"MM/yyyy"), 
                                             dateFormat(YEAR,"yyyy") 
                                             ); 
    /**
     * Return all the known formats that can be parsed.
     * @return
     */
    public static List<Format> all() {
        return new ArrayList<Format>(ALL_FORMATS);
    }
    
    /**
     * Attempt to parse the given value using the given formats
     * in the order they are listed in the fmts array.  
     * 
     * @param fmts
     * @param value
     * @return  the first value that can be parsed or null if 
     *           no value can be parsed
     */
    public static Calendar parse(List<Format> fmts, String value) {
        for(Format f : fmts) {
            Calendar c = f.parse(value);
            if(c != null)
                return c;
        }
        return null;
    }
}
