/*
 * Created on Apr 21, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG                                  *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 *                                                                           *
 *****************************************************************************/

package net.medcommons.router.services.dicom.util;

/**
 * @author sean
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import net.medcommons.router.configuration.Configuration;
import net.medcommons.router.configuration.ConfigurationException;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.media.DirBuilder;
import org.dcm4che.media.DirBuilderFactory;
import org.dcm4che.media.DirBuilderPref;
import org.dcm4che.media.DirWriter;
import org.dcm4che.net.Association;
import org.dcm4che.net.AssociationListener;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.PDU;
import org.dcm4che.server.DcmHandler;
import org.dcm4che.util.UIDGenerator;

/**
 * CSTORE FSU (File Set Updater). 
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision: 1.2 $ $Date: 2002/11/17 16:42:41 $
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>yyyymmdd author:</b>
 * <ul>
 * <li> explicit fix description (no line numbers but methods) go 
 *            beyond the cvs commit message
 * </ul>
 */
public class CStoreFSU implements AssociationListener, Runnable {
	// Constants -----------------------------------------------------
	static final Logger log = Logger.getLogger("CStoreSCP");

	// Attributes ----------------------------------------------------
	private static final Random RND = new Random();
	private static final DirBuilderFactory dirFact =
		DirBuilderFactory.getInstance();
	private static final DcmObjectFactory objFact =
		DcmObjectFactory.getInstance();

	private final DirBuilderPref dirPref = dirFact.newDirBuilderPref();
	private boolean autocommit;
	private File dicomdir;
	private File dir;
	private final int[] fileIDTags;
	private final String fsid;
	private final String fsuid;
	private DirWriter writer = null;
	private DirBuilder builder = null;
	private final LinkedList queue = new LinkedList();
	private DcmHandler handler;
	private boolean running = false;

	/**
	 * CSTORE File Set Updater
	 * 
	 * Handles DICOM CSTORE events within the current association.
	 * Each image is written to disk.
	 * 
	 * @param rootDirectory
	 * @param handler
	 * @throws ConfigurationException
	 * @throws Exception
	 */
	public CStoreFSU(File rootDirectory, DcmHandler handler)
		throws ConfigurationException, Exception {

		this.dir = rootDirectory;

		//this.dicomdir = new File(this.dir, "DICOMDIR");
		String fsFileID =
			(String) Configuration.getInstance().getConfiguredValue(
				"net.medcommons.dicom.fs-file-id");

		fileIDTags = toTags(ConfigurationTokenizer.tokenize(fsFileID));

		fsid =
			(String) Configuration.getInstance().getConfiguredValue(
				"net.medcommons.dicom.fs-id");
		fsuid =
			(String) Configuration.getInstance().getConfiguredValue(
				"net.medcommons.dicom.fs-uid");
		String fsLazyUpdate =
			(String) Configuration.getInstance().getConfiguredValue(
				"net.medcommons.dicom.fs-lazy-update");

		autocommit =true;// !"<yes>".equals(fsLazyUpdate);
		initDirBuilderPref();
		handler.addAssociationListener(this);
	}

	private static int[] toTags(String[] names) {
		int[] retval = new int[names.length];
		for (int i = 0; i < names.length; ++i) {
			retval[i] = Tags.forName(names[i]);
		}
		return retval;
	}
	
	private void initDirBuilderPref() throws Exception {
		HashMap map = new HashMap();
		Set keys = Configuration.getInstance().keys();
		for (Iterator it = keys.iterator(); it.hasNext();) {
			String element = (String) it.next();
			addDirBuilderPrefElem(map, element);
		}
		for (Iterator it = map.entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			dirPref.setFilterForRecordType(
				(String) entry.getKey(),
				(Dataset) entry.getValue());
		}
	}

	private void addDirBuilderPrefElem(HashMap map, String key) {
		String configNamespace = "net.medcommons.services.dicom.dir.";
		if (!key.startsWith(configNamespace)){
			return;
		}
		
		int pos2 = key.lastIndexOf('.');

		String type = key.substring(configNamespace.length(), pos2).replace('_', ' ');
		Dataset ds = (Dataset) map.get(type);
		if (ds == null) {
			map.put(type, ds = objFact.newDataset());
		}
		ds.putXX(Tags.forName(key.substring(pos2 + 1)));
	}

	// Public --------------------------------------------------------
	/**
	 * Creates a file for image data whose path is composed
	 * of file id tags (different portions of the image meta-data)
	 * 
	 * The tags used for the file system are defined in the config.xml
	 * configuration file.
	 * 
	 * @see Configuration
	 */
	public File toFile(Dataset ds) {
		File file = dir;
		for (int i = 0; i < fileIDTags.length; ++i) {
			file = new File(file, toFileID(ds, fileIDTags[i]));
		}
		File parent = file.getParentFile();
		
		return file;
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
	private String toFileID(Dataset ds, int tag) {
		String s = ds.getString(tag);
		if (s == null || s.length() == 0)
			return "__NULL__";

		if (tag == Tags.StudyInstanceUID)
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

	public void schedule(final File file, final Dataset ds) {

		synchronized (queue) {
			queue.addLast(new Runnable() {
				public void run() {
					try {
						update(file, ds);
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
				}
			});
			queue.notify();
		}
	}
	
	/**
	 * Processes each file update job entered by the schedule method.
	 */
	public void run() {
		try {
			running = true;
			while (running) {
				getJob().run();
			}
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		} finally {
			// Code not reached (yet) 
			if (handler == null)
				log.info("++++++++ HANDLER IS NULL IN CStoreFSU.close()");
			else
				handler.removeAssociationListener(this);
			builder = null;

		}
	}

	// Y overrides ---------------------------------------------------
	public void write(Association src, PDU pdu) {
	}

	public void received(Association src, PDU pdu) {
	}

	public void write(Association src, Dimse dimse) {
	}

	public void received(Association src, Dimse dimse) {
	}

	public void error(Association src, IOException ioe) {
	}

	/**
	 * Invoked when the DICOM association is closed.
	 * 
	 * This causes the run() method to exit; no more
	 * images will be processed in this association.
	 */
	public void close(Association src) {
		if (writer != null && !autocommit) {
			try {
				writer.commit();
				log.info("CSTORE Association Closed: M-WRITE " + dicomdir);
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
		try {
			// close the DICOMDIR file
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			running = false;
		}
	}


	private Runnable getJob() throws InterruptedException {
		synchronized (queue) {
			while (queue.isEmpty()) {
				queue.wait();
			}
			return (Runnable) queue.removeFirst();
		}
	}

	private synchronized void update(File file, Dataset ds)
		throws IOException {
		File imageFolder = file.getParentFile();
		File seriesFolder = imageFolder.getParentFile();
		File studyFolder = seriesFolder.getParentFile();
		this.dicomdir = new File(studyFolder, "DICOMDIR");
		initBuilder();
		builder.addFileRef(writer.toFileIDs(file), ds);

		writer.commit();
		//log.info("update: M-WRITE " + dicomdir);
		
	}

	private void initBuilder() throws IOException {

		if (dicomdir.exists()) {
			if (builder != null)
				return;
			writer = dirFact.newDirWriter(dicomdir, null);
		} else {

			String uid =
				fsuid.length() != 0
					? fsuid
					: UIDGenerator.getInstance().createUID();
			writer =
				dirFact.newDirWriter(dicomdir, uid, fsid, null, null, null);
		}
		builder = dirFact.newDirBuilder(writer, dirPref);
	}

	// Inner classes -------------------------------------------------
}
