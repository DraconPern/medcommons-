/*
 * Created on Aug 15, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package net.medcommons.router.services.dicom.util;

import java.security.NoSuchAlgorithmException;



import net.medcommons.modules.crypto.GuidGenerator;

import org.apache.log4j.Logger;

/**
 * This is a bit of a kludge class. It contains utilities 
 * <ul>
 * <li> for reading a study object containing an array of series objects 
 * 		(which in turn have each have an array of instance objects) into 
 * 		memory. It's a kludge because Hibernate probably manages this 
 * 		better directly if the schema was better defined.
 * <li> Mappings between GUIDs and filenames. Later this will be moved
 * 		to a separate redirection/resolution layer; for now the system 
 * 		is very simple.
 * </ul>
 * 
 * Still - this class contains a wrapper which is useful when called
 * from a jsp. It can be changed later without changing the jsps.
 * 
 * @author sean
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class DICOMDataManager {

	private GuidGenerator guidGenerator = null;
	final static Logger log = Logger.getLogger(DICOMDataManager.class);

	private static DICOMDataManager ddm = null;
	/**
	 * 
	 */
	public DICOMDataManager() {
		super();
		try {
			guidGenerator = new GuidGenerator();
		} catch (NoSuchAlgorithmException e) {
			log.error(e);
		}
		ddm = this;

	}
	
	public static DICOMDataManager getDICOMDataManager(){
		if (ddm== null)
			ddm= new DICOMDataManager();
		return(ddm);
	}
	/**
	 * Placeholder mechanism for series GUID generation.
	 * 
	 * @param input
	 * @return
	 */
	private synchronized String generateSeriesGuid(byte[] input){
		
		String guid = null;
		try{
		
		guid = guidGenerator.generateGuid(input);
		}
		catch(Exception e){
			log.error(e);
			e.printStackTrace();
		}
		//String guid = DICOMDataManager.encodeBytes(hash,0,hash.length);
		return(guid);
	}

	
	/**
	* A nibble->char mapping for printing out bytes.
	*/
	private static final char[] digits =
		{
			'0',
			'1',
			'2',
			'3',
			'4',
			'5',
			'6',
			'7',
			'8',
			'9',
			'A',
			'B',
			'C',
			'D',
			'E',
			'F' };
	public static String encodeBytes(byte[] data, int offset, int length) {
		int size = 2 * length;
		char[] buf = new char[size];
		int low_mask = 0x0f;
		int high_mask = 0xf0;
		int buf_pos = 0;
		byte b;

		int j = 0;
		for (int i = offset; i < (offset + length); ++i) {
			b = data[i];
			buf[buf_pos++] = digits[(high_mask & b) >> 4];
			buf[buf_pos++] = digits[(low_mask & b)];
			++j;
		}

		return new String(buf);
	}


}

