/**
 * $Id: StartDDLAction.java 3902 2010-10-28 06:16:03Z ssadedin $
 */
package net.medcommons.router.services.wado.stripes;

import static net.medcommons.modules.utils.Str.blank;

import java.io.FileReader;
import java.io.StringReader;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.configuration.ConfigurationException;
import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.modules.utils.Str;
import net.medcommons.router.messaging.MessageService;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;
import net.medcommons.router.web.stripes.BaseActionBean;
import net.medcommons.router.web.stripes.JSON;
import net.medcommons.router.web.stripes.JSONResolution;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidationErrorHandler;
import net.sourceforge.stripes.validation.ValidationErrors;

import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.filter.Filter;
import org.jdom.input.SAXBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A generic action for starting the DDL and sending a command to it.
 * 
 * @author ssadedin
 */
@SuppressWarnings("unchecked")
@UrlBinding("/ddl/{ddl.command}")
public class StartDDLAction extends BaseActionBean implements ValidationErrorHandler {
    
    /**
     * Predefined MIME type for Java Web Start
     */
    public final static  String JNLP_MIME_TYPE = "application/x-java-jnlp-file";
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(StartDDLAction.class);
    
    /**
     * Parameters that will be passed to DDL upon startup
     */
    private ParameterBlock ddl = new ParameterBlock();
    
    /**
     * If set then no attempt to set shortcuts or extensions will be made
     */
    private boolean quiet = true;
    
    /**
     * Create new StartDDLAction - set cacheable to true
     */
    public StartDDLAction() {
        // Prevent sending of cache headers
        // Note: we will send our own below - see notes 
        cacheable = true;
    }
    
    /**
     * Returns an JNLP XML stream that will invoke webstart on the
     * client configured with the parameters passed.
     */
    @DefaultHandler
    public Resolution startDDL(){
        try {
            initializeParams();
           
            SAXBuilder builder = new SAXBuilder();
            FileReader reader = new FileReader(CCRDocument.TEMPLATE_PATH+"/ddlTemplate.xml");
            Document jnlpDoc = builder.build(reader);
            editDDLTemplate(jnlpDoc, ddl);
            String jnlpXML = Str.toString(jnlpDoc);
            
            ctx.getResponse().reset();
            // This unusual way of preventing caching is a workaround
            // for bugs in IE whereby it strictly interprets 'no-cache' under
            // SSL and refuses to save files to disk.  Since JNLP requires the actual
            // file to be saved in cache, it causes a download error.   So we
            // use this different way of preventing caching instead.
            ctx.getResponse().setHeader("Pragma","public"); // HTTP 1.0
            ctx.getResponse().setHeader("Cache-Control","max-age=0"); // HTTP 1.1
            ctx.getResponse().addHeader("Content-Disposition", "Inline; fileName=medcommons_utility.jnlp");
            ctx.getResponse().setHeader("Content-Type", JNLP_MIME_TYPE);
            
            IOUtils.copy(new StringReader(jnlpXML), ctx.getResponse().getOutputStream());
            // return new StreamingResolution(JNLP_MIME_TYPE, jnlpXML);
            return null;
        }
        catch(Exception e){
            log.error("Error processing request", e);
            return new StreamingResolution("text/plain",e.getLocalizedMessage());
        }
    }
   
   /**
    * DDL ID for command being added
    */
   @Validate(on= {"command","poll","result"}, required=true, mask=GUID_PATTERN)
   private String ddlid;
   
   
        
   /**
    * Optional parameter for providing callback notification
    */
   private String jsonp; 
    
   /**
    * Add a command for a DDL to execute
    * @return
 * @throws ServiceException 
    */
   @JSON
   public Resolution command() throws Exception {
        
        // Because caching is disabled by default for this action, we have to do it here manually
        nocache();
        
       MessageService messages = Configuration.getBean("messageService");
       
       JSONObject cmd = new JSONObject();
       Map<String,String[]> params = ctx.getRequest().getParameterMap();
       for(String key : (Collection<String>)params.keySet()) {
           String[] values = params.get(key);
           if(values.length>0)  {
               cmd.put(key, values[0]);
           }
       }
       log.info("Received command " + cmd + " for ddl " + ddlid);
       
       messages.send("ddl:"+ddlid, cmd);
               
       jsonp = Str.bvl(jsonp, "");
       
       return new StreamingResolution("text/json", jsonp+"({status:'ok'})"); 
   }
    
    /**
     * Handle a poll for commands from a DDL
     */
    @JSON
    public Resolution poll() {
        
        log.info("Poll by ddl " + ddlid);
        
        MessageService messages = Configuration.getBean("messageService");
        
        JSONArray results = new JSONArray();
        try {
            // Is there a command waiting for this ddl?
           for(int i=0; i<3; ++i) {
               List<JSONObject> cmds = messages.read("ddl:"+ddlid, 10000);
               if(cmds != null) {
                   for(JSONObject cmd : cmds) {
                       results.put(cmd);
                   }
                   break;
               }
               else {
                   ctx.getResponse().getWriter().append(' ');
                   ctx.getResponse().getWriter().flush();
               }
            }
            
            return new JSONResolution().put("commands", results);
        }
        catch(Exception e) {
            log.error("Poll for commands failed", e);
            return new JSONResolution(e);
        }
    }
    
    @JSON
    public Resolution test() {
        return new JSONResolution();
    }
    
    /**
     * Results posted by previous DDLs
     */
    static Map<String, DDLResult > results = Collections.synchronizedMap(new LRUMap(200));
    
    /**
     * The command for which results were posted
     */
    @Validate(on="result,getResult", required=true, maxlength=120)
    String cmd;
    
    /**
     * Results posted by a DDL 
     */
    @Validate(on="result", required=true)
    String data;
    
    /**
     * Post a result from a previous command
     * @return
     */
    @JSON
    public Resolution result() {
        
        DDLResult result = new DDLResult(cmd, data);
        
        log.info("Result " + result.sha1 + " posted for command " + cmd + " with contents " + data);
        
        results.put(result.sha1, result);
        
        return new JSONResolution();
    }
    
    @Validate(on="getResult", required=true, mask=GUID_PATTERN)
    String key;
    
    @JSON
    public Resolution getResult() {
        DDLResult result = results.get(key);
        if(result == null) 
            throw new IllegalArgumentException("No result registered with key " + key);
        
        if(!result.command.equals(cmd)) {
            log.warn("Result requestd for incorrect command " + cmd + " instead of " + result.command);
            throw new IllegalArgumentException("Result " + key + " is not for command " + cmd);
        }
            
        return new JSONResolution().put("data", result.data);
    }

    /**
     * Set defaults on the parameter block based on the
     * configuration of the gateway and the user's session.
     * 
     * @throws ConfigurationException
     * @throws ServiceException 
     */
    private void initializeParams() throws ConfigurationException, ServiceException {
        HttpServletRequest request = this.ctx.getRequest();
        ddl.cxphost =  Configuration.getProperty("acDomain").replaceAll(":.*$", "");
        ddl.auth = this.session.getAuthenticationToken();
        ddl.cxpport = (request.getServerPort()==80||request.getServerPort()==443)?"":String.valueOf(request.getServerPort()); 
        ddl.cxpprotocol = request.getScheme();
        ddl.accountid = this.session.getOwnerMedCommonsId(); 
        ddl.groupname = this.session.getAccountSettings().getGroupName();
        ddl.quiet = this.quiet;
        
        if(blank(ddl.aeTitle)) 
            ddl.aeTitle = session.getAccountSettings().getDicomAeTitle();
        
        if(blank(ddl.ipAddress)) 
            ddl.ipAddress = session.getAccountSettings().getDicomHost();
        
        if(ddl.port <= 0)
            ddl.port = session.getAccountSettings().getDicomPort();
    }
    
    private void editDDLTemplate(Document doc, ParameterBlock params) throws ConfigurationException {
         
        String remoteAccessAddress = Configuration.getProperty("RemoteAccessAddress");
        String baseUrl = Configuration.getProperty("AccountsBaseUrl");
        
        Element jnlp = doc.getRootElement(); 
        jnlp.setAttribute(new Attribute("codebase", baseUrl + "DDL/app/"));
        jnlp.setAttribute(new Attribute("href", remoteAccessAddress + "/ddl/" + params.toParameterString()));
        Element resources = jnlp.getChild("resources");
        if (resources== null){
            List<Element> elements = jnlp.getChildren();
            for (int i=0;i<elements.size();i++){
                Element child = elements.get(i);
                log.info("Child:" + child.getName());
            }
        }
        
        resources = jnlp.getChild("application-desc");
        addProperty(resources, "command", params.command);
        addProperty(resources, "command_auth", params.auth);
        addProperty(resources, "command_guid", params.guid);
        addProperty(resources, "command_storageid", params.storageid);
        addProperty(resources, "command_cxphost", params.cxphost);
        addProperty(resources, "command_cxpprotocol", params.cxpprotocol);
        addProperty(resources, "command_cxpport", params.cxpport);
        addProperty(resources, "command_accountid", params.accountid); 
        addProperty(resources, "command_groupname", params.groupname); 
        addProperty(resources, "ddl.configuration", baseUrl + "DDL/app/DDL.properties");
        addProperty(resources, "gatewayRoot", remoteAccessAddress); 
        addProperty(resources, "command_applianceRoot", baseUrl); 
        
        /*
        if(!blank(params.aeTitle))
            replaceProperty(resources, "defaultDICOMRemoteAETitle", params.aeTitle); 
        
        if(!blank(params.ipAddress))
            replaceProperty(resources, "defaultDICOMRemoteHost", params.ipAddress); 
        
        if(params.port > 0)
            replaceProperty(resources, "defaultDICOMRemoteDicomPort", String.valueOf(params.port)); 
            */
        
        if(!blank(params.aeTitle))
            addProperty(resources, "defaultDICOMRemoteAETitle", params.aeTitle); 
        
        if(!blank(params.ipAddress))
            addProperty(resources, "defaultDICOMRemoteHost", params.ipAddress); 
        
        if(params.port > 0)
            addProperty(resources, "defaultDICOMRemoteDicomPort", String.valueOf(params.port)); 
        
        // addProperty(resources, "remoteAeTitle", params.aeTitle); 
        // addProperty(resources, "remoteIpAddress", params.ipAddress); 
        // addProperty(resources, "remotePort", String.valueOf(params.port)); 
        
        if(quiet) {
            jnlp.getChild("information").removeContent(new Filter() {
                @Override
                public boolean matches(Object c) {
                    if(!(c instanceof Element))
                        return false;
                    
                    Element e = (Element)c;
                    if("icon".equals(e.getName()) || "association".equals(e.getName()) || "shortcut".equals(e.getName())) 
                        return true;
                    
                    return false;
                }
            });
        }
    }
    
    private void replaceProperty(Element res, String name, String value) {
        for(Element e : (List<Element>)res.getChildren("property")) {
            if(!name.equals(e.getAttributeValue("name"))) 
                continue;
            e.setAttribute("value", value);
        }
    }
    
    private void addProperty(Element element, String name, String value){
        if(blank(value)) 
            return;
        Element property = new Element("argument");
        property.setText(name+"="+value);
        element.addContent(property);
    }
    
    public Resolution handleValidationErrors(ValidationErrors errors) throws Exception {
        JSONObject result = new JSONObject();
        result.put("status", "failed");
        result.put("error", "invalid input for field " + errors.keySet().iterator().next());
        return new StreamingResolution("text/plain", result.toString());
    }
    
    
    public ParameterBlock getDdl() {
        return ddl;
    }
    public void setDdl(ParameterBlock param) {
        this.ddl = param;
    }

    public boolean isQuiet() {
        return quiet;
    }

    public void setQuiet(boolean quiet) {
        this.quiet = quiet;
    }

    public String getDdlid() {
        return ddlid;
    }

    public void setDdlid(String ddlid) {
        this.ddlid = ddlid;
    }

    public String getJsonp() {
        return jsonp;
    }

    public void setJsonp(String jsonp) {
        this.jsonp = jsonp;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String command) {
        this.cmd = command;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
