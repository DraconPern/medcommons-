/*
 * $Id: StudyService.java 98 2004-05-12 18:27:46Z sdoyle $
 */

package net.medcommons.router.services.study;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.mail.MessagingException;
import javax.mail.internet.MimeUtility;

import net.medcommons.router.configuration.Configuration;
import net.medcommons.router.configuration.ConfigurationException;
import net.medcommons.router.services.dicom.util.MCStudy;
import net.medcommons.router.services.dicom.util.StudyMetadataManager;

public class StudyService {
	private StudyMetadataManager studyMetadata = new StudyMetadataManager();
	private boolean initialized = false;
	private File rootDirectory = null;

	/**
	 * Returns the set of Study Guids known to the router. Returns null if there are
	 * no studies on the router.
	 * 
	 * @param selectionSpec
	 * @return
	 */
	public String[] selectStudyGuids(String selectionSpec) {
		updateStudyMetadata();

		String[] guids = studyMetadata.guidQuery();

		return guids;
	}

	/**
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
		try {
			updateStudyMetadata();
			MCStudy study = studyMetadata.getStudyWithGUID(studyGuid);
			StringBuffer buff = new StringBuffer(study.mcGUID);
			buff.append("|");
			buff.append(study.StudyInstanceUID);
			buff.append("|");
			buff.append(study.PatientName);
			buff.append("|");
			buff.append(study.PatientID);
			buff.append("|");
			buff.append(study.StudyDate);
			buff.append("|");
			buff.append(study.StudyTime);
			buff.append("|");
			buff.append(study.StudyDescription);
			returnedValue = buff.toString();
			return (returnedValue);
			// Since these aren't binary - we don't need to encode -right?
			//return encodeBytes(filebytes);

		} catch (Exception e) {
			e.printStackTrace();
			throw new StudyServiceException(e.toString());
		}
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

	/**
	 * Refreshes the cache of study data from disk. 
	 *
	 */
	private void updateStudyMetadata() {
		try {

			if (!initialized)
				init();
			studyMetadata.scan(rootDirectory);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
