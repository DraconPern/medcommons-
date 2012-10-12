package net.medcommons.modules.publicapi.utils;


import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.medcommons.modules.utils.Str;

import org.apache.commons.fileupload.FileItem;
import org.apache.log4j.Logger;

/**
 * Placeholder class for parameters; least common denominator 
 * tests for parameter validity.
 * 
 * Doesn't contain any logic for payment testing since this requires 
 * document-type specific routines.
 * 
 * @author sean
 *
 */
public class AccountDocumentParameters {
    public String storageid;
    public String accountid;
    public String auth;
   
    public final static String STORAGEID = "storageid";
    public final static String AUTH = "auth";
    public final static String ACCOUNTID = "accountid";
    Map<String, String> documentParameters = new HashMap<String, String>();
    
    private static Logger log = Logger.getLogger(AccountDocumentParameters.class);
    
    public AccountDocumentParameters(String storageid, String accountid, String auth) {
        super();
        this.storageid = storageid;
        this.accountid = accountid;
        this.auth = auth;
    }

    public AccountDocumentParameters() {
    }
    
    public void addParameter(FileItem item){
       
        
            String name = item.getFieldName();
            String value = item.getString();
            
       
            log.info("parameter name=" + name + ", value=" + value);
            if (ACCOUNTID.equalsIgnoreCase(name)){
                accountid = value;
            }
            else if (STORAGEID.equalsIgnoreCase(name)){
                storageid = value;
            }
            else if (AUTH.equalsIgnoreCase(name)){
                auth = value;
            }
            else{
                documentParameters.put(name, value);
            }
           
        
    }
    public boolean canWriteToAccount(){
        boolean canWrite = false;
        return(canWrite);
    }
    
    public boolean canReadFromAccount(){
        boolean canRead = false;
        return(canRead);
    }
    
    public boolean requiredParametersValid(){
        boolean parametersValid = false;
       
        if (!Str.blank(storageid) && !Str.blank(accountid) && !Str.blank(auth)){
            parametersValid = true;
        }
        if (!parametersValid){
           if (Str.blank(storageid)) log.info("Blank storageid");
           if (Str.blank(accountid)) log.info("Blank accountid");
           if (Str.blank(auth)) log.info("Blank auth");
        }
        return(parametersValid);
    }
    
}
