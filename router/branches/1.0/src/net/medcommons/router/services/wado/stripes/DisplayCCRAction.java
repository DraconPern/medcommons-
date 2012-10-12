/*
 * $Id$
 */
package net.medcommons.router.services.wado.stripes;

import static net.medcommons.modules.utils.Str.blank;
import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamSource;

import net.medcommons.phr.PHRException;
import net.medcommons.phr.db.xml.XMLPHRDocument;
import net.medcommons.router.services.dicom.util.MCSeries;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;
import net.medcommons.router.web.stripes.CCRActionBean;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jdom.ProcessingInstruction;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.transform.JDOMResult;
import org.jdom.transform.JDOMSource;

/**
 * Displays a requested CCR.
 * 
 * This action supports both client side and server side stylesheet
 * rendering.  Originally client side was used but many inconsistent problems
 * were found across browsers and also generally poor error handling for
 * invalid content.  Hence the default is now server side, but you can 
 * reinstate client side by passing a "client=true" parameter.  This might
 * be possible when strict validation is done on CCR content.
 * <p> 
 * By default this action displays the CCR specified by a supplied parameter
 * "ccrIndex".  This parameter is required.  
 * <p>
 * It is possible, however, to display a CCR that is attached to the indicated
 * CCR by passing a parameter "guid=... the guid ...".  Note that you can't request
 * any guid, ONLY a guid that is associated with the specified CCR (ccrIndex)
 * will succeed.
 */
public class DisplayCCRAction extends CCRActionBean {

    /**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger(DisplayCCRAction.class);
	
	private String guid;
	
	private String client;
	
	public DisplayCCRAction() {
	    // Stop cache headers being sent automatically
	    this.cacheable = true;
	}

	@DefaultHandler
	public Resolution display() throws Exception { 
	    
        // Is there a guid param?  If so, find it in the user's CCR's
        if(!blank(guid)) {
            if(!guid.equals(ccr.getGuid())) {                
                // Check the series belonging to the current CCR
                for (MCSeries series : ccr.getSeriesList()) {
                    if(guid.equals(series.getFirstInstance().getSOPInstanceUID())) {
                        if(CCRDocument.CCR_MIME_TYPE.equals(series.getMimeType())) {
                            ccr = (CCRDocument)series.getFirstInstance().getDocument();
                        }
                    }
                }
            }            
        }
        if(ccr == null) 
            throw new NullPointerException("Can not display null ccr (specified guid = '" + guid + "')");
        
        log.info("displaying CCR [specified guid = " +guid+ " default guid = " + ccr.getGuid() + "]");
        
        
        // Client side processing - just add our style sheet and send back the results
        HttpServletResponse response = ctx.getResponse();
        if("true".equals(client)) {
            // We do not modify the original
            // Copying the whole CCR probably makes it almost as expensive as doing the 
            // rendering ourselves .... is there a better way?
            
            CCRDocument ccrCopy = ccr.copy();
            XMLPHRDocument doc = ccrCopy.getJDOMDocument();
            HashMap params = new HashMap();
            params.put("type", "text/xsl");
            params.put("href", "/router/stylesheets/ccr2htm.xsl");
            ProcessingInstruction pi = new ProcessingInstruction("xml-stylesheet", params);
            doc.getContent().add(0,pi);
            ccrCopy.syncFromJDom();
            
            response.setHeader("Cache-Control","no-cache"); // HTTP 1.1
            response.setHeader("Pragma","no-cache"); // HTTP 1.0
            response.setContentType("text/xml");
            response.getOutputStream().print(ccrCopy.getXml());
            return null;
        }
        
        try {
	        Format utfOutputFormat = Format.getPrettyFormat();
	        utfOutputFormat.setEncoding("UTF-8");
	        
	        // This is a hack to try and help a specific version of IE that cannot accept compressed CCRs
	        boolean hackForceNoCompression = false;
	        String userAgent = ctx.getRequest().getHeader("User-Agent");
	        log.info("Transforming for user agent " + userAgent);
	        if(userAgent != null && userAgent.indexOf("MSIE 7.0; Windows NT 5.2; Data Center;") >=0) {
	            hackForceNoCompression = true;
	        }
	        
	        if(blank(ccr.getGuid()) || ccr.getJDOMDocument().getModified() || hackForceNoCompression) {
                response.setHeader("Cache-Control","no-cache"); // HTTP 1.1
                response.setHeader("Pragma","no-cache"); // HTTP 1.0
	            
                if(hackForceNoCompression)
                    log.info("Transforming fresh due to User-Agent match of incompatible browser");
                else
                    log.info("Transforming unsaved or modified CCR (guid = " + ccr.getGuid() +")");
                
	            response.setContentType("text/html; charset=UTF-8"); 
		        JDOMResult out = doTransform();
		        new XMLOutputter(utfOutputFormat).output(out.getDocument(), response.getOutputStream());
		        return null;
	        }
	        else {
	            // cache the HTML
		        String fileName = "data/repository/"+ccr.getStorageId()+"/"+ccr.getGuid()+".html.gz";
		        File htmlFile = new File(fileName);
		        if(!htmlFile.exists()) {
		            log.info("Transforming saved CCR");
		            if(!htmlFile.getParentFile().exists())
		                htmlFile.getParentFile().mkdirs();
		            
		            JDOMResult out = doTransform();
		            GZIPOutputStream outputStream = new GZIPOutputStream(new FileOutputStream(fileName));
		            try {
	                    new XMLOutputter(utfOutputFormat).output(out.getDocument(), outputStream);
	                    outputStream.flush();
		            }
		            finally {
	                    closeQuietly(outputStream);
		            }
		        }
		        else
		            log.info("Rendering from cached transfrom of CCR " + htmlFile.getAbsolutePath());
		        
                response.setHeader("Pragma","public"); // HTTP 1.0
                response.setHeader("Cache-Control","max-age=0"); // HTTP 1.1
	            response.setHeader("Content-Encoding", "gzip");
	            response.setContentType("text/html; charset=UTF-8"); 
	            
	            ServletOutputStream out = response.getOutputStream();
	            out.flush();
	            
                FileInputStream is = new FileInputStream(fileName);
                try {
	                IOUtils.copy(is, out);
                }
                finally {
	                closeQuietly(is);
                }
                
                out.flush();
	            
	            return null;
	        }
        }
        catch(Exception e) {
            log.error("Failed to transform or render CCR guid = " + ccr.getGuid(),e);
            throw e;
        }
	}

    /**
     * @return
     * @throws TransformerConfigurationException
     * @throws TransformerFactoryConfigurationError
     * @throws PHRException
     * @throws TransformerException
     */
    private JDOMResult doTransform() throws TransformerConfigurationException, TransformerFactoryConfigurationError,
                    PHRException, TransformerException {
        log.info("Creating transformer");
        StreamSource source = null;
        source = new StreamSource(new File("data/stylesheets/ccr2htm.xsl"));
        Transformer transformer = 
            TransformerFactory.newInstance().newTransformer(source);
        
        JDOMSource in = new JDOMSource(ccr.getJDOMDocument());
        JDOMResult out = new JDOMResult();
        transformer.setParameter("stylesheet","ccr.css");
        transformer.transform(in, out);
        return out;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }
}
    

