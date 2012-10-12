/*
 * Created on May 11, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package net.medcommons.router.services.dicom.util;
import java.util.Comparator;
import java.util.Properties;
/**
 * @author sean
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 * 
 * Need to add min/max pixel value.
 */
public class MCInstance implements Comparator {
	public int InstanceNumber;
	public String SOPInstanceUID;
	public String ReferencedFileID;
	public String SeriesInstanceUID;
	public String StudyInstanceUID;
	public String level = ""; // Note: this might be a multivalued field.
	public String window = "";
	public int nFrames = Integer.MIN_VALUE;

	final static String SOPINSTANCEUID = "SOPInstanceUID";
	final static String INSTANCENUMBER = "InstanceNumber";
	final static String SERIESINSTANCEUID = "SeriesInstanceUID";
	final static String REFERENCEDFILEID = "ReferencedFileID";
	final static String STUDYINSTANCEUID = "StudyInstanceUID";
	final static String WINDOW = "Window";
	final static String LEVEL = "Level";
	final static String FRAMES = "Frames";

	public MCInstance() {

	}
	public MCInstance(Properties p) {
		this();

		SOPInstanceUID = p.getProperty(SOPINSTANCEUID, null);
		SeriesInstanceUID = p.getProperty(SERIESINSTANCEUID, null);
		try {
			InstanceNumber =
				Integer.parseInt(p.getProperty(INSTANCENUMBER, null));
		} catch (Exception e) {
			InstanceNumber = -1;
		}

		ReferencedFileID = p.getProperty(REFERENCEDFILEID, null);
		StudyInstanceUID = p.getProperty(STUDYINSTANCEUID, null);
		window = p.getProperty(WINDOW, "");
		level = p.getProperty(LEVEL, "");
		String sFrames = p.getProperty(FRAMES, "");
		try {

			if ((sFrames != null) && (!"".equals(sFrames)))
				nFrames = Integer.parseInt(sFrames);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void save(Properties p) {
		try {

			p.put(SOPINSTANCEUID, SOPInstanceUID);
			p.put(SERIESINSTANCEUID, SeriesInstanceUID);
			p.put(REFERENCEDFILEID, ReferencedFileID);
			p.put(INSTANCENUMBER, InstanceNumber + "");
			p.put(STUDYINSTANCEUID, StudyInstanceUID);
			if (level == null)
				level = "";
			p.put(LEVEL, level);
			if (window == null)
				window = "";
			p.put(WINDOW, window);

			if (nFrames != Integer.MIN_VALUE)
				p.put(FRAMES, "" + nFrames);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(this);
		}

	}
	public int compare(Object obj1, Object obj2) {
		MCInstance instance1 = (MCInstance) obj1;
		MCInstance instance2 = (MCInstance) obj2;
		if (instance1.InstanceNumber == instance2.InstanceNumber)
			return (0);
		else if (instance1.InstanceNumber > instance2.InstanceNumber)
			return (1);
		else
			return (-1);
	}
	public boolean equals(Object obj) {
		return (((MCInstance) obj).SOPInstanceUID.equals(SOPInstanceUID));
	}
	public String toString() {
		StringBuffer buff = new StringBuffer("Instance[");
		buff.append("\n\t InstanceNumber: ");
		buff.append(InstanceNumber);
		buff.append(",\n\t SOPInstanceUID: ");
		buff.append(SOPInstanceUID);
		buff.append(",\n\t ReferencedFileID: ");
		buff.append(ReferencedFileID);
		buff.append(",\n\t SeriesInstanceUID: ");
		buff.append(SeriesInstanceUID);
		buff.append(",\n\t StudyInstanceUID: ");
		buff.append(StudyInstanceUID);
		buff.append(",\n\t window: ");
		buff.append(window);
		buff.append(",\n\t level: ");
		buff.append(level);
		buff.append("  ]");

		return (buff.toString());
	}

}
