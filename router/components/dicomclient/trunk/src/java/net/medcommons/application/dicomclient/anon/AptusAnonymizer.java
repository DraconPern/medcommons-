package net.medcommons.application.dicomclient.anon;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import net.medcommons.application.dicomclient.ContextManager;
import net.medcommons.application.utils.JSONSimpleGET;

import org.apache.log4j.Logger;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.DicomOutputStream;

/**
 * Implements anonymization rules as specified by Aptus / MedCommons
 * agreement - a substitute PatientID is placed in these fields:
 * 
 *     0010,0010  PatientsName
 *     0010,0020  PatientID
 *     
 * and the following fields are removed, if they are present:
 * 
 *     0010,1000  OtherPatientIDs
 *     
 * and the accession number is also replaced:
 * 
 *     0008,0050  AccessionNumber
 *
 * The replacement patient id and accession number are required in the properties
 * object that is passed in the constructor.
 * 
 * @author ssadedin
 */
public class AptusAnonymizer implements Anonymizer {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(AptusAnonymizer.class);
    
    JSONSimpleGET json = new JSONSimpleGET();
    
    String patientId;
    
    String accessionNumber;
    
    File tmpDir = new File(ContextManager.get().getConfigurations().getBaseDirectory(),"tmp.anon");
    
    AptusAnonymizer(Properties props) {
        if(!props.containsKey("patientId"))
            throw new IllegalArgumentException("Anonymization Profile requires parameter 'patientId'");
            
        if(!props.containsKey("accessionNumber"))
            throw new IllegalArgumentException("Anonymization Profile requires parameter 'patientId'");
            
        patientId = props.getProperty("patientId");
        accessionNumber = props.getProperty("accessionNumber");
        
        if(!tmpDir.exists())
            tmpDir.mkdirs();
    }
    
    @Override
    public File anonymize(File f) throws IOException {
        
        try {
            DicomInputStream is = new DicomInputStream(f);
            DicomObject dcm = is.readDicomObject();
            if(dcm == null)
                throw new IllegalArgumentException("File " + f  + " does not represent a valid DICOM object");
            
            String oldPatientId = dcm.getString(Tag.PatientID);
            
            log.info("Replacing patient id " + oldPatientId + "with "+patientId);
            
            dcm.putString(Tag.PatientID, VR.ST, patientId);
            dcm.putString(Tag.PatientName, VR.PN, patientId);
            dcm.putString(Tag.AccessionNumber, VR.ST, accessionNumber);
            
            // dcm.putStrings(Tag.OtherPatientIDs, VR.ST, new String[] {});
            dcm.remove(Tag.OtherPatientIDs);
            
            File anonFile = new File(tmpDir, f.getName()+".anon");
            FileOutputStream os = new FileOutputStream(anonFile);
            try {
                DicomOutputStream out = new DicomOutputStream(os);
                try {
                    out.writeDicomFile(dcm);
                    out.flush();
                }
                finally {
                    out.close();
                }
                return anonFile;
            }
            finally {
                os.close();
            }
        }
        catch (IOException e) {
            log.error("Failed to anonymize DICOM file " + f.getAbsolutePath(), e);
            throw e;
        }
    }
}
