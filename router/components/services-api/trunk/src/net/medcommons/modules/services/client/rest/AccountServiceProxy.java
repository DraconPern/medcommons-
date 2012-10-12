package net.medcommons.modules.services.client.rest;


import static java.lang.Long.parseLong;
import static net.medcommons.modules.utils.Str.blank;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.services.interfaces.*;
import net.medcommons.rest.RESTException;
import net.medcommons.rest.RESTUtil;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.filter.ElementFilter;
import org.json.JSONArray;
import org.json.JSONObject;

 
/**
 * Implementation of the AccountService API that is actually a proxy to the service
 * implemented in PHP on the Account Server.
 * 
 * @author ssadedin
 */
public class AccountServiceProxy implements AccountService {
	/**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger(AccountService.class);
	
	private String authToken = null;
	
	 /**
     * Creates a AccountServiceProxy
     * @param clientId
     */
    public AccountServiceProxy(String clientId) {
        this.authToken = clientId;
    }
	
	public void addCCRLogEntry(String guid, String idp, String from, String to, String accid, Date date, String subject, String status, String trackingNumber) throws ServiceException{
		log.info("Creating CCR log entry for " + guid);
		 if(status == null)
	            status = "accepted";
	        try {
	            RESTUtil.call(authToken, "AccountService.addCCRLogEntry", "guid", guid, "idp", idp, "from", from, "to", to, "accid", accid,
	            		"date", date.toString(), "subject", subject, "status", status, "tracking", trackingNumber);
	        }
	        catch (RESTException e) {
	            throw new ServiceException("Unable to accept add CCR Log entry for =" + accid + " guid=" + guid, e);
	        }
	}

    public void setEmergencyCCR(String accId, String guid, String einfo) throws ServiceException {        
        try {
            RESTUtil.call(authToken, "AccountService.setEmergencyCCR", "guid", guid, "accid", accId, "einfo", einfo);
        }
        catch (RESTException e) {
            throw new ServiceException("Unable to set emergency CCR for =" + accId + " guid=" + guid, e);
        }        
    }

    public CoverInfo queryCoverInfo(Long coverId) throws ServiceException {
        try {
            Document doc = RESTUtil.call(authToken, "AccountService.queryCoverInfo", "coverId", String.valueOf(coverId));
            Element outputs = doc.getRootElement().getChild("outputs");
            if(!"ok".equals(outputs.getChildTextTrim("status"))) {
                throw new ServiceException("Unable to locate cover with id " + coverId + " status " 
                		+ outputs.getChildText("status"));
            }
            
            CoverInfo result = new CoverInfo();
            result.accountId = outputs.getChildTextTrim("accountId");
            result.encryptedPin = outputs.getChildTextTrim("encryptedPin");
            result.notification = outputs.getChildTextTrim("notification");             
            result.providerCode = outputs.getChildTextTrim("providerCode");            
            result.title = outputs.getChildTextTrim("title");            
            result.note = outputs.getChildTextTrim("note");            
            result.pin = outputs.getChildTextTrim("coverPin");            
            return result;
        }
        catch (RESTException e) {
            throw new ServiceException("Unable to query cover info for =" + coverId, e);
        }        
         
    }

    public void addAccountDocument(String accountId, String guid, AccountDocumentType documentType, String comment, boolean unique, String notificationStatus) throws ServiceException {
        try {
            RESTUtil.call(authToken, "AccountService.addAccountDocument", "guid", guid, "accid", accountId,
                            "documentType", documentType.name(), "comment", comment, "unique", unique ? "true" : "false", 
                            "status", notificationStatus);
            
            // Create a profile for the document
            ProfileService profiles = Configuration.getBean("profilesService");
            profiles.createProfile(accountId, new PHRProfile(documentType.name(), null));
        }
        catch (RESTException e) {
            throw new ServiceException("Unable to add account document " + accountId + " guid=" + guid, e);
        }        
    }

    public AccountSettings queryAccountSettings(String accountId) throws ServiceException {
        try {
            
            if(blank(accountId) || ServiceConstants.PUBLIC_MEDCOMMONS_ID.equals(accountId))
                return new AccountSettings(accountId, null,null,null,null,null,null,null,null,null,null);
            
            Element root = RESTUtil.call(authToken, "AccountService.querySettings", "accid",accountId).getRootElement().getChild("outputs");
            if(root == null) {
                throw new ServiceException("Unable to query account settings  for account " + accountId);
            }
            if("failed".equals(root.getChildTextTrim("status"))) {
                return new AccountSettings(accountId, null,null,null,null,null,null,null,null,null,null);
            }
            AccountSettings result = new AccountSettings(
                            accountId,
                            root.getChildTextTrim("groupAccountId"),
                            root.getChildTextTrim("groupName"),
                            root.getChildTextTrim("registry"), 
                            root.getChildTextTrim("directory"),
                            root.getChildTextTrim("groupInstanceId"),
                            root.getChildTextTrim("statusValues"),
                            root.getChildTextTrim("firstName"),
                            root.getChildTextTrim("lastName"),
                            root.getChildTextTrim("email"),
                            root.getChildTextTrim("photoUrl")
                            );
            
            result.setVouchersEnabled(Boolean.valueOf(root.getChildTextTrim("vouchersEnabled")));
            
            result.setAmazonPid(root.getChildTextTrim("amazonPid"));
            result.setAmazonProductToken(root.getChildTextTrim("amazonProductToken"));
            result.setAmazonUserToken(root.getChildTextTrim("amazonUserToken"));
            
            
            result.setTipState(Long.parseLong(root.getChildTextTrim("tipState")));
            
            String practiceIdValue = root.getChildTextTrim("practiceId");
            if(!blank(practiceIdValue)) 
                result.setPracticeId(Long.parseLong(practiceIdValue));
            
            String groupCreateDateTime = root.getChildTextTrim("groupCreateDateTime");
            if(!blank(groupCreateDateTime)) {
                SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                result.setGroupCreateDateTime(new Timestamp(f.parse(groupCreateDateTime).getTime()));
            }
            
            for (Iterator iter = root.getChild("creationRights").getDescendants(new ElementFilter("accountId")); iter.hasNext();) {
                Element creationRightId = (Element) iter.next();
                result.getCreationRights().add(creationRightId.getTextTrim());                
            }            
            
            for (Iterator iter = root.getDescendants(new ElementFilter("document")); iter.hasNext();) {
                Element doc = (Element) iter.next();
                result.getAccountDocuments().put(AccountDocumentType.valueOf(doc.getChildTextTrim("type")),doc.getChildTextTrim("guid")); 
            }            
            
            Element voucher = root.getChild("voucher"); 
            if(voucher != null) {
                SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd"); 
                result.setExpiryDate(f.parse(voucher.getChildTextTrim("expirationDate")));
                Voucher v = new Voucher();
                v.setId(voucher.getChildTextTrim("voucherId"));
                v.setCouponNum(parseLong(voucher.getChildTextTrim("couponNum")));
                v.setOtpHash(voucher.getChildTextTrim("otpHash"));
                v.setStatus(voucher.getChildTextTrim("status"));
                v.setProviderAccountId(voucher.getChildTextTrim("providerAccId"));
                result.setVoucher(v);
            }
            
            result.setDicomAeTitle(root.getChildTextTrim("dicomAeTitle"));
            result.setDicomHost(root.getChildTextTrim("dicomIpAddress"));
            
            String port = root.getChildTextTrim("dicomPort");
            if(!blank(port))
                result.setDicomPort(Integer.parseInt(port));
            
            for (Iterator iter = root.getDescendants(new ElementFilter("app")); iter.hasNext();) {
                Element e = (Element) iter.next();
                result.getApplications().add(new Application(e.getChildText("name"), e.getChildText("key"), e.getChildText("code"), "","",null,null));
            }            
            
            return result;
        }
        catch (RESTException e) {
            throw new ServiceException("Unable to query account settings  for account " + accountId, e);
        }
        catch (ParseException e) {
            throw new ServiceException("Unable to query account settings  for account " + accountId, e);
        } 
    }

    public String queryCCRLog(String accountId) throws ServiceException {
        try {
            String serviceUrl = RESTUtil.resolveServiceUrl("AccountService.queryAccountCCRs");
            StringWriter result = RESTUtil.fetchUrlResponse(serviceUrl+"?accid="+accountId);
            return result.toString();
        }
        catch (RESTException e) {
            throw new ServiceException("Unable to query CCRs for acocunt" + accountId,e);
        }
        catch (IOException e) {
            throw new ServiceException("Unable to query CCRs for acocunt" + accountId,e);
        }        
    }

    public void billingEvent(String accountId, BillingEventType type, int count, String reference, String description) throws ServiceException {
        try {
            RESTUtil.call(authToken, "AccountService.billingEvent", "accountId",accountId, "type", type.name(), "reference",reference, "count", String.valueOf(count), "description", description);
        }
        catch (RESTException e) {
            throw new ServiceException("Unable to register billing event " + type + " for account " + accountId + " (" + description + ")",e);
        }
    }

    public ArrayList<AccountShare> querySharingRights(String accountId) throws ServiceException {
        try {
            ArrayList<AccountShare> results = new ArrayList<AccountShare>();
            
            JSONObject obj = RESTUtil.callJSON(authToken, "AccountService.querySharingRights", "accid",accountId);
            
            // No shares, return empty result.  Otherwise attempt to access explodes below.
            if(obj.isNull("result")) 
                return results;
            
            JSONObject practices = obj.getJSONObject("result"); 
            for (Iterator iter = practices.keys(); iter.hasNext();) {
                String practiceId = (String)iter.next();
                log.debug("Found practice " + practiceId + " returned in result");
                JSONObject p = practices.getJSONObject(practiceId);
                JSONArray accounts = p.getJSONArray("accounts");
                String practiceName = p.isNull("practiceName") ? null :  p.getString("practiceName");
                String groupAcctId = p.isNull("groupAcctId") ? null : p.getString("groupAcctId");
                AccountShare share = new AccountShare(practiceId, practiceName, groupAcctId,p.getString("access"));
                
                if(!p.isNull("es_identity_type"))
                    share.setIdentityType(p.getString("es_identity_type"));
                
                if(!p.isNull("application_token"))
                    share.setApplicationToken(p.getString("application_token"));
                
                if(!p.isNull("es_id"))
                    share.setEsId(p.getLong("es_id"));
                
                for(int i =0; i<accounts.length(); ++i) {
                    JSONObject r = accounts.getJSONObject(i);
                    share.getAccounts().add(  
                        new AccountHolderRight(r.getString("mcid"),  
                                        r.getString("access"),
                                        r.isNull("first_name") ? null : r.getString("first_name"), 
                                        r.isNull("last_name") ? null :  r.getString("last_name"),  
                                        r.isNull("email") ? null : r.getString("email"),
                                        r.isNull("es_id") ? null : r.getLong("es_id"),
                                        r.isNull("es_identity_type") ? null : r.getString("es_identity_type"),
                                        r.isNull("es_create_date_time") ? 0 : r.getLong("es_create_date_time")
                                        ));
                }
                
                results.add( share );
            }
            return results;
        }
        catch (RESTException e) {
            log.error("Failed retrieving account shares: ",e);
            throw new ServiceException("Unable to query sharing rights for account " + accountId,e);
        }
    }

    public void updateSharingRights(String storageId, List<AccountHolderRight> update) throws ServiceException {
        ArrayList<String> params= new ArrayList<String>();
        params.add("accid");
        params.add(storageId);
        try {
            for(AccountHolderRight ahr : update) {
                params.add(URLEncoder.encode(ahr.getAccountId(),"ASCII"));
                params.add(ahr.getRights());
            }
            
            RESTUtil.callJSON(authToken, "AccountService.updateSharingRights", params.toArray(new String[params.size()]));
        }
        catch (RESTException e) {
            throw new ServiceException("Unable to update access rights for account " + storageId,e);
        }
        catch (UnsupportedEncodingException e) {
            throw new ServiceException("Unable to update access rights for account " + storageId,e);
        }
    }
    
    public void removeAccountDocument(String accountId, AccountDocumentType documentType) throws ServiceException {
            try {
                RESTUtil.call(authToken, "AccountService.removeAccountDocument", "accid", accountId, "documentType", documentType.name());
            }
            catch (RESTException e) {
                throw new ServiceException("Unable to remove account documents of type " + documentType + " for account " + accountId,e);
                
            }
    }

    public void updateWorkflow(String key, String srcAccountId, String patientAccountId, String workflowType, String status) throws ServiceException {
            try {
                RESTUtil.call(authToken, "AccountService.wsUpdateWorkflow", 
                                "src_accid", srcAccountId , 
                                "target_accid", patientAccountId, 
                                "type", workflowType , 
                                "status", status, 
                                "key", key 
                              );
            }
            catch (RESTException e) {
                throw new ServiceException("Unable to update workflow for account " + srcAccountId + " for patient / target " + patientAccountId + " of type " + workflowType + " with status " + status,e);
            }
    }
    
    public Application queryApplicationInfo(String applicationToken) throws ServiceException {
        try {
            JSONObject obj = RESTUtil.callJSON(authToken, "AccountService.queryApplicationInfo", "at",applicationToken);
            JSONObject result = obj.getJSONObject("result");
            
            String createDateTime = result.getString("ea_create_date_time");
            Date d = null;
            if(!blank(createDateTime)) {
                SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                d = f.parse(createDateTime);
            }
            
            String email = result.isNull("ea_contact_email")?null:result.getString("ea_contact_email");
            String url = result.isNull("ea_web_site_url") ? null:result.getString("ea_web_site_url");
            return new Application(result.getString("ea_name"), result.getString("ea_key"), result.getString("ea_code"), 
                            email, url, d, result.getString("ea_ip_address"));
            
        }
        catch (RESTException e) {
            throw new ServiceException("Failed to query application info for token " + applicationToken, e);
        }
        catch (ParseException e) {
            throw new ServiceException("Failed to query application info for token " + applicationToken, e);
        }
    }

    public void shareVoucher(String accid, Voucher voucher) throws ServiceException {
        try {
            JSONObject obj = RESTUtil.callJSON(authToken, "AccountService.shareVoucher", "accid", accid, "couponum", String.valueOf(voucher.getCouponNum()));
        }
        catch (RESTException e) {
            throw new ServiceException("Unable to share voucher " + voucher,e);
        }
    }

    public String createPhoneAccessCode(String phoneNumber, String carrier, String accessTo) throws ServiceException {
        try {
            JSONObject obj = RESTUtil.callJSON(authToken, "AccountService.createPhoneAccessCode", "phoneNumber", phoneNumber, "carrier", carrier, "accessTo", accessTo);
            return obj.getString("result");
        }
        catch (RESTException e) {
            throw new ServiceException("Unable to create access code for phone number " + phoneNumber,e);
        }
    }

    @Override
    public List<Feature> queryFeatures() throws ServiceException {
        try {
            JSONObject obj = RESTUtil.callJSON(authToken, "AccountService.queryFeatures");
            JSONArray objs = obj.getJSONArray("result");
            
            List<Feature> results = new ArrayList<Feature>(objs.length());
            for(int i =0; i< objs.length(); ++i) {
                results.add(Feature.fromJSON(objs.getJSONObject(i)));
            }
            
            return results;
        }
        catch (RESTException e) {
            throw new ServiceException("Unable to query enabled features",e);
        }
    }
}
