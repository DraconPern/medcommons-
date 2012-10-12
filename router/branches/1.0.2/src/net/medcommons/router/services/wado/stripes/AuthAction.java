/*
 * $Id: AuthAction.java 2468 2008-03-13 06:27:56Z ssadedin $
 * Created on 22/11/2007
 */
package net.medcommons.router.services.wado.stripes;

import net.medcommons.modules.utils.Str;
import net.medcommons.router.web.stripes.BaseActionBean;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;

/**
 * The auth action does nothing except accept establish an authorized session with a gateway.
 * <p/>
 * It would usually be done prior to another action that would follow without authorization.
 * </p>
 * This action is useful because third party sites may wish to be able to post data to a
 * gateway without having explicit dependency on or knowledge of the gateway.  To do that
 * the best way is to redirect through central, however we cannot redirect posts through
 * central.  Therefore the best way to first load a trivial item (eg. image) that establishes the
 * authorization context via central redirection, then to submit the POST data directly to the 
 * gateway.  This way central automatically handles establishing the auth context in the gateway
 * but the subsequent post can proceed without a problem.
 * 
 * @author ssadedin
 */
public class AuthAction extends BaseActionBean {
    
    private String next;
    
    @DefaultHandler
    public Resolution auth() {
        if(Str.blank(next))
            return new ForwardResolution("/images/blank.gif");
        else
            return new RedirectResolution(next);
    }

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

}
