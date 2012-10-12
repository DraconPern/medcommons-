/*
 * $Id: StudyService.java 1672 2007-05-01 23:49:56Z sdoyle $
 */

package net.medcommons.router.services.study;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.mail.MessagingException;
import javax.mail.internet.MimeUtility;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.configuration.ConfigurationException;

import org.apache.log4j.Logger;



public class StudyService {
	private boolean initialized = false;
	private File rootDirectory = null;
	
	/**
	   * Logger to use with this class
	   */
	  private static Logger log = Logger.getLogger(StudyService.class);

	/**
	 * Returns the set of Study Guids known to the router. Returns null if there are
	 * no studies on the router.
	 * 
	 * @param selectionSpec
	 * @return
	 */
	public String[] selectStudyGuids(String selectionSpec) {
		//Session session = null;
		String[] guids = null;
		/*
		try {
			session = HibernateUtil.currentSession();

			List orders = session.find("from MCOrder");
			int nOrders = orders.size();
			if (nOrders > 0) {
				guids = new String[nOrders];
				for (int i = 0; i < nOrders; i++) {
					guids[i] = ((MCOrder) orders.get(i)).getOrderGuid();
				}
			}

		} catch (Exception e) {
			System.out.println("Error searching GUIDs");
			e.printStackTrace();

		} finally {
			try {

				HibernateUtil.closeSession();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
*/
		log.info("DEAD CODE");
		return guids;
	}

	/**
	 * Note: this is broken; studies no longer have GUIDs.
	 * Need to find out what calls this and replace it with orders.
	 *  Returns a String containing fields for identifying and describing a study.
	 * 
	 * Fields in the array are delimited by the '|' character.
	 * The order of the fields is:
	 * <ol>
	 * <li> MedCommons Study GUID
	 * <li> DICOM StudyInstanceUID
	 * <li> Patient Name
	 * <li> Patient ID
	 * <li> StudyDate
	 * <li> StudyTime
	 * 
	 * </ol>
	 * @param studyGuid
	 * @return
	 * @throws StudyServiceException
	 */
	public String retrieveStudyData(String studyGuid)
		throws StudyServiceException {
		System.out.println(studyGuid);
		String returnedValue = "";
		log.info("DEAD CODE");
		/*
		Session session = null;
		Transaction tx = null;
		try {
			session = HibernateUtil.currentSession();
			tx = session.beginTransaction();
			Criteria studyCriteria = session.createCriteria(MCStudy.class);
			EqExpression eqGuid = new EqExpression("mcGUID", studyGuid, true);
			studyCriteria.add(eqGuid);
			List studies = studyCriteria.list();
			if (studies.size() > 0) {

				MCStudy study = (MCStudy) studies.get(0);
				StringBuffer buff = new StringBuffer(study.getStudyInstanceUID());
				buff.append("|");
				buff.append(study.getStudyInstanceUID());
				buff.append("|");
				buff.append(study.getPatientName());
				buff.append("|");
				buff.append(study.getPatientID());
				buff.append("|");
				buff.append(study.getStudyDate());
				buff.append("|");
				buff.append(study.getStudyTime());
				buff.append("|");
				buff.append(study.getStudyDescription());
				returnedValue = buff.toString();
			}
			return (returnedValue);
			// Since these aren't binary - we don't need to encode -right?
			//return encodeBytes(filebytes);

		} catch (Exception e) {
			e.printStackTrace();
			throw new StudyServiceException(e.toString());
		}
		finally{
          try{HibernateUtil.closeSession();}
          catch(Exception e){e.printStackTrace();}
        }
        */
		return(returnedValue);
	}

	private String encodeBytes(byte[] data) throws IOException {
		try {
			ByteArrayOutputStream encodedByteStream =
				new ByteArrayOutputStream();
			OutputStream encoder =
				MimeUtility.encode(encodedByteStream, "base64");
			encoder.write(data);
			encoder.flush();

			return new String(encodedByteStream.toByteArray());

		} catch (MessagingException me) {
			throw new IOException("Cannot encode data.");
		}
	}
	private void init() throws ConfigurationException, Exception {
		String dest =
			(String) Configuration.getInstance().getConfiguredValue(
				"net.medcommons.dicom.directory");
		rootDirectory = new File(dest);
		initialized = true;
	}

	

}
