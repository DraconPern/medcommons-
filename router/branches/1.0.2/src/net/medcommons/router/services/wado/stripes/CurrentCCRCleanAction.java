/*
 * $Id: CurrentCCRCleanAction.java 2310 2007-12-13 21:37:14Z ssadedin $
 * Created on 14/12/2007
 */
package net.medcommons.router.services.wado.stripes;

import net.sourceforge.stripes.action.UrlBinding;

/**
 * A synonym for CurrentCCRAction that supports clean(er) urls 
 * 
 * @author ssadedin
 */
@UrlBinding("/currentccr")
public class CurrentCCRCleanAction extends CurrentCCRAction {

}
