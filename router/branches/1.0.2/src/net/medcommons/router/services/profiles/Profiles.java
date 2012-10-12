/*
 * $Id: Profiles.java 3736 2010-06-03 11:21:01Z ssadedin $
 * Created on 16/10/2008
 */
package net.medcommons.router.services.profiles;

import static net.medcommons.modules.utils.Str.blank;
import static net.medcommons.modules.utils.Str.eq;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

import net.medcommons.modules.services.interfaces.PHRProfile;

/**
 * Wrapper object for carrying a list of profiles
 * 
 * @author ssadedin
 */
public class Profiles {
    
    private List<PHRProfile> profiles = new ArrayList<PHRProfile>();
    
    public void add(PHRProfile p) {
        
        // If there is a profile of the same name, replace it
        boolean found = false;
        
        if(!blank(p.getName())) {
            for (ListIterator<PHRProfile> i = profiles.listIterator(); i.hasNext();) {
                PHRProfile existing = i.next();
                if(eq(existing.getName(), p.getName())) {
                    i.set(p);
                    found = true;
                    break;
                }
            }
        }
        
        if(!found)  
            profiles.add(p); 
        
        this.sort();
    }
    
    void sort() {
        Collections.sort(this.profiles, new  Comparator<PHRProfile>() {
            public int compare(PHRProfile o1, PHRProfile o2) {
                return o2.getDate().compareTo(o1.getDate());
            }
        });
    }

    public List<PHRProfile> getProfiles() {
        return profiles;
    }

    public void setProfiles(List<PHRProfile> profiles) {
        this.profiles = profiles;
    }
}
