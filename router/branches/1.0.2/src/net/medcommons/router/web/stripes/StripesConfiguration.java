/*
 * $Id$
 * Created on 26/06/2006
 */
package net.medcommons.router.web.stripes;

import org.apache.log4j.Logger;

import net.sourceforge.stripes.config.RuntimeConfiguration;
import net.sourceforge.stripes.controller.ActionBeanPropertyBinder;

public class StripesConfiguration extends RuntimeConfiguration {
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(StripesConfiguration.class);

    public StripesConfiguration() {
        super();
    }

    @Override
    protected ActionBeanPropertyBinder initActionBeanPropertyBinder() {
        ActionBeanPropertyBinder b = new JDOMPropertyBinder();
        try {
            b.init(this);
        }
        catch (Exception e) {
            log.error("Unable to initialize default Stripes ActionBeanPropertyBinder",e);
        }
        return b;
    }
}
