package net.medcommons.application.dicomclient.http.utils;

import static net.medcommons.application.utils.Str.blank;

import java.io.IOException;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.util.Date;

import net.medcommons.application.dicomclient.transactions.ContextState;
import net.medcommons.application.dicomclient.transactions.PatientMatch;
import net.medcommons.application.dicomclient.utils.DB;
import net.medcommons.application.utils.JSONSimpleGET;
import net.medcommons.modules.services.interfaces.PatientDemographics;
import net.medcommons.modules.utils.Str;

import org.apache.commons.httpclient.HttpException;
import org.apache.log4j.Logger;
import org.json.JSONObject;

/**
 * Class invokes service to create voucher on appliance; obtains the
 * storage id of the created account or an error.
 * 
 * @author mesozoic
 *
 */
public class Voucher {
    
	private static Logger log = Logger.getLogger(Voucher.class);
	private String voucherURL;
	private String patientGivenName;
	private String patientFamilyName;
	private String patientSex;
	private String purpose;
	private ContextState contextState;

	private String service;
	private String voucherid = null;
	private String voucherStatus = null;
	private String patientAuth = null;
	private String patientMedCommonsId = null;
	private String otp = null;
	private Date createDateTime = new Date();
	

	// {"patientAuth":"fdc74c14830896343dcdd06c426832f30f436b96","voucherid":
	// "CRZMZOZ"
	// ,"status":"ok","patientMedCommonsId":"9040047571035135","otp":66897}

	public Voucher(ContextState contextState, String patientGivenName,
			String patientFamilyName, String sex, String service) {
		this.contextState = contextState;
		this.patientGivenName = patientGivenName;
		this.patientFamilyName = patientFamilyName;
		if (patientGivenName == null){
			patientGivenName = "UNKNOWN";
		}
		if (patientFamilyName == null){
			patientFamilyName = "UNKNOWN";
		}
		
		// Convert from DICOM form of sex to CCR form
		if("M".equals(sex))
		    sex = "Male";
		else
		if("F".equals(sex))
		    sex = "Female";
		
		this.service = service;
		this.voucherURL = createVoucherBaseURL();
	}

	/**
	 * Constructs a url for the voucher. Uses the HTTPS constant which contains
	 * a substitute protocol name so that self-signed certs can be used without
	 * any impact on the other code.
	 * 
	 * @return
	 */
	private String createVoucherBaseURL() {
		StringBuffer buff = new StringBuffer();
		if (contextState.getCxpProtocol().equals("https")) {
			buff.append(JSONSimpleGET.HTTPS);
		} else {
			buff.append(contextState.getCxpProtocol());
		}
		buff.append("://");
		buff.append(contextState.getCxpHost());
		if (contextState.getCxpPort() != null) {
			buff.append(":").append(contextState.getCxpPort());
		}
		buff.append("/router/CreateVoucher.action");
		return (buff.toString());
	}

	/**
	 * Returns true if there is enough context to invoke
	 * the service to create a voucher; false otherwise.
	 * @param c
	 * @return
	 */
	public static boolean contextComplete(ContextState c){
		boolean complete = true;
		if (c == null){
			log.info("voucher Context state is null");
			return(false);
		}
		if (Str.blank(c.getAccountId())){
			complete=false;
		}
		if (Str.blank(c.getAuth())){
			complete=false;
		}
		if (Str.blank(c.getCxpHost())){
			complete=false;
		}
		if (Str.blank(c.getCxpProtocol())){
			complete=false;
		}
		if (Str.blank(c.getGatewayRoot())){
			complete=false;
		}
		return(complete);
	}
	
	/**
	 * Creates a new context state from the existing context state plus
	 * values from the newly-created voucher. Basically - the 
	 * server hostname (and other url components), the auth token,
	 * the logged-in account id are derived from the context. The 
	 * account that the documents are to be created is extracted
	 * from the returned voucher value.
	 * 
	 * @param contextState
	 * @return
	 */
	public  ContextState createDocumentUploadContextState(ContextState contextState){
		
		ContextState uploadContextState = new ContextState();
		uploadContextState.setStorageId(getPatientMedCommonsId());
		uploadContextState.setAccountId(contextState.getAccountId());
		//uploadContextState.setAuth(voucher.getPatientAuth());
		uploadContextState.setAuth(contextState.getAuth());
		uploadContextState.setCxpHost(contextState.getCxpHost());
		uploadContextState.setCxpPath(contextState.getCxpPath());
		
		//uploadContextState.setCxpPort("80");
		//uploadContextState.setCxpProtocol("http");
		
		uploadContextState.setCxpPort(contextState.getCxpPort());
		uploadContextState.setCxpProtocol(contextState.getCxpProtocol());
	
		uploadContextState.setGatewayRoot(contextState.getGatewayRoot());
		uploadContextState.setGroupAccountId(contextState.getGroupAccountId());
		uploadContextState.setGroupName(contextState.getGroupName());
		DB.get().save(uploadContextState);
		return(uploadContextState);
	}
	
	/**
	 * Creates a voucher and (as a side effect) gets the StorageID of the newly
	 * created account.
	 * 
	 * http://ci.myhealthespace.com:9080/router/CreateVoucher.action?firstName=
	 * Badboy&accid=1117658438174637&lastName=Test&auth=
	 * d5d813d968b8ae64088b37be1d1ff82addfbab41&service=DICOM+Upload
	 */
	public void createVoucher() throws ParseException, HttpException,
			GeneralSecurityException, IOException {
	    
		StringBuilder buff = new StringBuilder(voucherURL);

		if (patientGivenName == null)
			patientGivenName="UNKNOWN";
		
		if (patientFamilyName == null) {
			patientFamilyName = "UNKNOWN";
		}
		
		buff.append("?firstName=").append(URLEncoder.encode(patientGivenName,"UTF-8"))
    		.append("&lastName=").append(URLEncoder.encode(patientFamilyName,"UTF-8"))
    		.append("&auth=").append(contextState.getAuth())
    		.append("&service=").append(service)
    		.append("&accid=").append(contextState.getAccountId());
		
		if(!blank(contextState.getStorageId())) 
			buff.append("&storageId=").append(contextState.getStorageId());
		
		if(!blank(patientSex))
		    buff.append("&sex=").append(URLEncoder.encode(patientSex,"UTF-8"));
		
		if(!blank(purpose))
		    buff.append("&purpose=").append(URLEncoder.encode(purpose,"UTF-8"));
		    
		String url = buff.toString();
		String errorMessage = null;
		log.info("About to invoke URL\n" + url + "\n");
		JSONSimpleGET get = new JSONSimpleGET();
		JSONObject obj = get.get(url,3);
		voucherStatus = obj.getString("status");
		if ("failed".equals(voucherStatus)) {
			errorMessage = obj.getString("error");
			throw new RuntimeException("Creating voucher failed:\n" + errorMessage);
		}
		
		voucherid = obj.getString("voucherid");
		patientAuth = obj.getString("patientAuth");
		patientMedCommonsId = obj.getString("patientMedCommonsId");
		otp = obj.getString("otp");

		PatientMatch.cache(getDemographics());
	}
	
	public PatientDemographics getDemographics() {
	    PatientDemographics pd = new PatientDemographics();
	    pd.setAccountId(patientMedCommonsId);
	    pd.setFamilyName(patientFamilyName);
	    pd.setGivenName(patientGivenName);
	    pd.setSex(patientSex);
	    return pd;
	}

	public String getVoucherid() {
		return (this.voucherid);
	}

	public String getVoucherStatus() {
		return (this.voucherStatus);
	}

	public String getPatientAuth() {
		return (this.patientAuth);
	}

	public String getPatientMedCommonsId() {
		return (this.patientMedCommonsId);
	}

	public String getOtp() {
		return (this.otp);
	}

    public String getPatientSex() {
        return patientSex;
    }

    public void setPatientSex(String patientSex) {
        this.patientSex = patientSex;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getPatientFamilyName() {
        return patientFamilyName;
    }

    public void setPatientFamilyName(String patientFamilyName) {
        this.patientFamilyName = patientFamilyName;
    }

    public String getPatientGivenName() {
        return patientGivenName;
    }

    public void setPatientGivenName(String patientGivenName) {
        this.patientGivenName = patientGivenName;
    }

    public Date getCreateDateTime() {
        return createDateTime;
    }

}
