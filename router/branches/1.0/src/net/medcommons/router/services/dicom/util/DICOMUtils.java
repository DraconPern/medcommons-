/*
 * $Id: $
 */
package net.medcommons.router.services.dicom.util;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.configuration.ConfigurationException;
import net.medcommons.modules.crypto.SHA1;
import net.medcommons.modules.utils.DateFormats;
import net.medcommons.storage.FileUtils;

import org.apache.log4j.Logger;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.StopTagInputHandler;

/**
 * Miscellaneous utils used for manipulating DICOM data
 * 
 * 
 * @author ssadedin
 */
public class DICOMUtils {
	
	  final static Logger log = Logger.getLogger(DICOMUtils.class);
  /**
   * A nibble->char mapping for printing out bytes. Hacked. Replaced the A-F
   * with 1-6
   */
  private static final char[] digits = { '0', '1', '2', '3', '4', '5', '6',
        '7', '8', '9', '1', '2', '3', '4', '5', '6' };        
    
  private static final char[] hex_digits = { '0', '1', '2', '3', '4', '5', '6',
        '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };        
  
  
  
  private static int[] fileIDTags;
  
	public static void init() throws ConfigurationException, Exception {
		String fsFileID =
			(String) Configuration.getInstance().getConfiguredValue(
				"net.medcommons.dicom.fs-file-id");

		fileIDTags = toTags(ConfigurationTokenizer.tokenize(fsFileID));
	}

	public static Date parseDate(String dateString) {
		try{
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
		catch(Exception e){
			try{
				Date d = parseFormattedDate(dateString);
				return(d);
			}
			catch(ParseException e2){
				log.error("Can't parse date - kludge with today's date '" + dateString + "'");
				
				return new Date(System.currentTimeMillis());
			}
		}
		
	}
	
	 
	  
	private static Date parseFormattedDate(String dateString) throws ParseException{
    	if (dateString == null) throw new NullPointerException("Null dateString");
    	DateFormat df = new SimpleDateFormat(DateFormats.EXACT_DATE_TIME_FORMAT);
    	 df.setTimeZone(TimeZone.getTimeZone("GMT"));
    	 return(df.parse(dateString));
    }

  /**
   * Formats the given date/time in a manner consistent with both DICOM
   * and the CCR 1.0 Spec.
   * 
   * @param dateTimeMs
   * @return
   */
  public static String formatDate(long dateTimeMs) {
      SimpleDateFormat zulu = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
      return zulu.format(new Date(dateTimeMs));
  }
    
  public static String formatDate(Date d){
	  SimpleDateFormat zulu = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
      return zulu.format(d);
  }
  /**
   * Returns a Java Date object for a Zulu-date format string.
   * <P>
   * Example from CCR: 2003-12-09T05:00:00Z
   * @param zuluDate
   * @return
   */
	  public static Date parseDateZulu(String zuluDate) throws ParseException {
		SimpleDateFormat zulu = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		zulu.setTimeZone(TimeZone.getTimeZone("GMT"));
		zulu.setLenient(true);
		Date d = zulu.parse(zuluDate);
		return (d);
	}
	  
	  
	  /**
	   * Returns a DICOM image object parsed from the given file
	   */
	  public static DicomObject getDICOMObject(File f) {
	      DicomObject dcmObj = new BasicDicomObject();
	      try {
	          DicomInputStream in = new DicomInputStream(f);
	          try {
	              in.setHandler(new StopTagInputHandler(Tag.StudyDate));
	              in.readDicomObject(dcmObj, 1024);
	              
	              // Attempt to get some values which will fail if the file isn't
	              // a valid DICOM file.
	              String transferSytnax = in.getTransferSyntax().uid();
	              long pos = in.getEndOfFileMetaInfoPosition();
	          } 
	          finally {
	              try { in.close(); } catch (IOException ignore) { }
	          }
	      } 
	      catch (IOException e) {
	          log.debug("Failed to parse - Not a DICOM file " + f + " - skipped.");
	          
	          return null;
	      }
	      
	      String sopClassUID = dcmObj.getString(Tag.SOPClassUID);
	      if (sopClassUID == null) {
	          log.debug("WARNING: Missing SOP Class UID in " + f + " - skipped.");
	          return null;
	      }
	      
	      String sopInstanceUID = dcmObj.getString(Tag.SOPInstanceUID);
	      if (sopInstanceUID == null) {
	          log.debug("Missing SOP Instance UID in " + f + " - skipped.");
	          return null;
	      }
	      return dcmObj;
	  }
	  
	/**
	 * Creates a file for image data whose path is composed of file id tags
	 * (different portions of the image meta-data) with the series GUID at the
	 * root.
	 * 
	 * The tags used for the file system are defined in the config.xml
	 * configuration file.
	 * 
	 * @see Configuration
	 */
	public static File toFile(File rootDir, String guidDirName, DicomObject ds) {
		File file = FileUtils.resolveGUIDAddress(rootDir, guidDirName);
		String imageFilename = imageFileSegment(ds);
		file = new File(file, imageFilename);
		return file;
	}
	/**
	 * Makes any file into a 'fixed content' one. In this context this simply means
	 * that the file's name is now the SHA-1 hash of the file's contents.
	 * <P>
	 * In the future we might calculate the SHA-1 hash on the bytes in memory first; this may
	 * mean changes to the underlying dcm4che code.
	 * @param imageFile
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	
	public static File createFileAsFixedContent(File imageFile) throws NoSuchAlgorithmException, IOException{
		SHA1 sha1 = new SHA1();
    	sha1.initializeHashStreamCalculation();
		String newName = sha1.calculateFileHash(imageFile);
		File newFile = new File(imageFile.getParent(), newName);
		
		boolean success = imageFile.renameTo(newFile);
		if (!success) // Failure typically occurs if the file is locked by another thread.
			throw new IOException("Unable to rename file " + imageFile.getAbsolutePath() + " to " +
					newFile.getAbsolutePath());
		return(newFile);
		
	}
	public static String imageFileSegment(DicomObject ds) {
		StringBuffer buff = new StringBuffer();
		for (int i = 0; i < fileIDTags.length; i++) {
			if (i > 0)
				buff.append("/");
			buff.append(toFileID(ds, fileIDTags[i]));

		}
		return (buff.toString());
	}
	
	private static int[] toTags(String[] names) {
		int[] retval = new int[names.length];
		for (int i = 0; i < names.length; ++i) {
			retval[i] = Tag.forName(names[i]);
		}
		return retval;
	}
	/**
	 * Maps DICOM identifiers to file ID segments.
	 * 
	 * All non-numeric values changed to '_'.
	 * All values padded to fit 8 characters with leading zeros
	 * (this means that the IDs sort nicely where there are different
	 * numbers of digits in different file segments).
	 * 
	 * Truncate segments to 8 bytes except in case
	 * of StudyInstanceUID.
	 * @param ds
	 * @param tag
	 * @return String containing file name segment. 
	 */
	private static String toFileID(DicomObject ds, int tag) {
		String s = ds.getString(tag);
		if (s == null || s.length() == 0)
			return "__NULL__";

		if (tag == Tag.StudyInstanceUID)
			return (s);
		char[] in = s.toUpperCase().toCharArray();

		int n = Math.min(8, in.length);

		char[] out = new char[8]; // Max length permitted in DICOMDIR

		for (int i = 0; i < out.length; i++)
			out[i] = '0';
		//char[] out = new char[Math.min(8, in.length)];
		for (int i = 0, j = 8 - n; i < n; ++i, ++j) {
			out[j] =
				in[i] >= '0'
					&& in[i] <= '9'
					|| in[i] >= 'A'
					&& in[i] <= 'Z' ? in[i] : '_';
		}
		return new String(out);
	}
  
  /**
   * Encodes the given bytes into a numeric-only base-16 form.  This is done
   * by encoding as to hex but with A-F replaced by 0-6.  
   * <p/>
   * Do not use this method to hex encode bytes.  See hexEncodeBytes() for this 
   * purpose.
   * 
   * @return
   */
  public static String encodeBytes(byte[] data, int offset, int length) {
    char[] buf = encodeBytes(data, offset, length, digits);
    return new String(buf);
  }

  /**
   * Encodes the given bytes into a base 16 string based on the given
   * digits.
   * 
   * @param data
   * @param offset
   * @param length
   * @param code_digits
   * @return
   */
  private static char[] encodeBytes(byte[] data, int offset, int length, char[] code_digits) {
      int size = 2 * length;
      char[] buf = new char[size];
      int low_mask = 0x0f;
      int high_mask = 0xf0;
      int buf_pos = 0;
      byte b;
      
      int j = 0;
      for (int i = offset; i < (offset + length); ++i) {
          b = data[i];
          buf[buf_pos++] = code_digits[(high_mask & b) >> 4];
          buf[buf_pos++] = code_digits[(low_mask & b)];
          ++j;
      }
      return buf;
  }      
  
  /**
   * Encodes the given bytes as a hex string
   * @param data
   * @param offset
   * @param length
   * @return
   */
  public static String hexEncodeBytes(byte[] data, int offset, int length) {
    char[] buf = encodeBytes(data, offset, length, hex_digits);
    return new String(buf);
  }
  
  
 }