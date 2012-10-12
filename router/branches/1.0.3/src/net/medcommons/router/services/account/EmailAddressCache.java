/*
 * $Id: EmailAddressCache.java 3078 2008-11-14 05:28:16Z ssadedin $
 * Created on 08/10/2007
 */
package net.medcommons.router.services.account;

import static net.medcommons.modules.utils.Algorithm.map;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Pattern;

import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.modules.services.interfaces.ServicesFactory;
import net.medcommons.modules.utils.Function;
import net.medcommons.modules.utils.Str;

import org.apache.log4j.Logger;

/**
 * Caches resolved email addresses for account ids so that they do not need to be queried more than once. 
 * 
 * @author ssadedin
 */
public class EmailAddressCache {
    
    private static final int EMAIL_CACHE_ENTRY_EXPIRE_TIME_MS = 5*60*1000;

    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(EmailAddressCache.class);
    
    
    /**
     * Cache of resolved email addresses for accounts.
     */
    private static Hashtable<String, EmailEntry> accountToEmail = new Hashtable<String, EmailEntry>();
    
    private static Hashtable<String, EmailEntry> emailToAccount = new Hashtable<String, EmailEntry>();
    
    private static Pattern accountPattern = Pattern.compile("[0-9]{16}");
    
    
    /**
     * Attempts to translate each of the provided emails into Account IDs
     */
    public static List<String> translateAccounts(ServicesFactory f, List<String> emails) {
        
        final long now = System.currentTimeMillis();
        final ServicesFactory svc = f;
        List<String> results = map(emails, new Function<String, String>() {
            public String $(String email) {
                if(emailToAccount.containsKey(email) && now - emailToAccount.get(email).timestamp < EMAIL_CACHE_ENTRY_EXPIRE_TIME_MS) {
                    return emailToAccount.get(email).accountId;
                }
                else {
                    try {
                        log.info("Resolving email address " + email);
                        String [] resolved = svc.getAccountCreationService().translateAccounts(new String[] {email});
                        emailToAccount.put(email,new EmailEntry(resolved[0],email));
                        return resolved[0];
                    }
                    catch (ServiceException e) {
                        throw new RuntimeException("Failed to resolve account " + email, e);
                    }
                }
            }
        });
        return results;
    }
    
    /**
     * Attempts to translate each of the provided accounts into an email address.  If 
     * no email address is available for the requested account, returns a null entry for 
     * the corresponding item in the output array. 
     */
    public String [] translate(ServicesFactory f, String [] accounts) throws ServiceException {
        
        // Create an array with all the accounts we don't know
        String results[] = new String[ accounts.length ]; 
        ArrayList<String> unresolved = new ArrayList<String>();
        for(int i=0; i<results.length; ++i) {
            String account = accounts[i];
            if(Str.blank(account)) { // Should never happen
                results[i] = "Unidentified User";
                log.warn("Blank / null account passed to translate");
            }
            else
            if(accountToEmail.containsKey(account) ) {
               EmailEntry emailEntry = accountToEmail.get(account);
               long ageMs = System.currentTimeMillis() - emailEntry.timestamp;
               if(ageMs > EMAIL_CACHE_ENTRY_EXPIRE_TIME_MS) { // If more than 5 minutes old, re-resolve
                 unresolved.add(account);
               }
               else {
                 results[i] = emailEntry.email;
               }
            }
            else
            if(accountPattern.matcher(account).matches()) { // Looks like a medcommons account, try and translate it
                unresolved.add(account);
            }
            else  // Not a medcommons account.  Just render it literally.
                results[i] = account;
        }
        
        if(!unresolved.isEmpty()) {
            // Now resolve all the ones we don't know yet
            String[] resolved = f.getAccountCreationService().translate((String[]) unresolved.toArray(new String[unresolved.size()]));

            int j = 0; // index of resolved email
            for (int i = 0; i < results.length; ++i) {
                if (results[i] == null) {
                    assert j < resolved.length;
                    results[i] = resolved[j];
                    if (resolved[j] != null) {
                        log.info("Account " + unresolved.get(j) + " mapped to email " + resolved[j]);
                        accountToEmail.put(unresolved.get(j), new EmailEntry(unresolved.get(j), resolved[j]));
                    }
                    else {
                        if(log.isDebugEnabled())
                            log.debug("Account " + unresolved.get(j) + " not resolved");
                    }
                    
                    ++j;
                }
            }
        }
        return results;
    }

}
