/*
 * $Id$
 * Created on 19/07/2007
 */
package net.medcommons.router.services.wado.stripes;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.configuration.ConfigurationException;
import net.medcommons.modules.services.client.rest.RESTProxyServicesFactory;
import net.medcommons.modules.utils.TestDataConstants;
import net.medcommons.router.selftest.SelfTest;
import net.medcommons.router.selftest.SelfTestFailure;
import net.medcommons.router.selftest.SelfTestResult;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;

import org.apache.log4j.Logger;

/**
 * Performs a self-test of the gateway by iterating over registered tests and displaying
 * a result page. 
 * 
 * @author ssadedin
 */
public class SelfTestAction implements ActionBean {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(SelfTestAction.class);
    
    private ActionBeanContext ctx = null;
    
    private List<SelfTestResult> results = new ArrayList<SelfTestResult>();
    
    private static List< HashMap<String, SelfTest> > selfTests = new ArrayList< HashMap<String, SelfTest> >();
    
    private  String acDomain = null;
    
   
    @DefaultHandler
    public Resolution exec() {
       HttpServletResponse response = getContext().getResponse();
       HttpServletRequest request = getContext().getRequest();
       
        try{
            acDomain =  Configuration.getProperty("acDomain");
        }
        catch(ConfigurationException e){
            acDomain = "Unknown acDomain:" + e.getLocalizedMessage();
                
        }
        log.info("acDomain is " + acDomain);
 
        synchronized(selfTests) {  // let's avoid two people running tests at same time
            for(Map<String, SelfTest> tests : selfTests) {
                
                if(tests == null)
                    continue;
                
                for(String n : tests.keySet()) {
                    log.info("Executing Self Test " + n);
                    SelfTest t = tests.get(n);
                    doTest(t, n);
                }
            }
        }
        if ("xml".equals(request.getParameter("fmt"))) {
            response.setContentType("text/xml");
            return new ForwardResolution("/selfTestReport-xml.jsp");
        } 
         else
            return new ForwardResolution("/selfTestReport.jsp");
        
    }

    /**
     * Perform the given test, capturing results in the results list
     * 
     * @param t - test to perform
     * @param n - name of the test
     */
    private void doTest(SelfTest t, String n) {
        SelfTestResult r = null;
        try {
            long startTime = System.currentTimeMillis();
            r = t.execute(new RESTProxyServicesFactory("token:"+TestDataConstants.DOCTOR_AUTH));
            long timeMs = System.currentTimeMillis() - startTime;
            if(r == null) {
                r = new SelfTestResult();
                r.setStatus("OK"); 
            }
            r.setName(n);
            r.setTimeMs(timeMs);
            log.info("Self Test " + n + " passed with result " + r);
        }
        catch (Throwable ex) {
            log.error("Self test " + n + " failed", ex);
            r = new SelfTestFailure("Exception thrown during test: " + ex.toString());
            r.setName(n);
            r.setException(ex);
        }
        this.results.add(r);
        
        if("Failed".equals(r.getStatus())) {
            // Load tips, if there are any
            try {
                InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(t.getClass().getName().replaceAll("\\.", "/")+".html");
                if(inputStream != null) {
                    StringBuffer tip = new StringBuffer();
                    byte[] buffer = new byte[4096];
                    int read = -1; 
                    while ((read = inputStream.read(buffer)) >= 0) {
                        tip.append(new String(buffer,0,read));
                    }
                    inputStream.close();
                    r.setTips(tip.toString());
                }
            }
            catch (IOException e) {
                log.debug("No tips loaded for failed self test " + n);
            }                                
        }
    }

    public ActionBeanContext getContext() {
        return ctx;
    }

    public void setContext(ActionBeanContext context) {
        this.ctx = context;
    }
    
    /**
     * Register a self test under it's class name.  A simple transformation
     * is performed on the class name to make it more readable.
     * 
     * @param t
     */
    public static void register(int group,SelfTest t) {
       register(group, t.getClass().getName().replaceAll("^.*\\.", "").replaceAll("([a-z])([A-Z])", "$1 $2"),t); 
    }
    
    /**
     * Register a self test under it's name
     * kk
     * @param t
     */
    public static void register(int group,String name, SelfTest t) {
        log.info("Adding Self Test " + name + " with class " + t.getClass().getCanonicalName());
        while(group>=selfTests.size()) {
            selfTests.add(new HashMap<String, SelfTest>());
        }
        selfTests.get(group).put(name, t);
    }

    public List<SelfTestResult> getResults() {
        return results;
    }

    public void setResults(List<SelfTestResult> results) {
        this.results = results;
    }
    public String getAcDomain(){
        return(acDomain);
    }
    public void setAcDomain(String s){
        acDomain = s;
    }
}
