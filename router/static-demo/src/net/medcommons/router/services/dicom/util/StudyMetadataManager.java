/*
 * Created on May 11, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package net.medcommons.router.services.dicom.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import org.apache.log4j.Logger;
/**
 * @author sean
 *
 * Manages the study meta data files on disk.
 */
public class StudyMetadataManager {
	private static StudyMetadataManager manager = new StudyMetadataManager();
	private static File rootDirectory = null;
	final static Logger log = Logger.getLogger(StudyMetadataManager.class);
	ArrayList studyList = null;
	public static StudyMetadataManager getManager() {
		return (manager);
	}

	/*
	 * Returns all of the GUIDs known to the router (as of the last 
	 * scan()).
	 */
	public String[] guidQuery() {
		int nStudies = studyList.size();
		if (nStudies == 0)
			return null;

		String[] values = new String[nStudies];
		int counter = 0;
		Iterator iter = studyList.iterator();

		while (iter.hasNext()) {
			MCStudy study = (MCStudy) iter.next();
			values[counter++] = study.mcGUID;
		}
		return (values);
	}

	/**
	 * Scans the entire directory for study metadata objects.
	 * @param dir
	 * @throws IOException
	 */
	public void scan(File dir) throws IOException {
		try {

			rootDirectory = dir;
			studyMetaDataFilter filter = new studyMetaDataFilter();
			File studyMetaData[] = rootDirectory.listFiles(filter);
			studyList = new ArrayList();
			//System.out.println("There are " + studyMetaData.length + " studies on router");
			for (int i = 0; i < studyMetaData.length; i++) {

				log.info("Study Metadata file:" + studyMetaData[i].getName());
				Properties p = new Properties();
				if (studyMetaData[i].exists()) {
					FileInputStream in = new FileInputStream(studyMetaData[i]);
					p.load(in);
					MCStudy study = new MCStudy(p);
					studyList.add(study);
				} else
					log.error(
						"Study Meta Data file does not exist: "
							+ studyMetaData[i].getAbsolutePath());
			}

		} catch (NullPointerException e) {
			log.error("Exception scanning study directory:" + e);
			e.printStackTrace();
		}
	}

	public MCStudy getStudy(String guid) throws IOException {
		MCStudy study = null;
		File studyFolder = null;

		// Load the study from disk.
		FileInputStream in = null;
		try {

			File propertyFilename = new File(rootDirectory, guid);
			if (!propertyFilename.exists())
				throw new FileNotFoundException(
					propertyFilename.getAbsolutePath());
			in = new FileInputStream(propertyFilename);
			Properties p = new Properties();
			p.load(in);
			study = new MCStudy(p);
		} finally {
			//in.close();
			in = null;
		}
		studyFolder = studyFolder(study.StudyInstanceUID);

		seriesMetaDataFilter sfilter = new seriesMetaDataFilter();
		File seriesMetaData[] = studyFolder.listFiles(sfilter);
		ArrayList seriesList = new ArrayList();
		for (int i = 0; i < seriesMetaData.length; i++) {
			//log.info("Series Metadata file:" + seriesMetaData[i].getName());
			Properties p = new Properties();
			in = new FileInputStream(seriesMetaData[i]);
			p.load(in);
			MCSeries series = new MCSeries(p);
			seriesList.add(series);
		}
		study.series = seriesList;

		instanceMetaDataFilter ifilter = new instanceMetaDataFilter();
		File instanceMetaData[] = studyFolder.listFiles(ifilter);
		ArrayList instanceList = new ArrayList();
		for (int i = 0; i < instanceMetaData.length; i++) {
			//log.info("Instance Metadata file:" + instanceMetaData[i].getName());
			
			
			Properties p = new Properties();
			in = new FileInputStream(instanceMetaData[i]);
			p.load(in);
			MCInstance instance = new MCInstance(p);
			if (instance.SeriesInstanceUID == null)
				log.error("SeriesInstanceUID is null");
			instanceList.add(instance);
		}
		Iterator allInstances = instanceList.iterator();

		while (allInstances.hasNext()) {
			MCInstance anInstance = (MCInstance) allInstances.next();
			String seriesUID = anInstance.SeriesInstanceUID;
			MCSeries aSeries = getSeriesWithSeriesInstanceUID(study, seriesUID);
			if (aSeries != null)
				aSeries.instances.add(anInstance);
			else {
				log.error("Missing series with SeriesInstanceUID=" + seriesUID);
			}
		}
		//Object[] seriesArray = study.series.toArray();
		/*
		Collections.sort(study.series);
		Iterator allSeries = study.series.iterator();
		while(allSeries.hasNext()){
			MCSeries aSeries = (MCSeries) allSeries.next();
			Collections.sort(aSeries.instances);
		}
		*/

		return (study);
	}

	/*
	 * Returns the MCStudy associated with a given StudyInstanceUID
	 * or null if no such study exists.
	 */
	public MCStudy getStudyWithStudyInstanceUID(String studyUID) {
		MCStudy study = null;
		Iterator iter = studyList.iterator();

		while (iter.hasNext()) {
			MCStudy candidateStudy = (MCStudy) iter.next();
			if (candidateStudy.StudyInstanceUID.equals(studyUID)) {
				study = candidateStudy;
				break;
			}
		}
		return (study);
	}

	/*
	 * Returns the MCStudy associated with a given guid
	 * or null if no such study exists.
	 */
	public MCStudy getStudyWithGUID(String guid) {
		MCStudy study = null;
		Iterator iter = studyList.iterator();

		while (iter.hasNext()) {
			MCStudy candidateStudy = (MCStudy) iter.next();
			if (candidateStudy.mcGUID.equals(guid)) {
				study = candidateStudy;
				break;
			}

		}
		return (study);
	}
	/**
	 * Returns MCSeries object within study or null if it does not exist.
	 * @param study
	 * @param seriesUID
	 * @return
	 */
	public MCSeries getSeriesWithSeriesInstanceUID(
		MCStudy study,
		String seriesUID) {
		MCSeries series = null;
		Iterator iter = study.series.iterator();

		while (iter.hasNext()) {
			MCSeries candidateSeries = (MCSeries) iter.next();
			if (candidateSeries.SeriesInstanceUID != null) {

				if (candidateSeries.SeriesInstanceUID.equals(seriesUID)) {
					series = candidateSeries;
					break;
				}
			} else {
				log.error("SeriesInstanceUID is null for " + candidateSeries);
			}

		}
		return (series);
	}

	/*
	 * Called to add a new study to the in-memory list. 
	 * Updates study on disk as a side-effect.
	 */
	public void newStudy(MCStudy study) throws IOException {
		// Sanity check
		if (getStudyWithStudyInstanceUID(study.StudyInstanceUID) == null) {
			studyList.add(study);
			updateStudy(study);
		}

	}
	private File studyFolder(String studyUID) {
		return (new File(rootDirectory, studyUID));
	}
	private File seriesFile(File studyFolder, String seriesUID) {
		return (new File(studyFolder, seriesUID + ".series"));
	}
	private File instanceFile(File studyFolder, String instanceUID) {
		return (new File(studyFolder, instanceUID + ".instance"));
	}

	public void newSeries(MCSeries series) {

	}
	/**
	 * Returns true if the series meta data file exists.
	 * @param studyUID
	 * @param seriesUID
	 * @return
	 */
	public boolean seriesMetadataExists(String studyUID, String seriesUID) {
		boolean exists = false;
		File studyDirectory = studyFolder(studyUID);
		if (studyDirectory.exists()) {
			File seriesFile = seriesFile(studyDirectory, seriesUID);
			if (seriesFile.exists())
				exists = true;
		}
		return (exists);
	}
	/**
	 * Returns true if the instance metadata file exists.
	 */
	public boolean instanceMetadataExists(
		String studyUID,
		String instanceUID) {
		boolean exists = false;
		File studyDirectory = studyFolder(studyUID);
		if (studyDirectory.exists()) {
			File instanceFile = instanceFile(studyDirectory, instanceUID);
			if (instanceFile.exists())
				exists = true;
		}
		return (exists);
	}
	/**
	 * A bit wasteful. Overwrites study file each time.
	 * Note that there is no synchronization.
	 * @param study
	 * @throws IOException
	 */
	public void updateStudy(MCStudy study) throws IOException {

		File propertyFilename = new File(rootDirectory, study.mcGUID);
		FileOutputStream out = new FileOutputStream(propertyFilename);
		FileInputStream in = new FileInputStream(propertyFilename);
		Properties p = new Properties();
		try {
			p.load(in);
			study.save(p);
			p.save(out, study.StudyInstanceUID);
		} finally {
			out.close();
			in.close();
		}

	}

	public void updateSeries(MCSeries series) throws IOException {
		File studyFolder = studyFolder(series.StudyInstanceUID);
		if (!studyFolder.exists())
			studyFolder.mkdir();
		File seriesFile = seriesFile(studyFolder, series.SeriesInstanceUID);

		FileOutputStream out = new FileOutputStream(seriesFile);
		FileInputStream in = new FileInputStream(seriesFile);
		Properties p = new Properties();
		try {
			p.load(in);
			series.save(p);
			p.save(out, series.SeriesInstanceUID);
		} finally {
			out.close();
			in.close();
		}

	}
	public void updateInstance(MCInstance instance) throws IOException {
		File studyFolder = studyFolder(instance.StudyInstanceUID);
		if (!studyFolder.exists())
			studyFolder.mkdir();
		File instanceFile = instanceFile(studyFolder, instance.SOPInstanceUID);

		FileOutputStream out = new FileOutputStream(instanceFile);
		FileInputStream in = new FileInputStream(instanceFile);
		Properties p = new Properties();
		try {
			p.load(in);
			instance.save(p);
			p.save(out, instance.SOPInstanceUID);
		} finally {
			out.close();
			in.close();
		}

	}
	class studyMetaDataFilter implements FileFilter {
		public boolean accept(File pathname) {
			return (!pathname.isDirectory());
		}
	}
	class seriesMetaDataFilter implements FileFilter {
		public boolean accept(File pathname) {
			boolean matches = false;
			String name = pathname.getName();
			if (name.indexOf(".series") > 0)
				matches = true;
			return (matches);
		}
	}
	class instanceMetaDataFilter implements FileFilter {
		public boolean accept(File pathname) {
			boolean matches = false;
			String name = pathname.getName();
			if (name.indexOf(".instance") > 0)
				matches = true;
			return (matches);
		}
	}

}
