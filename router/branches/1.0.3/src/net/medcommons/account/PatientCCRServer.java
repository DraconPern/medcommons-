/*
 *  
 */
package net.medcommons.account;
/**
 * SOAP server for Metatadata queries
 */
import java.util.Date;

import net.medcommons.Version;
import net.medcommons.cxp.CXPConstants;

import org.apache.log4j.Logger;

public class PatientCCRServer {
	private static Logger log = Logger.getLogger(PatientCCRServer.class);
	
	 /**
     * Returns the version of the server
     */
    public String getVersion() {
        return "MedCommons Patient CCR Server " + Version.getVersionString() + " built on " + Version.getBuildTime();
    }
    
    
    public MetadataResponse setEmergencyCCR(String accid, String guid){
    	MetadataResponse response = new MetadataResponse();
    	response.setStatus(500);
    	response.setReason("Not yet implemented");
    	return(response);
    }
    
    /**
     * Returns the set of CCRs that match the specified account id.
     * @param accid
     * @return
     */
   public MetadataResponse queryByAccid(String accid){ 
	   log.info("getCCRInfo:" + accid);
	   MetadataResponse response = new MetadataResponse();
	   
	   CCRInfo results[] = null;
	   try{
		   log.info("About to create new CCRLog");
		   CCRLog ccrlog = new CCRLog();
		   log.info("ccrlog is " + ccrlog);
		  try{
			   long nAccid = Long.parseLong(accid);
		   }
		  catch(NumberFormatException e){
			  response.setCcrResults(null);
			  response.setStatus(CXPConstants.CXP_STATUS_BAD_REQUEST);
			  response.setReason("CommonsIds are integer values, not '" + accid + "'");
			  return(response);
		  }
		   results = ccrlog.queryAcctid(accid);
		   if (results == null){
			   response.setCcrResults(null);
			   response.setReason("No matches for this account");
			   response.setStatus(CXPConstants.CXP_STATUS_SUCCESS);
		   }
		   else{
			   log.info("Results are " + results + ", length = " + results.length);
			   response.setCcrResults(results);
			   response.setReason("OK");
			   response.setStatus(CXPConstants.CXP_STATUS_SUCCESS);
		   }
		   
	   }
	   catch(Exception e){
		   response.setReason(e.toString());
		   response.setStatus(500);
		   log.info("id=" + accid, e);
	   }
	   catch(Throwable t){
		   response.setReason(t.toString());
		   response.setStatus(500);
		   log.info("id=" + accid, t);
	   }
	   
	   return(response);
   }
   /**
    * Returns set of CCRs taht match the specified account id after the specified date.
    * @param accid
    * @param d
    * @return
    */
   public MetadataResponse QueryByAccidDate(String accid, Date d){ 
	   log.info("getCCRInfo:" + accid);
	   MetadataResponse response = new MetadataResponse();
	   
	   CCRInfo results[] = null;
	   try{
		   log.info("About to create new CCRLog");
		   CCRLog ccrlog = new CCRLog();
		   log.info("ccrlog is " + ccrlog);
		   results = ccrlog.queryAcctid(accid);
		   log.info("Results are " + results + ", length = " + results.length);
		   response.setCcrResults(results);
		   response.setReason("OK");
		   response.setStatus(CXPConstants.CXP_STATUS_SUCCESS);
		   
	   }
	   catch(Exception e){
		   response.setReason(e.toString());
		   response.setStatus(CXPConstants.CXP_STATUS_SERVER_ERROR);
		   log.info("id=" + accid, e);
	   }
	   catch(Throwable t){
		   response.setReason(t.toString());
		   response.setStatus(CXPConstants.CXP_STATUS_SERVER_ERROR);
		   log.info("id=" + accid, t);
	   }
	   
	   return(response);
   }

}
