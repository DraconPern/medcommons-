package net.medcommons.emcbridge;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.SimpleMessage;
import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.controller.StripesConstants;
import net.sourceforge.stripes.exception.ExceptionHandler;

import org.apache.log4j.Logger;


public class BridgeExceptionHandler implements ExceptionHandler {
	/**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger(BridgeExceptionHandler.class.getName());

    /** Doesn't have to do anything... */
    public void init(Configuration configuration) throws Exception { }

    /** Do something a bit more complicated that just going to a view. */
    public void handle(Throwable throwable,
                       HttpServletRequest request,
                       HttpServletResponse response) throws ServletException, IOException {
    		log.error("Exception caught by bridge", throwable);
    		 ActionBean bean = (ActionBean)  request.getAttribute(StripesConstants.REQ_ATTR_ACTION_BEAN);
    		 request.setAttribute("exception", throwable);
    		 if (bean!= null){
    			 bean.getContext().getMessages().add(new SimpleMessage("BridgeExceptionhandler:" + throwable.getLocalizedMessage()));
    		 }
 			request.getRequestDispatcher("/emcbridgeError.jsp").forward(request, response);
    		 try{
    			 /*
	    		if (bean != null){
	    			 bean.getContext().getValidationErrors().addGlobalError(
	    			            new SimpleError("You made something blow up!  Bad user!"));
	    			
	    			 bean.getContext().getSourcePageResolution().execute(request, response);
	    		}
	    		else{
	    			request.setAttribute("exception", throwable);
	    			request.getRequestDispatcher("/emcbridgeError.jsp").forward(request, response);
	    		}
	    		*/
    		 }
    		 catch(Exception e){
    			 log.error("Unhandled error", e);
    		 }
       
    }
}