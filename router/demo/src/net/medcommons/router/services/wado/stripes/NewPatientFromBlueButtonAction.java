/*
 * $Id: NewPatientAction.java 3696 2010-04-28 08:00:44Z ssadedin $
 * Created on 10/10/2007
 */
package net.medcommons.router.services.wado.stripes;

import static net.medcommons.modules.utils.Str.blank;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;

import net.medcommons.document.ccr.CCRConstants;
import net.medcommons.modules.configuration.ConfigurationException;
import net.medcommons.modules.services.interfaces.ServiceConstants;
import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.modules.utils.Str;
import net.medcommons.phr.PHRException;
import net.medcommons.router.services.db.DB;
import net.medcommons.router.services.repository.RepositoryException;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;
import net.medcommons.router.web.stripes.JSONResolution;
import net.sourceforge.stripes.action.FileBean;
import net.sourceforge.stripes.action.Resolution;

import org.hibernate.Session;
import org.jdom.JDOMException;

/**
 * Creates a new patient based on a Blue Button File
 * 
 * @author ssadedin
 */
public class NewPatientFromBlueButtonAction extends NewPatientAction {
    
    /**
     * The uploaded blue button file (hopefully)
     */
    FileBean blueButtonFile;
    
    /**
     * Callers order reference for order to be associated with account (if any)
     */
    String callersOrderReference;
    
    /**
     * Blue button import action that we reuse some functions from
     */
    BlueButtonImportCCRAction bbAction = new BlueButtonImportCCRAction();

    @Override
    public Resolution create() {
        try {
            // Set the patient name that is created
            bbAction.setContext(this.getContext());
            bbAction.setUploadedFile(blueButtonFile);
            bbAction.extractCCR(ServiceConstants.PUBLIC_MEDCOMMONS_ID);
            CCRDocument resultCCR = bbAction.getResultCCR();
            this.setSex(resultCCR.getPatientGender());
            this.setDateOfBirth(resultCCR.getJDOMDocument().getValue("patientExactDateOfBirth"));
            this.setFamilyName(resultCCR.getPatientFamilyName());
            this.setGivenName(resultCCR.getPatientGivenName());
        }
        catch (Exception e) {
            return new JSONResolution("text/plain",e);
        }
        
        // Note - super method will call our getBaseCCR() below when it is time to make a CCR for the patient
        return super.create();
    }
    
    /**
     * Instead of loading an empty template we create the CCR by extracting the
     * data from the original Blue Button File.
     */
    @Override
    protected CCRDocument getBaseCCR(String patientId) throws JDOMException, IOException, ParseException,
                    RepositoryException, NoSuchAlgorithmException, PHRException {
        try {
            bbAction.getResultCCR().addPatientId(patientId, CCRConstants.MEDCOMMONS_ACCOUNT_ID_TYPE);
            bbAction.getResultCCR().setStorageId(patientId);
            bbAction.attachBlueButtonFile(patientId);
            if(!blank(this.callersOrderReference) && !blank(this.getSponsorAccountId()))  {
                updateOrder(patientId);
            }
            return bbAction.getResultCCR();
        }
        catch (ConfigurationException e) {
            throw new PHRException("Unable to import blue button file to create patient CCR: " + e.getMessage(), e);
        }
        catch (ServiceException e) {
            throw new PHRException("Unable to import blue button file to create patient CCR: " + e.getMessage(), e);
        }
    }

    protected void updateOrder(String patientId) {
        Session s = DB.currentSession();
        try {
            s.createSQLQuery("update dicom_order set mcid = :accid where callers_order_reference = :ref and group_account_id = :group")
                        .setString("ref", patientId)
                        .setString("group", this.getSponsorAccountId())
                        .executeUpdate();
        }
        finally {
            DB.closeSession();
        }
    }

    public FileBean getBlueButtonFile() {
        return blueButtonFile;
    }

    public void setBlueButtonFile(FileBean blueButtonFile) {
        this.blueButtonFile = blueButtonFile;
    }

    public String getCallersOrderReference() {
        return callersOrderReference;
    }

    public void setCallersOrderReference(String callersOrderReference) {
        this.callersOrderReference = callersOrderReference;
    }
}
