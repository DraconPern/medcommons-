/*
 * $Id$
 * Created on 26/06/2006
 */
package net.medcommons.router.services.wado.stripes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.medcommons.modules.services.interfaces.DirectoryEntry;
import net.medcommons.modules.utils.Str;
import net.medcommons.phr.ccr.CCRActorElement;
import net.medcommons.phr.ccr.CCRElement;
import net.medcommons.router.services.wado.NotLoggedInException;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;
import net.medcommons.router.web.stripes.CCRActionBean;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.ajax.JavaScriptResolution;

import org.jdom.filter.ElementFilter;

/**
 * Returns the todir for the current user.
 * @author ssadedin
 */
public class GetToDirAction extends CCRActionBean {

    public GetToDirAction() {
        super();
    } 
    
    @DefaultHandler
    public Resolution validate() throws Exception {
        if(this.session == null) 
            throw new NotLoggedInException();
        
       Set<String> alreadyAdded = new HashSet<String>();
       List<DirectoryEntry> remoteToDir = session.getToDir();
       List<DirectoryEntry> todir = new ArrayList<DirectoryEntry>(remoteToDir.size());
       for (DirectoryEntry entry : remoteToDir) {
           String key = entry.getAccid()+"."+entry.getContact()+"."+entry.getAlias();
           if(!alreadyAdded.contains(key)) {
               todir.add(entry);
               alreadyAdded.add(key);
           }
       }
       
       HashMap result = new HashMap();
       result.put("todir", todir);
       
       // Add entries from any CCRs loaded into the current session
       List<DirectoryEntry> ccrEntries = new ArrayList<DirectoryEntry>();
       for (CCRDocument ccr : this.session.getCcrs()) {
           // For all actors
           Iterator actors = ccr.getJDOMDocument().getDescendants(new ElementFilter("Actor",ccr.getNamespace()));
           while(actors.hasNext()) {
               CCRActorElement actor = (CCRActorElement) actors.next();
               
               // Extract first name, last name as alias
               CCRElement person = actor.getChild("Person");
               String accId = null;
               String email  = null;
               String name  = null;
               if(actor.getChild("EMail")!=null) {
                   email = actor.getChild("EMail").getChildTextTrim("Value");
               }
               if(person!=null) {
                   //email = XPathCache.getValue(person, "personEmail");
                   name = person.queryTextProperty("personName");
               }
               
               accId = actor.getAccountId();
               
               // If this account is not already in our list
               boolean found = false;
                                   
               // don't add it unless there is some form of contact means (accid, email)
               // and check there isn't already an identical entry
               String key = accId+"."+email+"."+name;
               if(!alreadyAdded.contains(key) && !(Str.blank(email) && Str.blank(accId))) { 
                   DirectoryEntry newEntry = new DirectoryEntry();
                   newEntry.setAlias(name);
                   newEntry.setAccid(accId);
                   newEntry.setContact(email);                        
                   if(Str.blank(email)) {
                       newEntry.setContact(accId);
                   }
                   ccrEntries.add(newEntry);
                   alreadyAdded.add(key);
               }
               
           }           
       }
       result.put("ccrDir", ccrEntries.toArray());
              
       return new JavaScriptResolution(result);
    }    
}
