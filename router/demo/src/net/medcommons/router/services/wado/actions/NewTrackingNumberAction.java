/*
 * $Id: NewTrackingNumberAction.java 3132 2008-12-09 06:42:45Z ssadedin $
 */
package net.medcommons.router.services.wado.actions;

import static net.medcommons.modules.utils.Str.escapeForJavaScript;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.medcommons.modules.services.interfaces.TrackingService;
import net.medcommons.modules.xml.XPathUtils;
import net.medcommons.router.services.UserSession;
import net.medcommons.router.services.dicom.util.DICOMUtils;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 * Allocates a new tracking number and assigns it to the current Reply CCR,
 * also returning it as the response.
 * 
 * Requires an input "src" specifying an existing CCR to copy to create the
 * new tracking number.
 * 
 * Accepts a "display" parameter which determines whether the CCR is 
 * rendered in the output stream or whether instead just the tracking number
 * is displayed (useful for ajax request).
 * 
 * @author ssadedin */
public class NewTrackingNumberAction extends Action {

    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(NewTrackingNumberAction.class);

    /**
     * Method execute
     * 
     * @return ActionForward
     * @throws
     * @throws ConfigurationException -
     *             if configuration cannot be accessed
     * @throws SelectionException -
     *             if a problem scanning the selections occurs
     */
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                    HttpServletResponse response) throws Exception {
        
        // Whether the result will be displayed in browser or not
        boolean displayResult = "true".equals(request.getParameter("display"));
        
        try {
            log.info("NewTrackingNumberAction"); 
            UserSession desktop = UserSession.required(request);
            
            // Get a new tracking number
            TrackingService trackingService = desktop.getServicesFactory().getTrackingService();
            String trackingNumber = trackingService.allocateTrackingNumber();              
            
            log.info("Allocated new tracking number " + trackingNumber);
            
            int srcIndex = Integer.parseInt(request.getParameter("ccrIndex"));
            
            // Get the CCR to copy
            CCRDocument srcCcr = desktop.getCcrs().get(srcIndex);
            srcCcr.syncFromJDom();
            CCRDocument ccr = srcCcr.copy(); 
            ccr.setGuid(null); // new ccr is not fixed content, but we may have copied the guid
            
            // Update the date/time
            XPathUtils.setValue(ccr.getJDOMDocument(),"ccrDateTime", DICOMUtils.formatDate(System.currentTimeMillis()));
            
            desktop.getCcrs().add(ccr);
            int ccrIndex = desktop.getCcrs().indexOf(ccr);
            request.setAttribute("ccrIndex",String.valueOf(ccrIndex));
            
            // we want actions that are forwarded to to update the new CCR, not the original
            request.setAttribute("updateIndex",String.valueOf(ccrIndex)); 
            
            // Get the CCR to copy
            ccr.setTrackingNumber(trackingNumber);
            log.debug("Created copy CCR " + ccr.hashCode() + " for tracking number " + trackingNumber);
            
            // If display parameter is set then show the CCR in the response, otherwise just return data
            // (ajax request).
            if(displayResult) 
                return mapping.findForward("success");   
            
            // Display flag not set, treat as simple ajax request
            // Note must manually set no-cache headers
            response.setHeader("Cache-Control","no-cache"); // HTTP 1.1
            response.setHeader("Pragma","no-cache"); // HTTP 1.0
            
            log.info("successfully allocated tracking number");
            
            // Return javascript object
            response.getOutputStream().print("{status: 'ok', ccrIndex:"+ccrIndex+",tn:'"+trackingNumber+"'}");
        }
        catch(Exception e) {
           log.error("Unable to allocate new tracking number",e);
           if(displayResult)
               throw e;
           else
               response.getOutputStream().print("{status: 'failed', error: '"+escapeForJavaScript(e.getMessage())+"'}");
        }
        return null;
    }
}