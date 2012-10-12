/*
 * $Id: XmlProfileService.java 3736 2010-06-03 11:21:01Z ssadedin $
 * Created on 16/10/2008
 */
package net.medcommons.router.services.profiles;

import static net.medcommons.modules.utils.Str.blank;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.configuration.ConfigurationException;
import net.medcommons.modules.services.interfaces.*;

import org.apache.log4j.Logger;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.StreamException;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * A simple {@link ProfileService} that stores a user's profiles as an 
 * XML file in their repository directory.  The files are
 * saved using {@link XStream} to produce a simple, automatic
 * rendering of the profiles to XML.
 * <p>
 * Backups of files are made using the small file {@link BackupService}
 * configured in the spring configuration file.
 * <p>TODO: implement some kind of locking.  The current implementation
 * is susceptible to a race condition when two separate users update 
 * a single set of profiles at the same time.  In such a situation
 * the last person to save will win.
 * 
 * @author ssadedin
 */
public class XmlProfileService implements ProfileService {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(XmlProfileService.class);
    
    /**
     * Service used to backup profiles
     */
    BackupService backupService = Configuration.getBean("smallFileBackupService");
    
    public XmlProfileService() throws ConfigurationException {
    }

    public void createProfile(String storageId, PHRProfile profile) throws ServiceException {
        
        if(blank(storageId) || ServiceConstants.PUBLIC_MEDCOMMONS_ID.equals(storageId))
            throw new IllegalArgumentException("Cannot store profiles for POPS users");
        
        try {
            log.info("Updating profiles for " + storageId);
            Profiles profiles = load(storageId);
            profiles.add(profile);
            save(storageId, profiles);
        }
        catch(IOException e) {
            throw new ServiceException("Failed to create / update profile " + profile,e);
        }
    }

    private Profiles load(String storageId) throws FileNotFoundException {
        File file = new File(getFilePath(storageId));
        XStream xstream = getXStream();
        Profiles profiles = null;
        if(file.exists()) {
            profiles = (Profiles) xstream.fromXML(new FileInputStream(file));
        }
        else {
            file.getParentFile().mkdirs();
            profiles = new Profiles();
        }
        return profiles;
    }
    
    
    private void save(String storageId, Profiles profiles) throws ServiceException {
        try {
            File file = new File(getFilePath(storageId));
            FileOutputStream out = new FileOutputStream(file);
            getXStream().toXML(profiles, out);
            out.close();
                         
            if(Configuration.getProperty("Backup_Documents", false)) {
                backupService.backup(storageId, file.getName(), file);
            }
        }
        catch(Exception e) {
            throw new ServiceException("Failed to save profiles for storageId = " + storageId, e);
        }
    }

    /**
     * Return profiles, fetching from S3 if they are not present locally
     */
    public List<PHRProfile> getProfiles(String storageId) throws ServiceException {
        try {
            File f = new File(getFilePath(storageId));
            if(!f.exists()) {
		        if(Configuration.getProperty("Backup_Documents", false)) {
		            backupService.restore(storageId, f.getName(), f);
		            
		        }
            }
            
            if(f.exists()) {
	            Profiles profiles = (Profiles) getXStream().fromXML(new FileInputStream(f));
	            profiles.sort();
	            return profiles.getProfiles();
            } 
            else 
                return new ArrayList<PHRProfile>();
        }
        catch (FileNotFoundException e) {
            throw new ServiceException("Failed to load profiles for storage id " + storageId, e);
        }
        
        catch (StreamException e){
            // This is thrown if the file is empty or corrupted
            throw new ServiceException("Failed to load profiles for storage id " + storageId, e);
        }
    }
    
    
    public void hideProfile(String storageId, String profileId) throws ServiceException {
        try {
            // Load the existing profiles
            Profiles profiles = load(storageId);
            
            // Remove the profiles from the list
            for (ListIterator<PHRProfile> iterator = profiles.getProfiles().listIterator(); iterator.hasNext();) {
                PHRProfile profile = (PHRProfile) iterator.next();
                if(profileId.equals(profile.getProfileId()))
                    iterator.remove();
            }
            this.save(storageId, profiles);
        }
        catch (FileNotFoundException e) {
            throw new ServiceException("Failed to hide " + profileId + " for storage id " + storageId, e);
        }
    }
    
    public void deleteProfile(String storageId) throws ServiceException {
        log.info("Deleting profiles for user " + storageId);
        
        File file = new File(getFilePath(storageId));
        if(file.exists()) { 
            if(!file.delete())
                throw new ServiceException("Unable to delete profiles for user " + storageId);
        }
        
        if(Configuration.getProperty("Backup_Documents", false)) {
            log.info("Deleting profile backup for user " + storageId);
	        this.backupService.delete(storageId, file.getName());
        }
    }
    
    /**
     * Return the file path of the profiles file for a given user
     * 
     * @param storageId
     * @return
     */
    private String getFilePath(String storageId) {
        return String.format("data/Repository/%s/profiles.xml", storageId);
    }

    /**
     * Create a new XStream instance appropriate for loading / saving profiles
     * @return
     */
    private XStream getXStream() {
        XStream xstream = new XStream(new DomDriver());
        xstream.alias("PHRProfiles", Profiles.class);
        xstream.alias("profile", PHRProfile.class);
        return xstream;
    }

}
