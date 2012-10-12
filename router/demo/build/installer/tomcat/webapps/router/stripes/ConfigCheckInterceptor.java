/*
 * $Id: ConfigCheckInterceptor.java 2468 2008-03-13 06:27:56Z ssadedin $
 * Created on 06/09/2007
 */
package net.medcommons.router.web.stripes;

import org.apache.log4j.Logger;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.utils.Str;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.controller.ExecutionContext;
import net.sourceforge.stripes.controller.Intercepts;
import net.sourceforge.stripes.controller.LifecycleStage;

@Intercepts(LifecycleStage.HandlerResolution)
public class ConfigCheckInterceptor implements net.sourceforge.stripes.controller.Interceptor {
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(ConfigCheckInterceptor.class);

    public Resolution intercept(ExecutionContext ctx) throws Exception {
        String nodeId = Configuration.getProperty("NodeID");
        if(Str.blank(nodeId) || "UNKNOWN".equals(nodeId)) {
            log.info("NodeID is empty:  This instance will be disabled until a NodeID is entered");
            
            if(!ctx.getActionBean().getClass().isAnnotationPresent(AllowUnconfigured.class)) {
                return new ForwardResolution("/platform.jsp");
            }
        }
        
        return ctx.proceed();
    }

}
