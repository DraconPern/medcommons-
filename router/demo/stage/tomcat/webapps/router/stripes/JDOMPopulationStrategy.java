/*
 * $Id$
 * Created on 26/06/2006
 */
package net.medcommons.router.web.stripes;

import net.sourceforge.stripes.exception.StripesJspException;
import net.sourceforge.stripes.tag.DefaultPopulationStrategy;
import net.sourceforge.stripes.tag.InputTagSupport;

public class JDOMPopulationStrategy extends DefaultPopulationStrategy {

    public JDOMPopulationStrategy() {
        super();
    }

    @Override
    protected Object getValueFromActionBean(InputTagSupport arg0) throws StripesJspException {
        // Look in JDOM
        return super.getValueFromActionBean(arg0);
    }

    
}
