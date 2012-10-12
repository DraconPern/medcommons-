package net.medcommons.application.dicomclient.dicom;





import java.awt.Image;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import net.medcommons.application.dicomclient.Configurations;
import net.medcommons.application.dicomclient.ContextManager;
import net.medcommons.application.dicomclient.ImportHandler;
import net.medcommons.application.dicomclient.utils.DirectoryUtils;
import net.medcommons.application.dicomclient.utils.ExtractFileMetadata;
import net.medcommons.application.dicomclient.utils.StatusDisplayManager;
import net.medcommons.modules.crypto.SHA1;
import net.medcommons.modules.services.interfaces.DicomMetadata;
import net.medcommons.modules.utils.FileUtils;

import org.apache.log4j.Logger;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.DicomOutputStream;
import org.dcm4che2.io.StopTagInputHandler;
import org.dcm4che2.net.Association;
import org.dcm4che2.net.Device;
import org.dcm4che2.net.DicomServiceException;
import org.dcm4che2.net.Executor;
import org.dcm4che2.net.NetworkApplicationEntity;
import org.dcm4che2.net.NetworkConnection;
import org.dcm4che2.net.NewThreadExecutor;
import org.dcm4che2.net.PDVInputStream;
import org.dcm4che2.net.Status;
import org.dcm4che2.net.TransferCapability;
import org.dcm4che2.net.service.StorageService;
import org.dcm4che2.net.service.VerificationService;
import org.hibernate.HibernateException;

/**
 * Portions (*MOST*) of this class were derived from the dcm4che2 Java class
 * org.dcm4che2.tool.dcmrcv.DcmRcv. The original license is below.
 *
 * basic changes made to this class:
 * <ul>
 *  <li> Removed main() and CommandLine functionality. We don't need the those jars nor
 *       a back door to start the thread.
 *  <li> Inserted callback to importHandler for each incoming object.
 *  <li> Makes a call to importHandler to detect duplicates. New behavior: if
 *       an incoming SopInstance is a duplicate - don't write to disk. Just
 *       process the stream and throw the object away.
 *  <li> Class now implements Runnable interface
 *  <li>
 * </ul>
 */
/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Gunter Zeilinger, Huetteldorferstr. 24/10, 1150 Vienna/Austria/Europe.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunterze@gmail.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */








/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision: 1.19 $ $Date: 2007/03/08 01:15:52 $
 * @since Oct 13, 2005
 */
public class CstoreScp extends StorageService implements Runnable{

	private static Logger log = Logger.getLogger(CstoreScp.class.getName());

    private static final String DEST = "dest";
    private static final String DEF_TS = "defts";
    private static final String NATIVE = "native";
    private static final String BIG_ENDIAN = "bigendian";
    private static final String BUFSIZE = "bufsize";
    private static final String PDV1 = "pdv1";
    private static final String ASYNC = "async";
    private static final String REQUEST_TO = "requestTO";
    private static final String RELEASE_TO = "releaseTO";
    private static final String SO_CLOSEDELAY = "soclosedelay";
    private static final String TCP_DELAY = "tcpdelay";
    private static final String SO_RCVBUF = "sorcvbuf";
    private static final String SO_SNDBUF = "sosndbuf";
    private static final String SND_PDULEN = "sndpdulen";
    private static final String RCV_PDULEN = "rcvpdulen";
    private static final String RSP_DELAY = "rspdelay";
    private static final String IDLE_TO = "idleTO";
    private static final String REAPER = "reaper";
    private static final String VERSION = "V";
    private static final String HELP = "h";

    private static final int KB = 1024;
    private static final int PEEK_LEN = 1024;
    private static final int TIMEOUT_FILE_IMPORT = 2;
    private static final int TIMEOUT_DICOM_CSTORE_IMPORT = 30;
    private static final String USAGE =
        "dcmrcv [Options] [<aet>[@<ip>]:]<port>";
    private static final String DESCRIPTION =
        "DICOM Server listening on specified <port> for incoming association " +
        "requests. If no local IP address of the network interface is specified " +
        "connections on any/all local addresses are accepted. If <aet> is " +
        "specified, only requests with matching called AE title will be " +
        "accepted.\n" +
        "Options:";
    private static final String EXAMPLE =
        "\nExample: dcmrcv DCMRCV:11112 -dest /tmp \n" +
        "=> Starts server listening on port 11112, accepting association " +
        "requests with DCMRCV as called AE title. Received objects " +
        "are stored to /tmp.";

    private static final String[] ONLY_DEF_TS =
    {
        UID.ImplicitVRLittleEndian
    };

    private static final String[] NATIVE_TS =
    {
        UID.ExplicitVRLittleEndian,
        UID.ExplicitVRBigEndian,
        UID.ImplicitVRLittleEndian
    };

    private static final String[] NATIVE_LE_TS =
    {
        UID.ExplicitVRLittleEndian,
        UID.ImplicitVRLittleEndian
    };

    private static final String[] NON_RETIRED_TS =
    {
        UID.JPEGLSLossless,
        UID.JPEGLossless,
        UID.JPEGLosslessNonHierarchical14,
        UID.JPEG2000LosslessOnly,
        UID.DeflatedExplicitVRLittleEndian,
        UID.RLELossless,
        UID.ExplicitVRLittleEndian,
        UID.ExplicitVRBigEndian,
        UID.ImplicitVRLittleEndian,
        UID.JPEGBaseline1,
        UID.JPEGExtended24,
        UID.JPEGLSLossyNearLossless,
        UID.JPEG2000,
        UID.MPEG2,
    };

    private static final String[] NON_RETIRED_LE_TS =
    {
        UID.JPEGLSLossless,
        UID.JPEGLossless,
        UID.JPEGLosslessNonHierarchical14,
        UID.JPEG2000LosslessOnly,
        UID.DeflatedExplicitVRLittleEndian,
        UID.RLELossless,
        UID.ExplicitVRLittleEndian,
        UID.ImplicitVRLittleEndian,
        UID.JPEGBaseline1,
        UID.JPEGExtended24,
        UID.JPEGLSLossyNearLossless,
        UID.JPEG2000,
        UID.MPEG2,
    };

    private static final String[] CUIDS =
    {
        UID.BasicStudyContentNotificationSOPClassRetired,
        UID.StoredPrintStorageSOPClassRetired,
        UID.HardcopyGrayscaleImageStorageSOPClassRetired,
        UID.HardcopyColorImageStorageSOPClassRetired,
        UID.ComputedRadiographyImageStorage,
        UID.DigitalXRayImageStorageForPresentation,
        UID.DigitalXRayImageStorageForProcessing,
        UID.DigitalMammographyXRayImageStorageForPresentation,
        UID.DigitalMammographyXRayImageStorageForProcessing,
        UID.DigitalIntraoralXRayImageStorageForPresentation,
        UID.DigitalIntraoralXRayImageStorageForProcessing,
        UID.StandaloneModalityLUTStorageRetired,
        UID.EncapsulatedPDFStorage,
        UID.StandaloneVOILUTStorageRetired,
        UID.GrayscaleSoftcopyPresentationStateStorageSOPClass,
        UID.ColorSoftcopyPresentationStateStorageSOPClass,
        UID.PseudoColorSoftcopyPresentationStateStorageSOPClass,
        UID.BlendingSoftcopyPresentationStateStorageSOPClass,
        UID.XRayAngiographicImageStorage,
        UID.EnhancedXAImageStorage,
        UID.XRayRadiofluoroscopicImageStorage,
        UID.EnhancedXRFImageStorage,
        UID.XRayAngiographicBiPlaneImageStorageRetired,
        UID.PositronEmissionTomographyImageStorage,
        UID.StandalonePETCurveStorageRetired,
        UID.CTImageStorage,
        UID.EnhancedCTImageStorage,
        UID.NuclearMedicineImageStorage,
        UID.UltrasoundMultiframeImageStorageRetired,
        UID.UltrasoundMultiframeImageStorage,
        UID.MRImageStorage,
        UID.EnhancedMRImageStorage,
        UID.MRSpectroscopyStorage,
        UID.RTImageStorage,
        UID.RTDoseStorage,
        UID.RTStructureSetStorage,
        UID.RTBeamsTreatmentRecordStorage,
        UID.RTPlanStorage,
        UID.RTBrachyTreatmentRecordStorage,
        UID.RTTreatmentSummaryRecordStorage,
        UID.NuclearMedicineImageStorageRetired,
        UID.UltrasoundImageStorageRetired,
        UID.UltrasoundImageStorage,
        UID.RawDataStorage,
        UID.SpatialRegistrationStorage,
        UID.SpatialFiducialsStorage,
        UID.RealWorldValueMappingStorage,
        UID.SecondaryCaptureImageStorage,
        UID.MultiframeSingleBitSecondaryCaptureImageStorage,
        UID.MultiframeGrayscaleByteSecondaryCaptureImageStorage,
        UID.MultiframeGrayscaleWordSecondaryCaptureImageStorage,
        UID.MultiframeTrueColorSecondaryCaptureImageStorage,
        UID.VLImageStorageRetired,
        UID.VLEndoscopicImageStorage,
        UID.VideoEndoscopicImageStorage,
        UID.VLMicroscopicImageStorage,
        UID.VideoMicroscopicImageStorage,
        UID.VLSlideCoordinatesMicroscopicImageStorage,
        UID.VLPhotographicImageStorage,
        UID.VideoPhotographicImageStorage,
        UID.OphthalmicPhotography8BitImageStorage,
        UID.OphthalmicPhotography16BitImageStorage,
        UID.StereometricRelationshipStorage,
        UID.VLMultiframeImageStorageRetired,
        UID.StandaloneOverlayStorageRetired,
        UID.BasicTextSR,
        UID.EnhancedSR,
        UID.ComprehensiveSR,
        UID.ProcedureLogStorage,
        UID.MammographyCADSR,
        UID.KeyObjectSelectionDocument,
        UID.ChestCADSR ,
        UID.StandaloneCurveStorageRetired,
        UID._12leadECGWaveformStorage,
        UID.GeneralECGWaveformStorage,
        UID.AmbulatoryECGWaveformStorage,
        UID.HemodynamicWaveformStorage,
        UID.CardiacElectrophysiologyWaveformStorage,
        UID.BasicVoiceAudioWaveformStorage,
        UID.HangingProtocolStorage,
        UID.SiemensCSANonImageStorage
    };


    private Executor executor = new NewThreadExecutor("DCMRCV");
    private Device device = new Device("DCMRCV");
    private NetworkApplicationEntity ae = new NetworkApplicationEntity();
    private NetworkConnection nc = new NetworkConnection();
    private String[] tsuids = NON_RETIRED_LE_TS;
    private File destination;
    private boolean devnull;
    private int fileBufferSize = 256;
    private int rspdelay = 0;
    
   

    ImportHandler importHandler = null;


    public CstoreScp(ImportHandler importHandler)
    {
        super(CUIDS);
        device.setNetworkApplicationEntity(ae);
        device.setNetworkConnection(nc);
        ae.setNetworkConnection(nc);
        ae.setAssociationAcceptor(true);
        ae.register(new VerificationService());
        ae.register(this);
        this.importHandler = importHandler;
      
    }

   
    public final void setAEtitle(String aet)
    {
        ae.setAETitle(aet);
    }

    public final void setHostname(String hostname)
    {
        nc.setHostname(hostname);
    }

    public final void setPort(int port)
    {
        nc.setPort(port);
    }

    public final void setPackPDV(boolean packPDV)
    {
        ae.setPackPDV(packPDV);
    }

    public final void setAssociationReaperPeriod(int period)
    {
        device.setAssociationReaperPeriod(period);
    }

    public final void setTcpNoDelay(boolean tcpNoDelay)
    {
        nc.setTcpNoDelay(tcpNoDelay);
    }

    public final void setRequestTimeout(int timeout)
    {
        nc.setRequestTimeout(timeout);
    }

    public final void setReleaseTimeout(int timeout)
    {
        nc.setReleaseTimeout(timeout);
    }

    public final void setSocketCloseDelay(int delay)
    {
        nc.setSocketCloseDelay(delay);
    }

    public final void setIdleTimeout(int timeout)
    {
        ae.setIdleTimeout(timeout);
    }

    public final void setDimseRspTimeout(int timeout)
    {
        ae.setDimseRspTimeout(timeout);
    }

    public final void setMaxPDULengthSend(int maxLength)
    {
        ae.setMaxPDULengthSend(maxLength);
    }

    public void setMaxPDULengthReceive(int maxLength)
    {
        ae.setMaxPDULengthReceive(maxLength);
    }

    public final void setReceiveBufferSize(int bufferSize)
    {
        nc.setReceiveBufferSize(bufferSize);
    }

    public final void setSendBufferSize(int bufferSize)
    {
        nc.setSendBufferSize(bufferSize);
    }

    public void setDimseRspDelay(int delay)
    {
        rspdelay = delay;
    }

    public String getVersion(){
   	 Package p = CstoreScp.class.getPackage();
        return(p.getImplementationVersion());
   }



    public void run(){
    	setTransferSyntax(NON_RETIRED_TS);
    	setPackPDV(false);
        setTcpNoDelay(false);
       // int maxOpsPerformed = parseInt("0", "illegal argument of option -async", 0, 0xffff);
        //setMaxOpsPerformed(maxOpsPerformed)


    	 initTransferCapability();
         try
         {
             start();
         }
         catch (IOException e)
         {
             e.printStackTrace();
         }
         finally{
        	 log.info("Exiting server run() method");
         }
    }
    private void setTransferSyntax(String[] tsuids)
    {
        this.tsuids = tsuids;
    }

    private void initTransferCapability()
    {
        TransferCapability[] tc = new TransferCapability[CUIDS.length+1];
        tc[0] = new TransferCapability(UID.VerificationSOPClass, ONLY_DEF_TS,
                TransferCapability.SCP);
        for (int i = 0; i < CUIDS.length; i++)
            tc[i+1] = new TransferCapability(CUIDS[i], tsuids, TransferCapability.SCP);
        ae.setTransferCapability(tc);
    }

    private void setFileBufferSize(int size)
    {
        fileBufferSize = size;
    }

    private void setMaxOpsPerformed(int maxOps)
    {
        ae.setMaxOpsPerformed(maxOps);
    }

    public void setCacheDirectory(String filePath) throws IOException{

    	setDestination(filePath);
    }
    private void setDestination(String filePath) throws IOException
    {
        this.destination = new File(filePath);
        this.devnull = "/dev/null".equals(filePath);
        if (!devnull){
        	DirectoryUtils.makeDirectory(destination);

        }
        if (devnull){
        	log.info("Incoming DICOM files will be discarded - network test mode");
        }
        else{
        	log.info("Cache directory for incoming DICOM: " + filePath);
        }

    }

    public void start() throws IOException
    {
        device.startListening(executor );
        log.info("Start Server listening on port " + nc.getPort() + " with AE Title " + ae.getAETitle());
    }

    public void stop(){
    	device.stopListening();
    	log.info("Stopped server listening on port " + nc.getPort() + " with AE Title " + ae.getAETitle());
    }

    private static String[] split(String s, char delim, int defPos)
    {
        String[] s2 = new String[2];
        s2[defPos] = s;
        int pos = s.indexOf(delim);
        if (pos != -1)
        {
            s2[0] = s.substring(0, pos);
            s2[1] = s.substring(pos + 1);
        }
        return s2;
    }

    private static void exit(String msg)
    {
        System.err.println(msg);
        System.err.println("Try 'dcmrcv -h' for more information.");
        System.exit(1);
    }

    public static int parseInt(String s, String errPrompt, int min, int max) {
        try {
            int i = Integer.parseInt(s);
            if (i >= min && i <= max)
                return i;
        } catch (NumberFormatException e) {}
        exit(errPrompt);
        throw new RuntimeException();
    }

    protected void doCStore(Association as, int pcid, DicomObject rq,
            PDVInputStream dataStream, String tsuid, DicomObject rsp)
            throws IOException, DicomServiceException
    {
    	Image currentImage = StatusDisplayManager.getCurrentImage();
    	StatusDisplayManager.setActiveIcon();
    	Configurations configs = ContextManager.getContextManager().getConfigurations();
    	configs.setDicomTimeout(TIMEOUT_DICOM_CSTORE_IMPORT);

    	boolean success = false;
        if (destination == null)
        {
        	log.info("CSTORE:destination=null");
            super.doCStore(as, pcid, rq, dataStream, tsuid, rsp);
            success = true;
        }
        else
        {
            try
            {
                String cuid = rq.getString(Tag.AffectedSOPClassUID);
                String iuid = rq.getString(Tag.AffectedSOPInstanceUID);
                boolean haveSopInstance = importHandler.sopInstanceExists(iuid);
                if (haveSopInstance){
                	log.info("CSTORE:ignoring duplicate image " + iuid);
                    super.doCStore(as, pcid, rq, dataStream, tsuid, rsp);
                    success = true;
                }
                else{
	                BasicDicomObject fmi = new BasicDicomObject();
	                fmi.initFileMetaInformation(cuid, iuid, tsuid);


	                File file = devnull ? destination : new File(destination, iuid);
	                log.debug("About to save to file:" + file.getAbsolutePath());
	                FileOutputStream fos = new FileOutputStream(file);

	                BufferedOutputStream bos = new BufferedOutputStream(fos, fileBufferSize);
	                DicomOutputStream dos = new DicomOutputStream(bos);

	                dos.writeFileMetaInformation(fmi);
	                dataStream.copyTo(dos);


	                dos.close();
	                ExtractFileMetadata extractFileMetadata = new ExtractFileMetadata(file);

	                DicomMetadata dicomMetadata = extractFileMetadata.parse();
	                SHA1 sha1 = new SHA1();
	                sha1.initializeHashStreamCalculation();
	                String guid = sha1.calculateFileHash(file);
	                dicomMetadata.setSha1(guid);
	                dicomMetadata.setFile(file);
	                dicomMetadata.setLength(file.length());
	                dicomMetadata.setTransactionStatus(DicomMetadata.STATUS_COMPLETE);
	                dicomMetadata.setCalledAeTitle(as.getCalledAET());
	                dicomMetadata.setCallingAeTitle(as.getCallingAET());
	                importHandler.cstoreEvent(dicomMetadata);
	                success = true;
                }

            }
            catch (IOException e)
            {
            	log.error("IOException", e);
                throw new DicomServiceException(rq, Status.ProcessingFailure,
                        e.getMessage());
            }
            catch (NoSuchAlgorithmException e)
            {
            	log.error("NoSuchAlgorithmException", e);
                throw new DicomServiceException(rq, Status.ProcessingFailure,
                        e.getMessage());
            }
            catch (HibernateException e)
            {
            	log.error("HibernateException", e);
                throw new DicomServiceException(rq, Status.ProcessingFailure,
                        e.getMessage());
            }
            finally{
    			if (success){
    				StatusDisplayManager.setIdleIcon();
    				//StatusDisplayManager.setImage(currentImage);
    			}
    			else{
    				StatusDisplayManager.setErrorIcon();
    			}
    		}
        }
        if (rspdelay > 0)
            try
            {
                Thread.sleep(rspdelay);
            }
            catch (InterruptedException e) {}
    }
    
    
    private void doFileImport(File file) throws IOException,
			DicomServiceException {
		
		StatusDisplayManager.setActiveIcon();

		boolean success = false;
		try {
			ExtractFileMetadata extractFileMetadata = new ExtractFileMetadata(
					file);

			DicomMetadata dicomMetadata = extractFileMetadata.parse();
			SHA1 sha1 = new SHA1();
			sha1.initializeHashStreamCalculation();
			String guid = sha1.calculateFileHash(file);
			dicomMetadata.setSha1(guid);
			dicomMetadata.setFile(file);
			dicomMetadata.setLength(file.length());
			dicomMetadata.setTransactionStatus(DicomMetadata.STATUS_COMPLETE);
			dicomMetadata.setCalledAeTitle("FILE");
			dicomMetadata.setCallingAeTitle("FILE");
			importHandler.cstoreEvent(dicomMetadata);
			success = true;
		} 
		catch(NoSuchAlgorithmException e){
			log.error("Exception importing file " + file.getAbsolutePath(), e);
			success = false;
		}
		finally {
			if (success) {
				StatusDisplayManager.setIdleIcon();
				// StatusDisplayManager.setImage(currentImage);
			} else {
				StatusDisplayManager.setErrorIcon();
			}
		}
	}
      
    /**
     * Scans directory for DICOM files.
     * If file exists and is a valid part 10 file then 
     * import it. Importing means that the file is copied to cache.
     * 
     * @param f
     * @throws IOException
     */
    public void importDicomFilesInDirectory(ImportFiles importFiles, File f) throws IOException {

    	Configurations configs = ContextManager.getContextManager().getConfigurations();
    	configs.setDicomTimeout(TIMEOUT_FILE_IMPORT);
        if (f.isDirectory()) {
            File[] fs = f.listFiles();
            for (int i = 0; i < fs.length; i++)
            	importDicomFilesInDirectory(importFiles, fs[i]);
            return;
        }
        
        DicomObject dcmObj = new BasicDicomObject();
        try {
            DicomInputStream in = new DicomInputStream(f);
            try {
                in.setHandler(new StopTagInputHandler(Tag.StudyDate));
                in.readDicomObject(dcmObj, PEEK_LEN);
                // Attempt to get some values which will fail if the file isn't
                // a valid DICOM file.
                String transferSytnax = in.getTransferSyntax().uid();
                long pos = in.getEndOfFileMetaInfoPosition();
            } finally {
                try {
                    in.close();
                } catch (IOException ignore) {
                }
            }
        } catch (IOException e) {
        	importFiles.skippedFile();
            log.debug("Failed to parse - Not a DICOM file " + f + " - skipped.");
         
            return; //throw e;

        }
        String sopClassUID = dcmObj.getString(Tag.SOPClassUID);
        if (sopClassUID == null) {
            log.debug("WARNING: Missing SOP Class UID in " + f
                    + " - skipped.");
            importFiles.skippedFile();
            return;


        }
        String sopInstanceUID = dcmObj.getString(Tag.SOPInstanceUID);
        if (sopInstanceUID == null) {
        	importFiles.skippedFile();
            log.debug("Missing SOP Instance UID in " + f
                    + " - skipped.");
            return;


        }
        boolean haveSopInstance = importHandler.sopInstanceExists(sopInstanceUID);
        if (haveSopInstance){
        	log.info("CSTORE:ignoring duplicate image " + sopInstanceUID);
        	importFiles.skippedFile();
        	return;
        }
        File file =  new File(destination, sopInstanceUID);
        FileUtils.copyFile(f, file);
        doFileImport(file);
        importFiles.importedFile();
        

    }


}
