//Created by MyEclipse Struts
// XSL source (default): platform:/plugin/com.genuitec.eclipse.cross.easystruts.eclipse_3.7.101/xslt/JavaClass.xsl

package net.medcommons.router.services.wado.actions;

import static net.medcommons.modules.utils.Str.blank;

import java.net.URLDecoder;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.medcommons.modules.services.interfaces.DocumentReference;
import net.medcommons.modules.utils.Str;
import net.medcommons.router.services.UserSession;
import net.medcommons.router.services.ccr.CCRChangeElement;
import net.medcommons.router.services.dicom.util.MCSeries;
import net.medcommons.router.services.repository.DocumentResolver;
import net.medcommons.router.services.wado.NotLoggedInException;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.json.JSONArray;
import org.json.JSONObject;

/** * Views or Edits the current CCR based on the 'mode' parameter
 * <p>
 * This class does some additional prep work that adds context
 * attributes that make a CCR ready for viewing.
 * <p>
 * For this reason it is strongly recommend to forward through 
 * this class when rendering a CCRDocument object.
 */
public class ViewEditCCRAction extends Action {
 
  /**
   * Logger to use with this class
   */
  private static Logger log = Logger.getLogger(ViewEditCCRAction.class);
    
  /**
   * Method execute
   * 
   * @return ActionForward
   * @throws
   * @throws ConfigurationException -
   *           if configuration cannot be accessed
   * @throws SelectionException -
   *           if a problem scanning the selections occurs
   */
  public ActionForward execute(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws Exception {

    log.info("ViewEditCCRAction");
    UserSession desktop = UserSession.required(request);
   
    // If both updateIndex and ccrIndex are set, promote ccrIndex now
    // because we are on the outward side of the transaction (rendering the CCR)
    // rather than the inbound side (updating a CCR).
    if(!blank(request.getParameter("updateIndex")) && !blank(request.getParameter("ccrIndex")) && blank((String)request.getAttribute("ccrIndex"))) {
        request.setAttribute("ccrIndex", request.getParameter("ccrIndex"));
    }
  
    CCRDocument ccr = desktop.getCurrentCcr(request);
    
    if(ccr == null) {
        throw new NotLoggedInException(); // ccr out of range - usually means not logged in
    }
    
    request.setAttribute("ccrIndex",String.valueOf(desktop.getCcrs().indexOf(ccr)));
    request.setAttribute("ccr", ccr); 
    
    // Set the pending notifications if there are any
    JSONArray notifications = new JSONArray();
    List<CCRChangeElement> changeNotifications = ccr.getChangeNotifications();
    if(changeNotifications != null) {
	    for (CCRChangeElement c : changeNotifications) {
	        JSONObject cobj = new JSONObject();
	        cobj.put("location",c.getLocation());
	        cobj.put("operation",c.getOperation());
	        notifications.put(cobj); 
	    }
    }
    String patientId = ccr.getPatientMedCommonsId();
    if(!Str.blank(patientId)) { 
        request.setAttribute("currentCcrGuid", desktop.getAccountSettings(patientId).getCurrentCcrGuid());
        request.setAttribute("patientAccountSettings", desktop.getAccountSettings(patientId));
    }
    else {
        if(desktop.hasAccount()) {
            request.setAttribute("currentCcrGuid", desktop.getAccountSettings().getCurrentCcrGuid());
        }
    }
    
    if(request.getCookies() != null) {
        for(Cookie c: request.getCookies()) {
            if("mctz".equals(c.getName())) {
                request.setAttribute("tz", URLDecoder.decode(c.getValue().replace("+","%2b"),"UTF-8"));
            } 
        }
    }
    
    request.setAttribute("changeNotifications", notifications.toString());
    
    // Check for any series that may require payment.  If they do, initialize them
    DocumentResolver resolver = null;
    for(MCSeries series : ccr.getSeriesList()) {
        if(series.getPaymentRequired() && series.getPendingBillingEvent() == null) {
            if(resolver == null)
                resolver = new DocumentResolver(desktop.getServicesFactory());
            
            DocumentReference ref = resolver.resolveGuid(ccr.getStorageId(), series.getMcGUID());
            if(ref != null && ref.getOutstandingCharge() != null && ref.getOutstandingCharge().getQuantity()>0) {
                series.setPendingBillingEvent(ref.getOutstandingCharge());
            }
            else
                series.setPaymentRequired(false);
        }
    }
    
    if(!blank(ccr.getPatientMedCommonsId()))
        request.setAttribute("patientAccountSettings", desktop.getAccountSettings(ccr.getPatientMedCommonsId()));
    
    if(request.getSession().getAttribute("share")!=null) {
        request.setAttribute("share", request.getSession().getAttribute("share"));
        request.getSession().removeAttribute("share");
    }
    
    request.setAttribute("framed", Boolean.TRUE);
    String mode = request.getParameter("mode");
    if(mode!=null) {
        
        // Special code for deep linking into patient demographics
        if(mode.equals("editdg")) {
            mode = "edit";
            request.setAttribute("showDemographics",true);
        }
        
        ActionForward forward = mapping.findForward(mode);
        if(forward != null) {
            return forward;
        }
        else {
        	log.error("Unknown mode " + mode + " specified for viewer");
        	return mapping.findForward("undefined");
            //throw new IllegalArgumentException("Unknown mode " + mode + " specified for viewer");
        }
    }
    else
        return mapping.findForward("edit");
  }
}