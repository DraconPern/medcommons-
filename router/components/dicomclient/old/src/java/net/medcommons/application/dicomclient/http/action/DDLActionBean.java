package net.medcommons.application.dicomclient.http.action;






import org.apache.log4j.Logger;

import net.medcommons.application.dicomclient.ContextManager;
import net.medcommons.application.dicomclient.http.utils.ResponseWrapper;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;

/**
 * Base DDL action
 */
public class DDLActionBean implements ActionBean {
    private ActionBeanContext context;
    private static Logger logger = Logger.getLogger(DDLActionBean.class);


    public ActionBeanContext getContext() { return context; }
    public void setContext(ActionBeanContext context) { this.context = context; }

    public ContextManager getContextManager(){
    	return(ContextManager.getContextManager());
    }

    public ResponseWrapper generateErrorResponse(String message, String content){
    	ResponseWrapper error = new ResponseWrapper();
    	error.setStatus(ResponseWrapper.Status.ERROR);
    	error.setMessage(message);
    	error.setContents(content);
    	return(error);
    }


    @DefaultHandler
    public Resolution addition() {

        return new ForwardResolution("/configure.html");
    }
}