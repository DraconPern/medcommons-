/*
 * $Id: CSSAction.java 2256 2007-11-12 08:02:53Z ssadedin $
 * Created on 09/11/2007
 */
package net.medcommons.router.services.wado.stripes;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;

/**
 * This action is designed to optimize performance.   The idea is to prevent round trips by the browser
 * by bundling all the CSS together in one big batch. 
 * 
 * @author ssadedin
 */
public class CSSAction implements ActionBean {
    
    /**
     * By default make them revalidate once a day
     */
    public static long REVALIDATE_SECONDS = 86400;
    
    ActionBeanContext ctx = null;
    
    private String content;
    
    @DefaultHandler
    public Resolution css() {
        ctx.getResponse().setHeader("Cache-Control","max-age='"+REVALIDATE_SECONDS+"', must-revalidate'");
        ctx.getResponse().setContentType("text/css");
        return new ForwardResolution("/"+content+".css.ftl");
    }

    public Resolution viewer() {
        this.content="viewer";
        return css();
    }
    
    public ActionBeanContext getContext() {
        return ctx;
    }

    public void setContext(ActionBeanContext ctx) {
        this.ctx = ctx;

    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
