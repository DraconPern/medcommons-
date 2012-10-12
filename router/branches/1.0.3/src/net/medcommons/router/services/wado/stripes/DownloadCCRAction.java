/*
 * $Id: DownloadCCRAction.java 3736 2010-06-03 11:21:01Z ssadedin $
 */
package net.medcommons.router.services.wado.stripes;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import net.medcommons.router.services.wado.CCROperationException;
import net.medcommons.router.web.stripes.CCRActionBean;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;

import org.apache.log4j.Logger;

/**
 * Sends back the specified CCR in XML form either directly as a response
 * or as a download.
 */
public class DownloadCCRAction extends CCRActionBean {

  /**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger(DownloadCCRAction.class);
	
	
	boolean inline = false;

	@DefaultHandler
	public Resolution execute() throws Exception { 
	    
        if(ccr == null) 
            throw new CCROperationException("CCR specified for download could not be located");
        
        byte [] ccrBytes = ccr.getXml().getBytes("UTF8");
        
        String patientName = (ccr.getPatientGivenName() + "_" + ccr.getPatientFamilyName()).replaceAll("[^A-Za-z]", "_");
        
        SimpleDateFormat format = new SimpleDateFormat("MM_dd_yyyy_HH_mm_ss");
        String fileName = patientName + "_"+format.format(new Date()) + ".xml";
        
        HttpServletResponse response = ctx.getResponse();
        if(!inline) {
	        response.setContentType("application/octet-stream");
	        response.addHeader("Content-Disposition", "attachment; filename="+fileName);
        }
        else {
	        response.setContentType("text/xml");
        }
        response.setContentLength(ccrBytes.length);                
        response.getOutputStream().write(ccrBytes);
        
        return null;
	}

    public boolean getInline() {
        return inline;
    }

    public void setInline(boolean inline) {
        this.inline = inline;
    }
}
    

