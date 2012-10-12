package net.medcommons.application.dicomclient.http.utils;

import java.io.IOException;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.text.ParseException;

import net.medcommons.application.dicomclient.transactions.ContextState;
import net.medcommons.application.dicomclient.transactions.TransactionUtils;
import net.medcommons.application.utils.JSONSimpleGET;
import net.medcommons.modules.utils.Str;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.ssl.HttpSecureProtocol;
import org.apache.commons.ssl.TrustMaterial;
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
	private ContextState contextState;

	private String service;
	private String voucherid = null;
	private String voucherStatus = null;
	private String patientAuth = null;
	private String patientMedCommonsId = null;
	private String otp = null;
	

	// {"patientAuth":"fdc74c14830896343dcdd06c426832f30f436b96","voucherid":
	// "CRZMZOZ"
	// ,"status":"ok","patientMedCommonsId":"9040047571035135","otp":66897}

	public Voucher(ContextState contextState, String patientGivenName,
			String patientFamilyName, String service) {
		this.contextState = contextState;
		this.patientGivenName = patientGivenName;
		this.patientFamilyName = patientFamilyName;
		if (patientGivenName == null){
			patientGivenName = "UNKNOWN";
		}
		if (patientFamilyName == null){
			patientFamilyName = "UNKNOWN";
		}
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
		uploadContextState = TransactionUtils.saveTransaction(uploadContextState);
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
		StringBuffer buff = new StringBuffer(voucherURL);

		if (patientGivenName == null)
			patientGivenName="UNKNOWN";
		if (patientFamilyName == null){
			patientFamilyName = "UNKNOWN";
		}
		buff.append("?firstName=").append(URLEncoder.encode(patientGivenName));
		buff.append("&lastName=").append(URLEncoder.encode(patientFamilyName));

		buff.append("&auth=").append(contextState.getAuth());
		buff.append("&service=").append(service);
		buff.append("&accid=").append(contextState.getAccountId());
		String url = buff.toString();
		String errorMessage = null;
log.info("About to invoke URL\n" + url + "\n");
		JSONSimpleGET get = new JSONSimpleGET(contextState);
		
		
		JSONObject obj = get.executeMethod(url);
		voucherStatus = obj.getString("status");
		if ("failed".equals(voucherStatus)) {
			errorMessage = obj.getString("error");
			throw new RuntimeException("Creating voucher failed:\n"
					+ errorMessage);
		}
		voucherid = obj.getString("voucherid");

		patientAuth = obj.getString("patientAuth");
		patientMedCommonsId = obj.getString("patientMedCommonsId");
		otp = obj.getString("otp");

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

}
