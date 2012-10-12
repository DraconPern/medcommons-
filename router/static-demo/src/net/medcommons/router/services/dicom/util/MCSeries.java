/*
 * Created on May 11, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package net.medcommons.router.services.dicom.util;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;
/**
 * @author sean
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class MCSeries implements Comparator {

	final static String SERIESINSTANCEUID = "SeriesInstanceUID";
	final static String SERIESNUMBER = "SeriesNumber";
	final static String MODALITY = "Modality";
	final static String SERIESDESCRIPTION = "SeriesDescription";
	final static String STUDYINSTANCEUID = "StudyInstanceUID";
	final static String SERIESDATE = "SeriesDate";
	final static String SERIESTIME = "SeriesTime";

	public int SeriesNumber;
	public String SeriesInstanceUID;
	public String Modality;
	public String SeriesDescription ;
	public String StudyInstanceUID;
	public String SeriesDate;
	public String SeriesTime;

	public ArrayList instances;

	public MCSeries() {

	}
	public MCSeries(Properties p) {
		this();

		SeriesInstanceUID = p.getProperty(SERIESINSTANCEUID, null);
		Modality = p.getProperty(MODALITY, null);
		SeriesDescription = p.getProperty(SERIESDESCRIPTION, null);
		StudyInstanceUID = p.getProperty(STUDYINSTANCEUID, null);
		SeriesDate = p.getProperty(SERIESDATE, null);
		SeriesTime = p.getProperty(SERIESTIME, null);
		try{
			SeriesNumber  = Integer.parseInt(p.getProperty(SERIESNUMBER, null));
		}
		catch(Exception e){
			SeriesNumber = -1;
		}
		instances = new ArrayList();
	}
	public void save(Properties p) {

		if (SeriesDescription == null)
			SeriesDescription = "Series" + SeriesNumber;
		p.put(SERIESINSTANCEUID, SeriesInstanceUID);
		p.put(MODALITY, Modality);
		p.put(SERIESDESCRIPTION, SeriesDescription);
		p.put(STUDYINSTANCEUID, StudyInstanceUID);
		if (SeriesDate == null)
			SeriesDate = "";
		p.put(SERIESDATE, SeriesDate);
		if (SeriesTime == null)
			SeriesTime = "";
		p.put(SERIESTIME, SeriesTime);
		p.put(SERIESNUMBER, Integer.toString(SeriesNumber));
	}
	public int compare(Object obj1, Object obj2) {
		MCSeries series1 = (MCSeries) obj1;
		MCSeries series2 = (MCSeries) obj2;
		if (series1.SeriesNumber == series2.SeriesNumber)
			return (0);
		else if (series1.SeriesNumber > series2.SeriesNumber)
			return (1);
		else
			return (-1);
	}
	public boolean equals(Object obj) {
		return (((MCSeries) obj).SeriesInstanceUID.equals(SeriesInstanceUID));
	}
    
  public int size() {
      return this.instances.size();
  }
  
  public Date getDate() {
      if((this.SeriesDate != null) && (this.SeriesDate.trim().length()>0))
          return DICOMUtils.parseDate(this.SeriesDate);
      else 
          return new Date();
    }  
}
