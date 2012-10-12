/*
 * Created on Apr 20, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 * 
 * Original copyright below
 */
/*                                                                           *
	 *  Copyright (c) 2002, 2003 by TIANI MEDGRAPH AG                            *
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
	 */

package net.medcommons.router.services.dicom;

//import gnu.getopt.LongOpt;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Set;

import net.medcommons.router.configuration.Configuration;
import net.medcommons.router.configuration.ConfigurationException;
import net.medcommons.router.services.dicom.util.CStoreFSU;
import net.medcommons.router.services.dicom.util.ConfigurationTokenizer;
import net.medcommons.router.services.dicom.util.GetStudyGUID;
import net.medcommons.router.services.dicom.util.MCInstance;
import net.medcommons.router.services.dicom.util.MCSeries;
import net.medcommons.router.services.dicom.util.MCStudy;
import net.medcommons.router.services.dicom.util.StudyMetadataManager;

import org.apache.log4j.Logger;
import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.DcmParser;
import org.dcm4che.data.DcmParserFactory;
import org.dcm4che.data.FileMetaInfo;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.AssociationFactory;
import org.dcm4che.net.DcmServiceBase;
import org.dcm4che.net.DcmServiceRegistry;
import org.dcm4che.net.Dimse;
import org.dcm4che.server.DcmHandler;
import org.dcm4che.server.Server;
import org.dcm4che.server.ServerFactory;
import org.dcm4che.util.DcmProtocol;
import org.dcm4che.util.SSLContextAdapter;
/**
 * @author sean
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class CStoreSCP extends DcmServiceBase {

	//private Logger log = Logger.getLogger(CStoreSCP.class);

	/**
	 * <description>
	 *
	 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
	 * @created    May, 2002
	 * @version    $Revision: 1.12 $ $Date: 2003/06/22 22:33:02 $
	 */

	// Constants -----------------------------------------------------
	final static Logger log = Logger.getLogger(CStoreSCP.class);

	// Attributes ----------------------------------------------------
	//private static ResourceBundle messages = 
	//	ResourceBundle.getBundle("CStoreSCP", Locale.getDefault());

	private final static ServerFactory srvFact = ServerFactory.getInstance();
	private final static AssociationFactory fact =
		AssociationFactory.getInstance();
	private final static DcmParserFactory pFact =
		DcmParserFactory.getInstance();
	private final static DcmObjectFactory oFact =
		DcmObjectFactory.getInstance();

	private SSLContextAdapter tls = null;
	private DcmProtocol protocol = DcmProtocol.DICOM;

	private Dataset overwrite = oFact.newDataset();
	private AcceptorPolicy policy = fact.newAcceptorPolicy();
	private DcmServiceRegistry services = fact.newDcmServiceRegistry();
	private DcmHandler handler = srvFact.newDcmHandler(policy, services);
	private Server server = srvFact.newServer(handler);
	private int bufferSize = 512;
	private File dir = null;
	private CStoreFSU fsu = null;
	private long rspDelay = 0L;
	private static StudyMetadataManager studyMetadata =
		new StudyMetadataManager();
	private static GetStudyGUID getStudyGUID = new GetStudyGUID();
	public CStoreSCP() throws Exception {

		try {

			String rspdelay =
				(String) Configuration.getInstance().getConfiguredValue(
					"net.medcommons.services.dicom.rsp-delay");
			String buflen =
				(String) Configuration.getInstance().getConfiguredValue(
					"net.medcommons.services.dicom.buf-len");

			rspDelay = Integer.parseInt(rspdelay) * 1000L;
			bufferSize = Integer.parseInt(buflen) & 0xfffffffe;
			initServer();
			initDest();
			initStudyData();
			initTLS();
			initPolicy();

			//initOverwrite();
		} catch (Exception e) {
			// Kludge: Need policy about startup failures & what to do
			// See Mantis #97
			log.error(e);
			e.printStackTrace();
			throw e;
		}

	}

	/**
	 *  Starts the server.
	 *
	 * @exception  IOException               Description of the Exception
	 */
	public void start() throws IOException {
		if (fsu != null) {
			new Thread(fsu).start();
		}
		server.start();
		/*//test code for stopping and starting server again 
		System.out.println("STOPPING");
		server.stop();
		System.out.println("STARTING");
		try{Thread.sleep(2500);}catch(Exception e){}
		server.start();
		new Socket(InetAddress.getLocalHost(), 4000);*/
	}
	/**
	 * Stops the server.
	 *
	 */
	public void stop() {
		server.stop();
		fsu = null;
	}

	// DcmServiceBase overrides --------------------------------------
	/**
	 * Processes the CSTORE of a DICOM instance within the specified
	 * association.
	 *
	 * @param  assoc            DICOm Association
	 * @param  rq               DICOM request
	 * @param  rspCmd           DICOM response
	 * @exception  IOException  Description of the Exception
	 */
	protected void doCStore(ActiveAssociation assoc, Dimse rq, Command rspCmd)
		throws IOException {
		InputStream in = rq.getDataAsStream();
		try {

			if (this.fsu == null) {
				this.fsu = new CStoreFSU(dir, handler);
				new Thread(this.fsu).start();

			}

			if (dir != null) {
				Command rqCmd = rq.getCommand();
				FileMetaInfo fmi =
					objFact.newFileMetaInfo(
						rqCmd.getAffectedSOPClassUID(),
						rqCmd.getAffectedSOPInstanceUID(),
						rq.getTransferSyntaxUID());

				storeToFileset(in, fmi);
			}
		} catch (IOException ioe) {
			// ??
			ioe.printStackTrace();

		} catch (Exception e) {
			// ??
			e.printStackTrace();
		} finally {
			in.close();
		}
		if (rspDelay > 0L) {
			try {
				Thread.sleep(rspDelay);
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		}
		rspCmd.putUS(Tags.Status, Status.Success);
	}

	private OutputStream openOutputStream(File file) throws IOException {
		File parent = file.getParentFile();
		if (!parent.exists()) {
			if (!parent.mkdirs()) {
				throw new IOException("Could not create " + parent);
			}
			//log.info("M-WRITE " + parent);
		}
		//log.info("M-WRITE " + file);
		return new BufferedOutputStream(new FileOutputStream(file));
	}

	/**
	 * Stores images to a directory; creates DICOMDIR metadata.
	 * @param in
	 * @param fmi
	 * @throws IOException
	 */
	private void storeToFileset(InputStream in, FileMetaInfo fmi)
		throws IOException {
		MCStudy study = null;
		boolean newStudy = true;
		Dataset ds = oFact.newDataset();
		DcmParser parser = pFact.newDcmParser(in);
		parser.setDcmHandler(ds.getDcmHandler());
		DcmDecodeParam decParam =
			DcmDecodeParam.valueOf(fmi.getTransferSyntaxUID());

		parser.parseDataset(decParam, Tags.PixelData);
		String studyUID = ds.getString(Tags.StudyInstanceUID);

		study = studyMetadata.getStudyWithStudyInstanceUID(studyUID);
		if (study == null) {
			study = new MCStudy();
			study.StudyInstanceUID = studyUID;
			study.mcGUID = "abcde" + System.currentTimeMillis();//getStudyGUID.getNewGuid();
		} else
			newStudy = false;
		study.PatientName = ds.getString(Tags.PatientName);
		study.PatientID = ds.getString(Tags.PatientID);
		study.StudyInstanceUID = ds.getString(Tags.StudyInstanceUID);
		study.StudyDate = ds.getString(Tags.StudyDate);
		study.StudyTime = ds.getString(Tags.StudyTime);
		study.StudyDescription = ds.getString(Tags.StudyDescription);
		if (newStudy)
			studyMetadata.newStudy(study);
		else
			studyMetadata.updateStudy(study);

		String seriesInstanceUID = ds.getString(Tags.SeriesInstanceUID);
		String sopInstanceUID = ds.getString(Tags.SOPInstanceUID);

		if (!studyMetadata
			.seriesMetadataExists(study.StudyInstanceUID, seriesInstanceUID)) {
			log.info("Creating new series metadata object");
			MCSeries series = new MCSeries();
			series.Modality = ds.getString(Tags.Modality);
			series.SeriesDescription = ds.getString(Tags.SeriesDescription);
			series.SeriesInstanceUID = seriesInstanceUID;
			series.StudyInstanceUID = study.StudyInstanceUID;
			series.SeriesDate = ds.getString(Tags.SeriesDate);
			series.SeriesTime = ds.getString(Tags.SeriesTime);
			try {
				series.SeriesNumber =
					Integer.parseInt(ds.getString(Tags.SeriesNumber));
			} catch (Exception e) {
				e.printStackTrace();
				series.SeriesNumber = -1;
			}
			studyMetadata.updateSeries(series);

		}
		if (!studyMetadata
			.instanceMetadataExists(study.StudyInstanceUID, sopInstanceUID)) {
			log.info("Creating new instance metadata object");
			MCInstance instance = new MCInstance();
			instance.SOPInstanceUID = sopInstanceUID;
			instance.SeriesInstanceUID = seriesInstanceUID;
			instance.StudyInstanceUID = study.StudyInstanceUID;

			// Replace \ characters with /. This is
			// a no-op in Unix; in Windows the \ characters
			// cause havoc down the HTML/JavaScript path. Placing
			// these filenames in Unix conventions removes all 
			// need for escape sequences.
			String instanceFilename =
				fsu.toFile(ds).toString().replace('\\', '/');
			int pos = instanceFilename.length();
			for (int i = 0; i < 3; i++) {
				pos = instanceFilename.lastIndexOf("/", pos - 1);
			}

			instance.ReferencedFileID = instanceFilename.substring(pos + 1);
			instance.window = ds.getString(Tags.WindowWidth);
			instance.level = ds.getString(Tags.WindowCenter);
			String sFrame = ds.getString(Tags.NumberOfFrames);

			try {
				instance.InstanceNumber =
					Integer.parseInt(ds.getString(Tags.InstanceNumber));
				if ((sFrame != null) && (!"".equals(sFrame)))
					instance.nFrames = Integer.parseInt(sFrame);
			} catch (Exception e) {
				instance.InstanceNumber = -1;
				e.printStackTrace();
			}
			studyMetadata.updateInstance(instance);

		}
		doOverwrite(ds);
		File file = fsu.toFile(ds);

		OutputStream out = openOutputStream(file);
		try {
			ds.setFileMetaInfo(fmi);
			ds.writeFile(out, (DcmEncodeParam) decParam);
			if (parser.getReadTag() != Tags.PixelData) {
				return;
			}
			ds.writeHeader(
				out,
				(DcmEncodeParam) decParam,
				parser.getReadTag(),
				parser.getReadVR(),
				parser.getReadLength());
			copy(in, out);
		} finally {
			try {
				out.close();
			} catch (IOException ignore) {
				ignore.printStackTrace();
			}
		}
		fsu.schedule(file, ds);
	}

	private void doOverwrite(Dataset ds) {
		for (Iterator it = overwrite.iterator(); it.hasNext();) {
			DcmElement el = (DcmElement) it.next();
			ds.putXX(el.tag(), el.vr(), el.getByteBuffer());
		}
	}

	// Package protected ---------------------------------------------

	// Protected -----------------------------------------------------

	// Private -------------------------------------------------------
	private void copy(InputStream in, OutputStream out) throws IOException {
		if (bufferSize > 0) {
			byte[] buffer = new byte[bufferSize];
			int c;
			while ((c = in.read(buffer)) != -1) {
				out.write(buffer, 0, c);
			}
		} else {
			int ch;
			while ((ch = in.read()) != -1) {
				out.write(ch);
			}
		}
	}

	/**
	 * Initializes the image directory destination.
	 * 
	 * 
	 */
	private final void initDest() {
		try {

			String dest =
				(String) Configuration.getInstance().getConfiguredValue(
					"net.medcommons.dicom.directory");

			if (dest.length() == 0) {
				throw new RuntimeException("Directory destination not defined");
			}

			this.dir = new File(dest);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Initializes the server's configuration.
	 * 
	 * @param cfg
	 */
	private void initServer() {
		try {

			String port =
				(String) Configuration.getInstance().getConfiguredValue(
					"net.medcommons.services.dicom.cstore-scp-port");
			String maxClients =
				(String) Configuration.getInstance().getConfiguredValue(
					"net.medcommons.services.dicom.max-clients");
			String rqTimeout =
				(String) Configuration.getInstance().getConfiguredValue(
					"net.medcommons.services.dicom.rq-timeout");
			String dimseTimeout =
				(String) Configuration.getInstance().getConfiguredValue(
					"net.medcommons.services.dicom.dimse-timeout");
			String soCloseDelay =
				(String) Configuration.getInstance().getConfiguredValue(
					"net.medcommons.services.dicom.so-close-delay");
			String packPdvs =
				(String) Configuration.getInstance().getConfiguredValue(
					"net.medcommons.services.dicom.pack-pdvs");

			server.setPort(Integer.parseInt(port));
			server.setMaxClients(Integer.parseInt(maxClients));
			handler.setRqTimeout(Integer.parseInt(rqTimeout));
			handler.setDimseTimeout(Integer.parseInt(dimseTimeout));
			handler.setSoCloseDelay(Integer.parseInt(soCloseDelay));
			handler.setPackPDVs("true".equalsIgnoreCase(packPdvs));
		} catch (ConfigurationException e) {
			//TODO: what is correct behavior here? (See Mantis)
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sets the DICOM CSTORE policies.
	 * 
	 * Important: no restrictions on calling AE titles. We want our
	 * server to be very promiscuous.
	 * 
	 * @param cfg
	 */
	private void initPolicy() throws ConfigurationException, Exception {
		//TODO: Kludge. Where does <any> get recognized? Appears to be noop.
		String calledAETS = "ARCHIVE,DCMSND,<any>";
		//(String) Configuration.getInstance().getConfiguredValue("net.medcommons.services.dicom.called-aets");
		String callingAETS = "ARCHIVE,DCMSND,<any>";
		//(String) Configuration.getInstance().getConfiguredValue("net.medcommons.services.dicom.calling-aets");
		String maxPDULen =
			(String) Configuration.getInstance().getConfiguredValue(
				"net.medcommons.services.dicom.max-pdu-len");
		String maxOpInvoked =
			(String) Configuration.getInstance().getConfiguredValue(
				"net.medcommons.services.dicom.max-op-invoked");

		policy.setCalledAETs(ConfigurationTokenizer.tokenize(calledAETS));
		policy.setCallingAETs(ConfigurationTokenizer.tokenize(callingAETS));
		policy.setMaxPDULength(Integer.parseInt(maxPDULen));
		policy.setAsyncOpsWindow(Integer.parseInt(maxOpInvoked), 1);
		Set keys = Configuration.getInstance().keys();

		/*
		 * Initialize all of the supported presentation contexts specified in the configuration.
		 */
		for (Iterator it = keys.iterator(); it.hasNext();) {
			String key = (String) it.next();
			String supportedPresentationContextNamespace =
				"net.medcommons.services.dicom.spc.";
			int start = supportedPresentationContextNamespace.length();
			if (key.startsWith(supportedPresentationContextNamespace)) {
				String pcKey =
					(String) Configuration.getInstance().getConfiguredValue(
						key);
				initPresContext(
					key.substring(start),
					ConfigurationTokenizer.tokenize(pcKey));
			}
		}
	}

	private void initStudyData() throws IOException {
		GetStudyGUID.init();
		studyMetadata.scan(dir);
		String[] guids = studyMetadata.guidQuery();
		// Hack Code:
		/*
		if (guids != null) {
			for (int i = 0; i < guids.length; i++) {
				MCStudy study = studyMetadata.getStudyWithGUID(guids[i]);
				//System.out.println("\n===\nFilename = " + guids[i]);
				//System.out.println(study);
			}
		
		}
		*/
	}
	/**
	 * Generates a DICOM presentation context for the SCP.
	 * 
	 * These are the list of types of DICOM transfers which 
	 * will be accepted. 
	 * 
	 * @param asName (example: MRImageStorage)
	 * @param tsNames (example: DeflatedExplicitVRLittleEndian, ExplicitVRLittleEndian,
	 * 					ExplicitVRBigEndian,ImplicitVRLittleEndian
	 * 
	 */
	private void initPresContext(String asName, String[] tsNames) {

		String as = UIDs.forName(asName);

		String[] tsUIDs = new String[tsNames.length];
		for (int i = 0; i < tsUIDs.length; ++i) {
			tsUIDs[i] = UIDs.forName(tsNames[i]);

		}
		policy.putPresContext(as, tsUIDs);
		services.bind(as, this);
	}
	/**
	 * Mechanism for overwriting certain DICOM group/element values.
	 * Needs to be incorporated in a more dynamic way (the replaced
	 * values may not be constants for all studies - only within a study)
	 */
	/*
		private void initOverwrite(Configuration cfg) {
			for (Enumeration it = cfg.keys(); it.hasMoreElements();) {
				String key = (String) it.nextElement();
				if (key.startsWith("set.")) {
					try {
						overwrite.putXX(
							Tags.forName(key.substring(4)),
							cfg.getProperty(key));
					} catch (Exception e) {
						throw new IllegalArgumentException(
							"Illegal entry in dcmsnd.cfg - "
								+ key
								+ "="
								+ cfg.getProperty(key));
					}
				}
			}
		}
		*/

	/**
	 * Initializes DICOM protocol over TLS (Transport Layer Security).
	 */
	private void initTLS() {
		try {
			String configProtocol =
				(String) Configuration.getInstance().getConfiguredValue(
					"net.medcommons.services.dicom.protocol");

			this.protocol = DcmProtocol.valueOf(configProtocol);
			if (!protocol.isTLS()) {
				return;
			}

			tls = SSLContextAdapter.getInstance();
			String tlsKeyPassword =
				(String) Configuration.getInstance().getConfiguredValue(
					"net.medcommons.services.dicom.tls-key-passwd");

			char[] keypasswd = tlsKeyPassword.toCharArray();
			String tlsKey =
				(String) Configuration.getInstance().getConfiguredValue(
					"net.medcommons.services.dicom.tls-key");

			tls.setKey(
				tls.loadKeyStore(
					CStoreSCP.class.getResource(tlsKey),
					keypasswd),
				keypasswd);

			String tlsCaCerts =
				(String) Configuration.getInstance().getConfiguredValue(
					"net.medcommons.services.dicom.tls-cacerts");
			String tlsCaCertsPassword =
				(String) Configuration.getInstance().getConfiguredValue(
					"net.medcommons.services.dicom.tls-cacerts-passwd");

			tls.setTrust(
				tls.loadKeyStore(
					CStoreSCP.class.getResource(tlsCaCerts),
					tlsCaCertsPassword.toCharArray()));
			this.server.setServerSocketFactory(
				tls.getServerSocketFactory(protocol.getCipherSuites()));
		} catch (Exception ex) {
			throw new RuntimeException(
				"Could not initalize TLS configuration: ",
				ex);
		}
	}

}
