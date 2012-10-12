/*
 * $Id: $
 */
package net.medcommons.router.services.dicom.util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Miscellaneous utils used for manipulating DICOM data
 * 
 * @author ssadedin
 */
public class DICOMUtils {
    public static Date parseDate(String dateString) {
        Calendar cal = new GregorianCalendar();
        // 4 chars for the year
        int year = Integer.parseInt(dateString.substring(0, 4));
        //log.info("Year is " + year);
        int month = Integer.parseInt(dateString.substring(4, 6));
        //log.info("Month is " + month);
        int day = Integer.parseInt(dateString.substring(6, 8));
        //log.info("Day is " + day);
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.DAY_OF_MONTH, day - 1);
        return cal.getTime();
    }
}