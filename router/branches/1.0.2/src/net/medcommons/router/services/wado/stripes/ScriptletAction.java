/*
 * $Id$
 * Created on 13/04/2007
 */
package net.medcommons.router.services.wado.stripes;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import net.medcommons.router.web.stripes.CCRActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;

public class ScriptletAction extends CCRActionBean {
    
    private String scriptlet = "";
    
    private String scriptletOutput;
    
    private Pattern scriptPattern = Pattern.compile("<\\?(.*?)\\?>", Pattern.DOTALL);
    
    @DefaultHandler
    public Resolution exec() {
        
        if(true)
            throw new RuntimeException("This action is Disabled");
        
        if(scriptlet == null || scriptlet.isEmpty()) {
            scriptlet = (String) this.ctx.getRequest().getSession().getAttribute("scriptlet");
        }
            
        if(scriptlet != null) {
            StringWriter s = new StringWriter();
            PrintWriter out = new PrintWriter(s);
            ScriptEngineManager mgr = new ScriptEngineManager();
            ScriptEngine jsEngine = mgr.getEngineByName("JavaScript");
            try {
                jsEngine.put("out",out);
                jsEngine.put("ccr",this.ccr);
                jsEngine.put("params",ctx.getRequest().getParameterMap());

                Matcher m = scriptPattern.matcher(scriptlet);
                int previousEnd = 0;
                while(m.find()) {
                    //jsEngine.eval(this.scriptlet);
                    out.print(scriptlet.substring(previousEnd,m.start()));
                    jsEngine.eval(m.group(1));
                    previousEnd = m.end();
                }
                out.print(scriptlet.substring(previousEnd));
            }
            catch (ScriptException ex) {
                ex.printStackTrace(out);
            }    

            this.scriptletOutput = s.toString();

            this.ctx.getRequest().getSession().setAttribute("scriptlet", this.scriptlet);
        }
        
        return new ForwardResolution("/scriptlet.jsp");
    }
    
    @Override
    public void setContext(ActionBeanContext ctx) {
        super.setContext(ctx);
        // Hack - let us just assume it's ccr zero if none specified
        // need a better soln, probably put it in session
        if(this.ccr == null) {
            this.ccr = session.getCcrs().get(0);
        }
    }

    public String getScriptlet() {
        return scriptlet;
    }

    public void setScriptlet(String scriptlet) {
        this.scriptlet = scriptlet;
    }

    public String getScriptletOutput() {
        return scriptletOutput;
    }

    public void setScriptletOutput(String scriptletOutput) {
        this.scriptletOutput = scriptletOutput;
    }

}
